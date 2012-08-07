package modularwotclaninfo;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Yoyo117 (johnp)
 */
public class ProvincesPanel extends javax.swing.JPanel {

    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final GUI gui;

    private ArrayList<Province> provinces;
    private ProvinceTableModel tableModel;

    /**
     * Creates new form ProvincesPanel
     * @param provinces ArrayList of provinces to display
     * @param gui Reference to main class gui
     */
    public ProvincesPanel(ArrayList<Province> provinces, GUI gui) {
        this.provinces = provinces;
        this.gui = gui;
        initComponents();
        provincesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        provincesTable.getSelectionModel().addListSelectionListener(
                new ProvinceTableSelectionListener(provincesTable));
        provincesTable.getColumnModel().getColumn(0).setPreferredWidth(1);
        provincesTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        provincesTable.getColumnModel().getColumn(2).setPreferredWidth(125);
        provincesTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        provincesTable.getColumnModel().getColumn(5).setPreferredWidth(55);
        provincesTable.getColumnModel().getColumn(4).setPreferredWidth(45);
        provincesTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        setProvinces();
    }

    private void setProvinces() {
        goldPerDay.setText(gui.getGoldPerDay());
        int goldSum = 0;
        for (Province p : provinces) {
            goldSum += p.getRevenue() * p.getOccupyTime();
        }
        totalGold.setText(Integer.toString(goldSum));
        Object[][] data = new Object[provinces.size()][tableModel.getColumnCount()];
        int i = 0;
        for (Province p : provinces) {
            if (p.isCapital()) {
                data[i][0] = new ImageIcon(getClass().getResource("/img/hqProvince.png"), "Headquarter province");
            } else if ("normal".equals(p.getType())) {
                data[i][0] = new ImageIcon(getClass().getResource("/img/normalProvince.png"), "Normal province");
            } else if ("start".equals(p.getType())) {
                data[i][0] = new ImageIcon(getClass().getResource("/img/startProvince.png"), "Landing province");
            } else { // gold province
                data[i][0] = new ImageIcon(getClass().getResource("/img/goldProvince.png"), "Gold province");
            }
            data[i][1] = p.getName();
            data[i][2] = p.getArenaName();
            data[i][3] = dateFormat.format(p.getPrimeTime());
            data[i][4] = p.getRevenue();
            data[i][5] = p.getOccupyTime();
            data[i][6] = p.isAttacked() || p.isCombatsRunning();
            i++;
        }
        tableModel.setNewData(data);
    }

    private class ProvinceTableModel extends AbstractTableModel {
        private String[] columns = {"Type", "Province", "Map", "Time", "Gold", "Days", "Under Attack"};
        private Class[] columnsClasses = {ImageIcon.class, String.class, String.class, String.class, Integer.class, Integer.class, Boolean.class};
        private Object[][] data;

        ProvinceTableModel() { this(new Object[7][0]); }
        ProvinceTableModel(Object[][] data) {
            setNewData(data);
        }

        public void setNewData(Object[][] data) {
          this.data = data;
          fireTableDataChanged();
        }

        public void setNewColumNames(String[] columns) {
          this.columns = columns;
          fireTableStructureChanged();
        }

        @Override
        public int getColumnCount() {
            return this.columns.length;
        }

        @Override
        public int getRowCount() {
            return this.data.length;
        }

        @Override
        public String getColumnName(int index) {
            return this.columns[index];
        }

        @Override
        public Object getValueAt(int i1, int i2) {
            return this.data[i1][i2];
        }

        @Override
        public Class getColumnClass(int index) {
            try {
                return columnsClasses[index];
            } catch (NullPointerException e) { return null; }
        }
    }

    private class ProvinceTableSelectionListener implements ListSelectionListener {
        JTable table;

        ProvinceTableSelectionListener(JTable table) {
            this.table = table;
        }

