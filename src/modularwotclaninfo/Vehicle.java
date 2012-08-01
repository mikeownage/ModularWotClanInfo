package modularwotclaninfo;

/**
 *
 * @author Yoyo117 (johnp)
 */
public class Vehicle {

    private final String name;
    private final String nation;
    private final String vclass;
    private final int tier;
    private final int battles;
    private final double winrate;
    // TODO: private final boolean premium


    Vehicle(String name, String nation, String vclass, int level, int battles, double winrate)
    {
        this.name = name;
        this.nation = nation;
        this.vclass = vclass;
        this.tier = level;
        this.battles = battles;
        this.winrate = winrate;
    }

    @Override
    public String toString() {
        return new StringBuilder("Name: ").append(name).
                    append("\nNation: ").append(nation).
                    append("\nClass: ").append(vclass).
                    append("\nBattles: ").append(battles).
                    append("\nWinrate: ").append(winrate).toString();
    }

    public String getName() {
        return name;
    }

    public String getNation() {
        return nation;
    }

    public String getVClass() {
        return vclass;
    }

    public int getBattles() {
        return battles;
    }

    public double getWinrate() {
        return winrate;
    }

    public int getTier() {
        return tier;
    }
}
