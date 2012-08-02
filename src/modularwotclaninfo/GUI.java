package modularwotclaninfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import org.ini4j.Ini;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.RavenSkin;

/**
 *
 * @author Yoyo117 (johnp)
 */
public class GUI extends JFrame {

    public static void main(String[] params)
    {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run()
          {
                SubstanceLookAndFeel.setSkin(new RavenSkin());
                GUI gui = new GUI();
                gui.setLocationRelativeTo(null);
                gui.setVisible(true);
          }
        });
    }

    private static final NumberFormat formatter = NumberFormat.getPercentInstance();

    private VehiclePanel vPanel;
    private VehiclePanel vPlayerPanel;
    private TreeMap<String, Integer> minVClassTiers;
    private Clan clan;
    public Clan getClan() { return clan; }

    private PlayerListModel listModel;

    public GUI() {
        formatter.setMaximumFractionDigits(3);
        // create default minimum tiers TODO: rewrite this all better
        this.minVClassTiers = new TreeMap<>(SettingsFrame.VehicleClassComparator);
        this.minVClassTiers.put("Heavy Tank", 8);
        this.minVClassTiers.put("Medium Tank", 8);
        this.minVClassTiers.put("Light Tank", 4);
        this.minVClassTiers.put("Tank Destroyer", 8);
        this.minVClassTiers.put("SPG", 6);
        this.minVClassTiers.put("Unknown", 1);
        // try 2 load from ini or create first ini
        initialIniLoad();

        initComponents();
        this.listModel = new PlayerListModel();
        this.membersList.setModel(listModel);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initialIniLoad() {
        try {
            if (!SettingsFrame.settingsFile.exists()) {
                // initial ini create
                SettingsFrame.settingsFile.createNewFile();
                Ini ini = new Ini(SettingsFrame.settingsFile);
                ini.put("VehicleTiers", "Heavy Tank", 8);
                ini.put("VehicleTiers", "Medium Tank", 8);
                ini.put("VehicleTiers", "Light Tank", 4);
                ini.put("VehicleTiers", "Tank Destroyer", 8);
                ini.put("VehicleTiers", "SPG", 6);
                ini.put("VehicleTiers", "Unknown", 1);
                ini.store();
            } else {
                Ini ini = new Ini(SettingsFrame.settingsFile);
                iniLoadVClassMinTier(ini, "Heavy Tank");
                iniLoadVClassMinTier(ini, "Medium Tank");
                iniLoadVClassMinTier(ini, "Light Tank");
                iniLoadVClassMinTier(ini, "Tank Destroyer");
                iniLoadVClassMinTier(ini, "SPG");
                iniLoadVClassMinTier(ini, "Unknown");
            }
        } catch (Exception ex) { /* just skip */}
    }

    private void iniLoadVClassMinTier(Ini ini, String vClass) {
        try {
            String s_minTier = ini.get("VehicleTiers", vClass);
            if (s_minTier != null && !s_minTier.isEmpty()) {
                int minTier = Integer.parseInt(s_minTier);
                this.minVClassTiers.put(vClass, minTier);
            }
        } catch (Exception e) { /* just sip */ }
    }

    public void publishClanInfo(String clanTag, String name, int members, ImageIcon emblem) {
        tagLabel.setText(clanTag);
        nameLabel.setText(name);
        membersLabel.setText(Integer.toString(members));
        emblemLabel.setIcon(emblem);
    }

    public void publishClanPlayers(Clan clanFound) {
        // reset old UI/data
        reset();
        this.clan = clanFound;

        // re-enable button
        searchButton.setText("Search");
        searchButton.setEnabled(true);
        inputTextField.setEnabled(true);

        // build up new UI
        //tagLabel.setText(clan.getClanTag());
        //membersLabel.setText(Integer.toString(clan.getPlayers().size()));
        //nameLabel.setText(clan.getName());
        //emblemLabel.setIcon(clan.getEmblem());

        formatter.setMaximumFractionDigits(1);
        avgWr.setText(formatter.format(clan.getAvg_wr()));
        avgTopWr.setText(formatter.format(clan.getAvg_Top_wr()));
        formatter.setMaximumFractionDigits(3);

        avgEff.setText(Long.toString(Math.round(clan.getAvg_eff())));
        avgTopEff.setText(Long.toString(Math.round(clan.getAvg_Top_eff())));

        listModel.setList(clan.getPlayers());

        if (vPanel != null) mainVehiclePanel.remove(vPanel);
        vPanel = new VehiclePanel();
        for (Vehicle v : clan.getVehicles()) {
            if (v.getTier() >= minVClassTiers.get(v.getVClass())) {
                vPanel.addNation(v);
            }
        } // TODO: rewrite this all into a method elsewhere
        for (NationPanel n : vPanel.getNations()) {
            for (ClassPanel p : n.getVclasses()) {
                for (JLabel name : p.getNames()) {
                    name.addMouseListener(new VehicleNameMouseListener());
                }
            }
        }
        for (Player p : clan.getPlayers()) {
            switch(p.getMaxTier()) {
                case 10:
                    increaseCounter(tierXNum);
                    break;
                case 9:
                    increaseCounter(tierIXNum);
                    break;
                case 8:
                    increaseCounter(tierVIIINum);
                    break;
                case 7:
                    increaseCounter(tierVIINum);
                    break;
                case 6:
                    increaseCounter(tierVINum);
                    break;
                case 5:
                    increaseCounter(tierVNum);
                    break;
            }
        }
        mainVehiclePanel.add(vPanel, BorderLayout.CENTER);
    }

    private void increaseCounter(JLabel label) {
        label.setText(Integer.toString(Integer.parseInt(label.getText())+1));
    }

    public void errorPanel(String msg, String title) {
        try {
            JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
        } catch (org.pushingpixels.substance.api.UiThreadingViolationException e) { /* buggy */}
        //System.exit(-2*(msg.length()+title.length()));
    }

    protected void setMinVClassTiers(TreeMap<String, Integer> newMinVClassTiers) {
        if (this.minVClassTiers.equals(newMinVClassTiers)) return;
        this.minVClassTiers = newMinVClassTiers;
        // update UI
        if (vPanel != null) mainVehiclePanel.remove(vPanel);
        vPanel = new VehiclePanel();
        for (Vehicle v : clan.getVehicles()) {
            if (v.getTier() >= minVClassTiers.get(v.getVClass())) {
                vPanel.addNation(v);
            }
        }
        boolean found = !clicked;
        for (NationPanel n : vPanel.getNations()) {
            for (ClassPanel p : n.getVclasses()) {
                for (JLabel name : p.getNames()) {
                    name.addMouseListener(new VehicleNameMouseListener());
                    String vName = name.getText().substring(0, name.getText().length()-1); // cut ':'
                    if (vName.equals(nameEntered)) {
                        found = true;
                        name.setForeground(name.getForeground().brighter());
                    }
                }
            }
        }
        if (!found) {
            // selected vehicle is gone because it doesn't match minVClassTier
            // -> deselect manually
            listModel.setList(clan.getPlayers());
            nameEntered = null;
            clicked = false;
        }
        mainVehiclePanel.add(vPanel, BorderLayout.CENTER);
        this.pack();
        this.invalidate();
        this.repaint();
    }

    protected void inputReset() {
        searchButton.setEnabled(true);
        inputTextField.setEnabled(true);
        searchButton.setText("Search");
    }

    private void reset() {
        //tagLabel.setText("");
        //membersLabel.setText("");
        //nameLabel.setText("");

        tierXNum.setText("0");
        tierIXNum.setText("0");
        tierVIIINum.setText("0");
        tierVIINum.setText("0");
        tierVINum.setText("0");
        tierVNum.setText("0");

        listModel.clear();
        //emblemLabel.setIcon(null);
        avgWr.setText("");
        avgTopWr.setText("");
        avgEff.setText("");
        avgTopEff.setText("");

        victoriesLabel.setText("");
        defeatsLabel.setText("");
        hitRatioLabel.setText("");
        avgDmgLabel.setText("");
        avgExpLabel.setText("");
        battlesLabel.setText("");
        efficiencyLabel.setText("");
        leftPanel.setVisible(true);
        if (vPanel != null) vPanel.setVisible(true);
    }

    private void updatePlayersPanel(Player p) {
        playerName.setText(p.getName());
        victoriesLabel.setText(formatter.format(p.getAvg_wr()));
        defeatsLabel.setText(formatter.format(p.getAvg_lr()));
        hitRatioLabel.setText(Integer.toString(p.getHitRatio())+'%');
        avgDmgLabel.setText(Long.toString(Math.round(p.getAvg_dmg())));
        avgExpLabel.setText(Integer.toString(p.getAvg_xp()));
        battlesLabel.setText(Integer.toString(p.getBattles()));
        efficiencyLabel.setText(Long.toString(Math.round(p.getEfficiency())));
        avgSrvLabel.setText(formatter.format(p.getAvg_srv()));
    }

    private void showVehiclesOf(Player p) {
        if (vPlayerPanel != null) mainVehiclePanel.remove(vPlayerPanel);
        vPlayerPanel = new VehiclePanel();
        ArrayList<Vehicle> vehicles = Utils.sortVehiclesByTier(p.getVehicles());
        vehicles = Utils.sortVehiclesByClass(vehicles);
        vehicles = Utils.sortVehiclesByNation(vehicles);
        for (Vehicle v : vehicles) {
            if (v.getTier() >= minVClassTiers.get(v.getVClass())) {
                vPlayerPanel.addNation(v);
            }
        }
        vPanel.setVisible(false);
        mainVehiclePanel.add(vPlayerPanel, BorderLayout.CENTER);
    }

    private void showAllVehicles() {
        if (vPlayerPanel != null) mainVehiclePanel.remove(vPlayerPanel);
        vPlayerPanel = null;
        vPanel.setVisible(true);
    }

    private void startSearch() {
        searchButton.setEnabled(false);
        inputTextField.setEnabled(false);
        searchButton.setText("Running...");
        String input = inputTextField.getText().trim();
        if (input.isEmpty()) { // TODO: make button only active if not empty -> dynamic clan loading
            inputReset();
            inputTextField.setText("No input specified!");
            return;
        } // prevent "URL Injection"
        if (input.contains("&")) {
            inputReset();
            inputTextField.setText("Invalid input!");
            return;
        } // TODO: regex valid clan tags/names

        String searchFor = tagNameButtonGroup.getSelection().equals(tagButton.getModel()) ? "abbreviation" : "name";
        GetClanData gcd = new GetClanData(input, this, searchFor); // TODO: support clan name search
        try {
            gcd.execute();
        } catch (Exception e) { // TODO: search old (bluej-times) errorPanel with ExceptionScrollPane
            if (e instanceof ExecutionException) {
                Throwable t = e.getCause();
                if (t instanceof ProgrammException) {
                    ProgrammException pe = (ProgrammException)t;
                    pe.publish();
                } else if (t instanceof IOException) {
                    // TODO: differentiate between no connection and server down
                    IOException ioe = (IOException)t;
                    StringWriter sw = new StringWriter();
                    ioe.printStackTrace(new PrintWriter(sw));
                    errorPanel("Couldn't retrieve data.\n"
                            + "Please check if you can connect to the World of Tanks website.",
                            " Connection error (GUI)");
                } else {
                    StringWriter sw = new StringWriter();
                    t.printStackTrace(new PrintWriter(sw));
                    errorPanel("Unknown error. Please report:\n"
                            + t.getMessage() + '\n' + sw.toString(),
                            " Unknown execution error");

                }
                inputReset();
            } else {
                errorPanel("Unknown error. Please report:\n" + e.getMessage() + '\n'
                        + e.getStackTrace().toString(), " Unknown error");
            }
        }
    }

    private Player selectedPlayer;
    private class PlayerCellRenderer extends JLabel implements ListCellRenderer<Player> {
        PlayerCellRenderer() { setOpaque(true); }
        @Override
        public Component getListCellRendererComponent(JList<? extends Player> list,
                Player player, int index, boolean isSelected, boolean cellHasFocus) {
            setText(player.getName());
            setIcon(player.getClanRoleIcon());
            if (isSelected) {
                //setBackground(new Color(46, 44, 42));
                setBackground(Color.DARK_GRAY.darker());
                setForeground(ClassPanel.NAME_COLOR.brighter());
                //setForeground(list.getSelectionForeground());
                // prevent multiple execution
                if (!player.equals(selectedPlayer)) {
                    selectedPlayer = player;
                    updatePlayersPanel(player);
                    showVehiclesOf(player);
                }
            } else if ((index+1) % 2 == 0) {
                setBackground(new Color(20, 13, 6));
                setForeground(list.getForeground());
            } else {
                setBackground(new Color(21, 14, 7));
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            return this;
        }
    }

    private static String nameEntered;
    private static boolean clicked;
    private class VehicleNameMouseListener implements MouseListener {
        VehicleNameMouseListener() {}
        @Override
        public void mouseClicked(MouseEvent e) {
            JLabel srcLabel = (JLabel)e.getSource();
            String vName = srcLabel.getText().substring(0, srcLabel.getText().length()-1); // cut ':'
            if (nameEntered == null) { // nothing clicked yet
                specifyList(srcLabel, vName);
            }
            if (clicked) { // already clicked some item
                if (nameEntered.equals(vName)) { // it's this one hurray !!
                    listModel.setList(clan.getPlayers());
                    srcLabel.setForeground(ClassPanel.NAME_COLOR);
                    nameEntered = null;
                    clicked = false;
                } // TODO: add backreference and switch ??
            } else { // mouseEntered() already, but not clicked yet
                clicked = true;
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (!clicked && nameEntered == null) {
                JLabel srcLabel = (JLabel)e.getSource();
                String vName = srcLabel.getText().substring(0, srcLabel.getText().length()-1); // cut ':'
                specifyList(srcLabel, vName);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!clicked && nameEntered != null) {
                listModel.setList(clan.getPlayers());
                JLabel srcLabel = (JLabel)e.getSource();
                srcLabel.setForeground(ClassPanel.NAME_COLOR);
                nameEntered = null;
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {}
        @Override
        public void mouseReleased(MouseEvent e) {}

        private void specifyList(JLabel srcLabel, String vName) {
            nameEntered = vName;
            srcLabel.setForeground(srcLabel.getForeground().brighter());

            ArrayList<Player> players = new ArrayList<>(100); // worst case
            for (Player p : clan.getPlayers()) {
                for (Vehicle v : p.getVehicles())
                    if (v.getName().equals(vName))
                        players.add(p);
            }
            players.trimToSize();
            players = Utils.sortPlayersByWinrate(players); // do we rlly need this ?
            listModel.setList(players);
        }
    }

    private class PlayerListModel extends AbstractListModel {
        private ArrayList<Player> list;

        PlayerListModel() { this(new ArrayList<Player>(0)); }
        PlayerListModel(ArrayList<Player> list) {
            this.list = list;
        }

        @Override
        public Object getElementAt(int index) {
            return list.get(index);
        }

        @Override
        public int getSize() {
            return list.size();
        }

        public void add(Player player) {
            this.list.add(player);
            this.fireIntervalAdded(this, list.size()-2, list.size()-1);
        }

        public void addAll(ArrayList<Player> players) {
            int prevMaxIndex = list.size()-1;
            this.list.addAll(players);
            this.fireIntervalAdded(this, prevMaxIndex, list.size()-1);
        }

        public void setList(ArrayList<Player> list) {
            this.list = list;
            this.fireContentsChanged(this, 0, list.size()-1);
        }

        public void clear() {
            if (list.isEmpty()) return;
            int prevMaxIndex = list.size()-1;
            this.list.clear();
            this.fireIntervalRemoved(this, 0, prevMaxIndex);
        }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tagNameButtonGroup = new javax.swing.ButtonGroup();
        topPanel = new javax.swing.JPanel();
        topSeperator = new javax.swing.JSeparator();
        topLabel = new javax.swing.JLabel();
        optionsButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        searchLabel = new javax.swing.JLabel();
        inputTextField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        tagTextLabel = new javax.swing.JLabel();
        tagLabel = new javax.swing.JLabel();
        membersTextLabel = new javax.swing.JLabel();
        membersLabel = new javax.swing.JLabel();
        nameTextLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        leftPanel = new javax.swing.JPanel();
        leftTopPanel = new javax.swing.JPanel();
        tierLabel = new javax.swing.JLabel();
        numLabel = new javax.swing.JLabel();
        tierXLabel = new javax.swing.JLabel();
        tierIXLabel = new javax.swing.JLabel();
        tierVIIILabel = new javax.swing.JLabel();
        tierVIILabel = new javax.swing.JLabel();
        tierVILabel = new javax.swing.JLabel();
        tierVLabel = new javax.swing.JLabel();
        tierXNum = new javax.swing.JLabel();
        tierIXNum = new javax.swing.JLabel();
        tierVIIINum = new javax.swing.JLabel();
        tierVIINum = new javax.swing.JLabel();
        tierVINum = new javax.swing.JLabel();
        tierVNum = new javax.swing.JLabel();
        leftPlayerPanel = new javax.swing.JPanel();
        playerName = new javax.swing.JLabel();
        leftPlayerPanelSeperator = new javax.swing.JSeparator();
        victoriesTextLabel = new javax.swing.JLabel();
        defeatsTextLabel = new javax.swing.JLabel();
        hitRatioTextLabel = new javax.swing.JLabel();
        avgDmgTextLabel = new javax.swing.JLabel();
        avgExpTextLabel = new javax.swing.JLabel();
        battlesTextLabel = new javax.swing.JLabel();
        efficiencyTextLabel = new javax.swing.JLabel();
        avgSrvTextLabel = new javax.swing.JLabel();
        victoriesLabel = new javax.swing.JLabel();
        defeatsLabel = new javax.swing.JLabel();
        hitRatioLabel = new javax.swing.JLabel();
        avgDmgLabel = new javax.swing.JLabel();
        avgExpLabel = new javax.swing.JLabel();
        battlesLabel = new javax.swing.JLabel();
        efficiencyLabel = new javax.swing.JLabel();
        avgSrvLabel = new javax.swing.JLabel();
        leftPanelSeperator = new javax.swing.JSeparator();
        emblemLabel = new javax.swing.JLabel();
        avgWrTextLabel = new javax.swing.JLabel();
        avgTopWrTextLabel = new javax.swing.JLabel();
        avgWr = new javax.swing.JLabel();
        avgTopWr = new javax.swing.JLabel();
        clanStatsLabel = new javax.swing.JLabel();
        avgEffTextLabel = new javax.swing.JLabel();
        avgTopEffTextLabel = new javax.swing.JLabel();
        avgEff = new javax.swing.JLabel();
        avgTopEff = new javax.swing.JLabel();
        vehicleScrollPane = new javax.swing.JScrollPane();
        mainVehiclePanel = new javax.swing.JPanel();
        rightPanel = new javax.swing.JPanel();
        membersListScrollPane = new javax.swing.JScrollPane();
        membersList = new javax.swing.JList<>();
        createdByLabel = new javax.swing.JLabel();
        tagButton = new javax.swing.JRadioButton();
        nameButton = new javax.swing.JRadioButton();
        searchForLabel = new javax.swing.JLabel();
        tagNameButtonGroup.add(tagButton);
        tagNameButtonGroup.add(nameButton);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Modular WoT Clan Info - v0.10 alpha - by Yoyo117");
        setName("MainFrame"); // NOI18N
        setPreferredSize(new java.awt.Dimension(1130, 687));

        topPanel.setPreferredSize(new java.awt.Dimension(1130, 58));

        topSeperator.setForeground(new java.awt.Color(102, 102, 102));

        topLabel.setFont(new java.awt.Font("Terminator Two", 0, 16)); // NOI18N
        topLabel.setForeground(new java.awt.Color(255, 0, 0));
        topLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        topLabel.setText("Modular WoT Clan Info");
        topLabel.setToolTipText("");
        topLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        optionsButton.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        optionsButton.setForeground(new java.awt.Color(255, 255, 255));
        optionsButton.setText("Options");
        optionsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                optionsButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topSeperator)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addContainerGap(381, Short.MAX_VALUE)
                .addComponent(topLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 408, Short.MAX_VALUE)
                .addComponent(optionsButton)
                .addContainerGap())
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(topLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
                    .addComponent(optionsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(topSeperator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(topPanel, java.awt.BorderLayout.PAGE_START);

        mainPanel.setForeground(new java.awt.Color(255, 255, 255));
        mainPanel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        mainPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        mainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        searchLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        searchLabel.setForeground(new java.awt.Color(255, 255, 255));
        searchLabel.setText("Search:");
        searchLabel.setToolTipText("");
        mainPanel.add(searchLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(144, 0, 40, 25));

        inputTextField.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        inputTextField.setForeground(new java.awt.Color(255, 255, 255));
        inputTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputTextFieldActionPerformed(evt);
            }
        });
        mainPanel.add(inputTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(185, 3, 620, -1));

        searchButton.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        searchButton.setForeground(new java.awt.Color(255, 255, 255));
        searchButton.setText("Search");
        searchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchButtonMouseClicked(evt);
            }
        });
        mainPanel.add(searchButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 0, 124, -1));

        tagTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        tagTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        tagTextLabel.setText("Clan Tag:");
        mainPanel.add(tagTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(185, 31, -1, -1));

        tagLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        tagLabel.setForeground(new java.awt.Color(255, 255, 255));
        tagLabel.setToolTipText("");
        tagLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        mainPanel.add(tagLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(238, 31, 60, 20));

        membersTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        membersTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        membersTextLabel.setText("Members:");
        mainPanel.add(membersTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(304, 31, -1, -1));

        membersLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        membersLabel.setForeground(new java.awt.Color(255, 255, 255));
        membersLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        mainPanel.add(membersLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(359, 31, 45, 20));

        nameTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        nameTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        nameTextLabel.setText("Clan Name:");
        mainPanel.add(nameTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 31, -1, -1));

        nameLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        nameLabel.setForeground(new java.awt.Color(255, 255, 255));
        nameLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        mainPanel.add(nameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(471, 31, 389, 20));

        leftPanel.setForeground(new java.awt.Color(255, 255, 255));
        leftPanel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        leftPanel.setMaximumSize(new java.awt.Dimension(164, 530));
        leftPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        leftPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        leftTopPanel.setForeground(new java.awt.Color(255, 255, 255));
        leftTopPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tierLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 15)); // NOI18N
        tierLabel.setForeground(new java.awt.Color(255, 255, 255));
        tierLabel.setText("Tier");
        leftTopPanel.add(tierLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, 34, -1));

        numLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 15)); // NOI18N
        numLabel.setForeground(new java.awt.Color(255, 255, 255));
        numLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        numLabel.setText("#");
        leftTopPanel.add(numLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, 30, -1));

        tierXLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 14)); // NOI18N
        tierXLabel.setForeground(new java.awt.Color(255, 255, 255));
        tierXLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tierXLabel.setText("X");
        leftTopPanel.add(tierXLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 34, -1));

        tierIXLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 14)); // NOI18N
        tierIXLabel.setForeground(new java.awt.Color(255, 255, 255));
        tierIXLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tierIXLabel.setText("IX");
        leftTopPanel.add(tierIXLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, 33, 30));

        tierVIIILabel.setFont(new java.awt.Font("Comic Sans MS", 1, 14)); // NOI18N
        tierVIIILabel.setForeground(new java.awt.Color(255, 255, 255));
        tierVIIILabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tierVIIILabel.setText("VIII");
        leftTopPanel.add(tierVIIILabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, -1, 30));

        tierVIILabel.setFont(new java.awt.Font("Comic Sans MS", 1, 14)); // NOI18N
        tierVIILabel.setForeground(new java.awt.Color(255, 255, 255));
        tierVIILabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tierVIILabel.setText("VII");
        leftTopPanel.add(tierVIILabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, 33, -1));

        tierVILabel.setFont(new java.awt.Font("Comic Sans MS", 1, 14)); // NOI18N
        tierVILabel.setForeground(new java.awt.Color(255, 255, 255));
        tierVILabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tierVILabel.setText("VI");
        leftTopPanel.add(tierVILabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 160, 33, 30));

        tierVLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 14)); // NOI18N
        tierVLabel.setForeground(new java.awt.Color(255, 255, 255));
        tierVLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tierVLabel.setText("V");
        leftTopPanel.add(tierVLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 191, 33, 20));

        tierXNum.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tierXNum.setForeground(new java.awt.Color(255, 255, 255));
        tierXNum.setText("0");
        leftTopPanel.add(tierXNum, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 60, 30, 20));

        tierIXNum.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tierIXNum.setForeground(new java.awt.Color(255, 255, 255));
        tierIXNum.setText("0");
        leftTopPanel.add(tierIXNum, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 81, 31, 30));

        tierVIIINum.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tierVIIINum.setForeground(new java.awt.Color(255, 255, 255));
        tierVIIINum.setText("0");
        leftTopPanel.add(tierVIIINum, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 110, 31, 30));

        tierVIINum.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tierVIINum.setForeground(new java.awt.Color(255, 255, 255));
        tierVIINum.setText("0");
        leftTopPanel.add(tierVIINum, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 140, 31, 21));

        tierVINum.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tierVINum.setForeground(new java.awt.Color(255, 255, 255));
        tierVINum.setText("0");
        leftTopPanel.add(tierVINum, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 160, 31, 30));

        tierVNum.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tierVNum.setForeground(new java.awt.Color(255, 255, 255));
        tierVNum.setText("0");
        leftTopPanel.add(tierVNum, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 191, 31, 20));

        leftPanel.add(leftTopPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 160, 230));

        leftPlayerPanel.setForeground(new java.awt.Color(255, 255, 255));
        leftPlayerPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        playerName.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        playerName.setForeground(new java.awt.Color(255, 255, 255));
        leftPlayerPanel.add(playerName, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 4, 110, 30));
        leftPlayerPanel.add(leftPlayerPanelSeperator, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 32, 140, 10));

        victoriesTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        victoriesTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        victoriesTextLabel.setText("Victories:");
        leftPlayerPanel.add(victoriesTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 60, -1));

        defeatsTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        defeatsTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        defeatsTextLabel.setText("Defeats:");
        leftPlayerPanel.add(defeatsTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 60, -1));

        hitRatioTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        hitRatioTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        hitRatioTextLabel.setText("HitRatio:");
        leftPlayerPanel.add(hitRatioTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 60, -1));

        avgDmgTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgDmgTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        avgDmgTextLabel.setText("Avg Dmg:");
        leftPlayerPanel.add(avgDmgTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 60, -1));

        avgExpTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgExpTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        avgExpTextLabel.setText("Avg Exp:");
        leftPlayerPanel.add(avgExpTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 60, -1));

        battlesTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        battlesTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        battlesTextLabel.setText("Battles:");
        leftPlayerPanel.add(battlesTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 60, -1));

        efficiencyTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        efficiencyTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        efficiencyTextLabel.setText("Efficiency:");
        leftPlayerPanel.add(efficiencyTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 60, -1));

        avgSrvTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgSrvTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        avgSrvTextLabel.setText("Survived:");
        leftPlayerPanel.add(avgSrvTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, 60, -1));

        victoriesLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        victoriesLabel.setForeground(new java.awt.Color(238, 180, 84));
        leftPlayerPanel.add(victoriesLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 40, 70, 13));

        defeatsLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        defeatsLabel.setForeground(new java.awt.Color(238, 180, 84));
        leftPlayerPanel.add(defeatsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 60, 70, 13));

        hitRatioLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        hitRatioLabel.setForeground(new java.awt.Color(238, 180, 84));
        leftPlayerPanel.add(hitRatioLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 80, 70, 13));

        avgDmgLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgDmgLabel.setForeground(new java.awt.Color(238, 180, 84));
        leftPlayerPanel.add(avgDmgLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 100, 70, 13));

        avgExpLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgExpLabel.setForeground(new java.awt.Color(238, 180, 84));
        leftPlayerPanel.add(avgExpLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 120, 70, 13));

        battlesLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        battlesLabel.setForeground(new java.awt.Color(238, 180, 84));
        leftPlayerPanel.add(battlesLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 140, 70, 13));

        efficiencyLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        efficiencyLabel.setForeground(new java.awt.Color(238, 180, 84));
        leftPlayerPanel.add(efficiencyLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 160, 70, 13));

        avgSrvLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgSrvLabel.setForeground(new java.awt.Color(238, 180, 84));
        leftPlayerPanel.add(avgSrvLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 180, 70, 13));

        leftPanel.add(leftPlayerPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 160, 230));
        leftPanel.add(leftPanelSeperator, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 150, -1));

        emblemLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        emblemLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        leftPanel.add(emblemLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 270, 100, 80));

        avgWrTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgWrTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        avgWrTextLabel.setText("Avg winrate: ");
        leftPanel.add(avgWrTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 360, -1, -1));

        avgTopWrTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgTopWrTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        avgTopWrTextLabel.setText("   -> Top 20: ");
        leftPanel.add(avgTopWrTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 380, 70, -1));

        avgWr.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgWr.setForeground(new java.awt.Color(238, 180, 84));
        leftPanel.add(avgWr, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 360, 40, 10));

        avgTopWr.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgTopWr.setForeground(new java.awt.Color(238, 180, 84));
        leftPanel.add(avgTopWr, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 380, 40, 10));

        clanStatsLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 14)); // NOI18N
        clanStatsLabel.setForeground(new java.awt.Color(255, 255, 255));
        clanStatsLabel.setText("Clan Statistics");
        clanStatsLabel.setToolTipText("");
        leftPanel.add(clanStatsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, 110, 30));

        avgEffTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgEffTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        avgEffTextLabel.setText("Avg efficiency:");
        leftPanel.add(avgEffTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 400, 80, -1));

        avgTopEffTextLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgTopEffTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        avgTopEffTextLabel.setText("   -> Top 20:");
        leftPanel.add(avgTopEffTextLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 420, 70, -1));

        avgEff.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgEff.setForeground(new java.awt.Color(238, 180, 84));
        leftPanel.add(avgEff, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 400, 40, 10));

        avgTopEff.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        avgTopEff.setForeground(new java.awt.Color(238, 180, 84));
        leftPanel.add(avgTopEff, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 420, 40, 10));

        mainPanel.add(leftPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 62, 160, 510));

        vehicleScrollPane.setBorder(null);
        vehicleScrollPane.setForeground(new java.awt.Color(255, 255, 255));
        vehicleScrollPane.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N

        mainVehiclePanel.setForeground(new java.awt.Color(255, 255, 255));
        mainVehiclePanel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        mainVehiclePanel.setLayout(new java.awt.BorderLayout());
        vehicleScrollPane.setViewportView(mainVehiclePanel);

        mainPanel.add(vehicleScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 52, 700, 530));

        rightPanel.setMinimumSize(new java.awt.Dimension(0, 0));

        membersListScrollPane.setForeground(new java.awt.Color(255, 255, 255));
        membersListScrollPane.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        membersListScrollPane.setPreferredSize(new java.awt.Dimension(240, 561));

        membersList.setBackground(new java.awt.Color(15, 10, 5));
        membersList.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        membersList.setForeground(new java.awt.Color(255, 255, 255));
        membersList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        membersList.setCellRenderer(new PlayerCellRenderer());
        membersList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                membersListValueChanged(evt);
            }
        });
        membersListScrollPane.setViewportView(membersList);

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(membersListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addComponent(membersListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 49, Short.MAX_VALUE))
        );

        mainPanel.add(rightPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 31, -1, -1));

        createdByLabel.setFont(new java.awt.Font("Tahoma", 1, 9)); // NOI18N
        createdByLabel.setForeground(new java.awt.Color(255, 255, 255));
        createdByLabel.setText("Created by [HAVOC]Yoyo117 - EU Server");
        createdByLabel.setToolTipText("");
        mainPanel.add(createdByLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 580, -1, -1));

        tagButton.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        tagButton.setForeground(new java.awt.Color(255, 255, 255));
        tagButton.setSelected(true);
        tagButton.setText("Tag");
        mainPanel.add(tagButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 0, -1, -1));

        nameButton.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        nameButton.setForeground(new java.awt.Color(255, 255, 255));
        nameButton.setText("Name");
        mainPanel.add(nameButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 0, -1, -1));

        searchForLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        searchForLabel.setForeground(new java.awt.Color(255, 255, 255));
        searchForLabel.setText("Search for:");
        mainPanel.add(searchForLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 0, 60, 20));

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchButtonMouseClicked
        startSearch();
    }//GEN-LAST:event_searchButtonMouseClicked

    private void membersListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_membersListValueChanged
        if (evt.getValueIsAdjusting() == false) {
            int index = membersList.getSelectedIndex();
            if (index == -1) {
                leftTopPanel.setVisible(true);
                selectedPlayer = null;
                showAllVehicles();
            } else {
                leftTopPanel.setVisible(false);
            }
        }
    }//GEN-LAST:event_membersListValueChanged

    private void inputTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputTextFieldActionPerformed
        startSearch();
    }//GEN-LAST:event_inputTextFieldActionPerformed

    private void optionsButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_optionsButtonMouseClicked
        try {
            SettingsFrame s = new SettingsFrame(this);
            s.setLocationRelativeTo(this);
            s.setVisible(true);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            errorPanel("Couldn't load "+SettingsFrame.settingsFile.getName()+":"
                    + e.getMessage() + '\n' + sw.toString(),
                    " Error loading config file");
        }
    }//GEN-LAST:event_optionsButtonMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel avgDmgLabel;
    private javax.swing.JLabel avgDmgTextLabel;
    private javax.swing.JLabel avgEff;
    private javax.swing.JLabel avgEffTextLabel;
    private javax.swing.JLabel avgExpLabel;
    private javax.swing.JLabel avgExpTextLabel;
    private javax.swing.JLabel avgSrvLabel;
    private javax.swing.JLabel avgSrvTextLabel;
    private javax.swing.JLabel avgTopEff;
    private javax.swing.JLabel avgTopEffTextLabel;
    private javax.swing.JLabel avgTopWr;
    private javax.swing.JLabel avgTopWrTextLabel;
    private javax.swing.JLabel avgWr;
    private javax.swing.JLabel avgWrTextLabel;
    private javax.swing.JLabel battlesLabel;
    private javax.swing.JLabel battlesTextLabel;
    private javax.swing.JLabel clanStatsLabel;
    private javax.swing.JLabel createdByLabel;
    private javax.swing.JLabel defeatsLabel;
    private javax.swing.JLabel defeatsTextLabel;
    private javax.swing.JLabel efficiencyLabel;
    private javax.swing.JLabel efficiencyTextLabel;
    private javax.swing.JLabel emblemLabel;
    private javax.swing.JLabel hitRatioLabel;
    private javax.swing.JLabel hitRatioTextLabel;
    private javax.swing.JTextField inputTextField;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JSeparator leftPanelSeperator;
    private javax.swing.JPanel leftPlayerPanel;
    private javax.swing.JSeparator leftPlayerPanelSeperator;
    private javax.swing.JPanel leftTopPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel mainVehiclePanel;
    private javax.swing.JLabel membersLabel;
    private javax.swing.JList membersList;
    private javax.swing.JScrollPane membersListScrollPane;
    private javax.swing.JLabel membersTextLabel;
    private javax.swing.JRadioButton nameButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel nameTextLabel;
    private javax.swing.JLabel numLabel;
    private javax.swing.JButton optionsButton;
    private javax.swing.JLabel playerName;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JButton searchButton;
    private javax.swing.JLabel searchForLabel;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JRadioButton tagButton;
    private javax.swing.JLabel tagLabel;
    private javax.swing.ButtonGroup tagNameButtonGroup;
    private javax.swing.JLabel tagTextLabel;
    private javax.swing.JLabel tierIXLabel;
    private javax.swing.JLabel tierIXNum;
    private javax.swing.JLabel tierLabel;
    private javax.swing.JLabel tierVIIILabel;
    private javax.swing.JLabel tierVIIINum;
    private javax.swing.JLabel tierVIILabel;
    private javax.swing.JLabel tierVIINum;
    private javax.swing.JLabel tierVILabel;
    private javax.swing.JLabel tierVINum;
    private javax.swing.JLabel tierVLabel;
    private javax.swing.JLabel tierVNum;
    private javax.swing.JLabel tierXLabel;
    private javax.swing.JLabel tierXNum;
    private javax.swing.JLabel topLabel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JSeparator topSeperator;
    private javax.swing.JScrollPane vehicleScrollPane;
    private javax.swing.JLabel victoriesLabel;
    private javax.swing.JLabel victoriesTextLabel;
    // End of variables declaration//GEN-END:variables
}
