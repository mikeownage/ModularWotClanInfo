package modularwotclaninfo;

import java.util.ArrayList;
import javax.swing.Icon;

/**
 *
 * @author Yoyo117 (johnp)
 */
public class Player {

    private final long ID;
    private final double lastActive;
    private final String name;
    private final String clanRole;
    private final Icon clanRoleIcon;
    private final int maxTier;
    private final int battles;
    private final int hitRatio;
    private final int avg_xp;
    private final double avg_dmg;
    private final double avg_wr;
    private final double avg_lr;
    private final double avg_srv;
    private final double eff;
    private final ArrayList<Vehicle> vehicles;


    public Player(long ID, double lastActive, String name, String clanRole,
            Icon clanRoleIcon, int maxTier, int battles, int hitRatio, double avg_dmg,
            double avg_wr, double avg_lr, double avg_srv, int avg_xp, double eff,
            ArrayList<Vehicle> vehicles) {
        this.ID = ID;
        this.lastActive = lastActive;
        this.name = name;
        this.clanRole = clanRole;
        this.clanRoleIcon = clanRoleIcon;
        this.maxTier = maxTier;
        this.battles = battles;
        this.hitRatio = hitRatio;
        this.avg_dmg = avg_dmg;
        this.avg_wr = avg_wr;
        this.avg_lr = avg_lr;
        this.avg_srv = avg_srv;
        this.avg_xp = avg_xp;
        this.eff = eff;
        this.vehicles = vehicles;
    }

    @Override
    public String toString() {
        return name;
    }

    public long getID() {
        return ID;
    }

    public double getLastActive() {
        return lastActive;
    }

    public String getName() {
        return name;
    }

    public double getAvg_wr() {
        return avg_wr;
    }

    public int getAvg_xp() {
        return avg_xp;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }

    public String getClanRole() {
        return clanRole;
    }

    public Icon getClanRoleIcon() {
        return clanRoleIcon;
    }

    public int getBattles() {
        return battles;
    }

    public int getHitRatio() {
        return hitRatio;
    }

    public double getAvg_dmg() {
        return avg_dmg;
    }

    public double getAvg_lr() {
        return avg_lr;
    }

    public int getMaxTier() {
        return maxTier;
    }

    public double getEfficiency() {
        return eff;
    }

    public double getAvg_srv() {
        return avg_srv;
    }
}
