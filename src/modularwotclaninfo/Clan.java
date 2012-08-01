package modularwotclaninfo;

import java.util.ArrayList;
import javax.swing.ImageIcon;

/**
 *
 * @author Yoyo117 (johnp)
 */
public class Clan {

    private final String name;
    private final String clanTag;
    private final long ID;
    private final ArrayList<Player> players;
    private final ArrayList<Vehicle> vehicles;
    private final double avg_wr;
    private final double avg_top_wr;
    private final double avg_eff;
    private final double avg_top_eff;

    private final ImageIcon emblem;


    Clan(String name, String clanTag, long ID, ArrayList<Player> players, ArrayList<Vehicle>
            vehicles, double avg_wr, double avg_top_wr, double avg_eff, double avg_top_eff,
            ImageIcon emblem)  {
        this.name = name;
        this.clanTag = clanTag;
        this.ID = ID;
        this.players = players;
        this.avg_wr = avg_wr;
        this.avg_top_wr = avg_top_wr;
        this.avg_eff = avg_eff;
        this.avg_top_eff = avg_top_eff;
        this.emblem = emblem;
        this.vehicles = vehicles;
    }

    public long getID() {
        return ID;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }

    public double getAvg_wr() {
        return avg_wr;
    }

    public double getAvg_Top_wr() {
        return avg_top_wr;
    }

    public ImageIcon getEmblem() {
        return emblem;
    }

    public String getClanTag() {
        return clanTag;
    }

    public String getName() {
        return name;
    }

    public double getAvg_eff() {
        return avg_eff;
    }

    public double getAvg_Top_eff() {
        return avg_top_eff;
    }
}
