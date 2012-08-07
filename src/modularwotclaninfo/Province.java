package modularwotclaninfo;

import java.util.Date;

/**
 *
 * @author Yoyo117 (johnp)
 */
@SuppressWarnings("SuspiciousConstantFieldName")
public class Province {

    private final String name;
    private final int arenaID;
    private final Date primeTime;
    private final boolean attacked;
    private final int revenue;
    private final int occupyTime;
    private final boolean combatsRunning;
    private final boolean capital;
    private final String arenaName;
    private final String type;
    private final String ID;

    public Province(String name, int arenaID, Date primeTime, boolean attacked,
            int revenue, int occupyTime, boolean combatsRunning, boolean capital,
            String arenaName, String type, String ID) {
        this.name = name;
        this.arenaID = arenaID;
        this.primeTime = primeTime;
        this.attacked = attacked;
        this.revenue = revenue;
        this.occupyTime = occupyTime;
        this.combatsRunning = combatsRunning;
        this.capital = capital;
        this.arenaName = arenaName;
        this.type = type;
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public int getArenaID() {
        return arenaID;
    }

    public Date getPrimeTime() {
        return primeTime;
    }

    public boolean isAttacked() {
        return attacked;
    }

    public int getRevenue() {
        return revenue;
    }

    public int getOccupyTime() {
        return occupyTime;
    }

    public boolean isCombatsRunning() {
        return combatsRunning;
    }

    public boolean isCapital() {
        return capital;
    }

    public String getArenaName() {
        return arenaName;
    }

    public String getType() {
        return type;
    }

    public String getID() {
        return ID;
    }
}