        @Override
        public void valueChanged(ListSelectionEvent evt) {
            if (!evt.getValueIsAdjusting()) {
                try {
                    Desktop.getDesktop().browse(new URI("http://uc.worldoftanks."
                            + gui.getServerRegion()+"/uc/clanwars/maps/?province="
                            + provinces.get(table.getSelectedRow()).getID()));
                } catch (URISyntaxException | IOException e) {
                    gui.errorPanel("Couldn't open the worldmap in browser:\n"
                            + e.getMessage(), " ProvinceURLError");
                }
            }
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
        java.awt.GridBagConstraints gridBagConstraints;

        provincesLabel = new javax.swing.JLabel();
        goldPerDayLabel = new javax.swing.JLabel();
        totalGoldLabel = new javax.swing.JLabel();
        provincesScrollPane = new javax.swing.JScrollPane();
        tableModel = new ProvinceTableModel();
        provincesTable = new javax.swing.JTable(tableModel);
        verticalFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        goldPerDay = new javax.swing.JLabel();
        totalGold = new javax.swing.JLabel();
        leftFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        rightFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        midFiller = new javax.swing.Box.Filler(new java.awt.Dimension(50, 0), new java.awt.Dimension(50, 0), new java.awt.Dimension(50, 32767));

        setPreferredSize(new java.awt.Dimension(680, 500));
        setLayout(new java.awt.GridBagLayout());

        provincesLabel.setFont(new java.awt.Font("Tahoma", 0, 25)); // NOI18N
        provincesLabel.setForeground(new java.awt.Color(255, 255, 255));
        provincesLabel.setText("Provinces");
        provincesLabel.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 7;
        add(provincesLabel, gridBagConstraints);

        goldPerDayLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        goldPerDayLabel.setForeground(new java.awt.Color(255, 255, 255));
        goldPerDayLabel.setText("Total Gold per Day: ");
        goldPerDayLabel.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        add(goldPerDayLabel, gridBagConstraints);

        totalGoldLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        totalGoldLabel.setForeground(new java.awt.Color(255, 255, 255));
        totalGoldLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        totalGoldLabel.setText("Total Gold per Time Owned: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        add(totalGoldLabel, gridBagConstraints);

        provincesScrollPane.setMinimumSize(new java.awt.Dimension(700, 475));
        provincesScrollPane.setPreferredSize(new java.awt.Dimension(700, 475));

        provincesTable.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        provincesTable.setForeground(new java.awt.Color(255, 255, 255));
        provincesTable.setToolTipText("");
        provincesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        provincesTable.setMaximumSize(new java.awt.Dimension(700, 450));
        provincesTable.setMinimumSize(new java.awt.Dimension(700, 400));
        provincesTable.setOpaque(false);
        provincesTable.setPreferredSize(new java.awt.Dimension(700, 450));
        provincesTable.setRowHeight(25);
        provincesTable.setSelectionBackground(new java.awt.Color(50, 40, 25));
        provincesTable.setSelectionForeground(new java.awt.Color(250, 200, 75));
        provincesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        provincesTable.getTableHeader().setResizingAllowed(false);
        provincesTable.getTableHeader().setReorderingAllowed(false);
        provincesScrollPane.setViewportView(provincesTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        add(provincesScrollPane, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        add(verticalFiller, gridBagConstraints);

        goldPerDay.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        goldPerDay.setForeground(new java.awt.Color(255, 172, 0));
        goldPerDay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/gold.png"))); // NOI18N
        goldPerDay.setText("0");
        goldPerDay.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        goldPerDay.setIconTextGap(2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        add(goldPerDay, gridBagConstraints);

        totalGold.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        totalGold.setForeground(new java.awt.Color(255, 172, 0));
        totalGold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/gold.png"))); // NOI18N
        totalGold.setText("0");
        totalGold.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        totalGold.setIconTextGap(2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        add(totalGold, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 0.1;
        add(leftFiller, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 0.1;
        add(rightFiller, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        add(midFiller, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel goldPerDay;
    private javax.swing.JLabel goldPerDayLabel;
    private javax.swing.Box.Filler leftFiller;
    private javax.swing.Box.Filler midFiller;
    private javax.swing.JLabel provincesLabel;
    private javax.swing.JScrollPane provincesScrollPane;
    private javax.swing.JTable provincesTable;
    private javax.swing.Box.Filler rightFiller;
    private javax.swing.JLabel totalGold;
    private javax.swing.JLabel totalGoldLabel;
    private javax.swing.Box.Filler verticalFiller;
    // End of variables declaration//GEN-END:variables
}
