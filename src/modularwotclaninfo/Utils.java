package modularwotclaninfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingUtilities;

/**
 *
 * @author Yoyo117 (johnp)
 *
 * Note: .asList() just gives reference so we can fast-return argument Arrays
 */
public class Utils {
    private Utils() {}

    private static final Comparator<Player> PlayerWinrateComparator = new Comparator<Player>() {
        @Override
        public int compare(Player p1, Player p2) {
            return Double.compare(p2.getAvg_wr(), p1.getAvg_wr());
        }
    };

    private static final Comparator<Player> PlayerEfficiencyComparator = new Comparator<Player>() {
        @Override
        public int compare(Player p1, Player p2) {
            return Double.compare(p2.getEfficiency(), p1.getEfficiency());
        }
    };

    private static final Comparator<Vehicle> VehicleTierComparator = new Comparator<Vehicle>() {
        @Override
        public int compare(Vehicle v1, Vehicle v2) {
            return v2.getTier() - v1.getTier();
        }
    };

    private static final Comparator<Vehicle> VehicleNationComparator = new Comparator<Vehicle>() {
        @Override
        public int compare(Vehicle v1, Vehicle v2) {
            return v2.getNation().compareTo(v1.getNation());
        }
    };

    private static final Comparator<Vehicle> VehicleClassComparator = new Comparator<Vehicle>() {
        @Override
        public int compare(Vehicle v1, Vehicle v2) {
            return compareVehicleClasses(v1.getVClass(), v2.getVClass());
        }
    };

    // manually modified for UI purposes (TODO: IMPROVE ME)
    public static int compareVehicleClasses(String class1, String class2) {
        //hvy@top
        boolean v1Hvy = class1.equals("Heavy Tank");
        boolean v2Hvy = class2.equals("Heavy Tank");
        if (v1Hvy  && v2Hvy)  return 0;
        if (v1Hvy  && !v2Hvy) return -1;
        if (!v1Hvy && v2Hvy)  return 1;

        //Med>Light
        boolean v1Med = class1.equals("Medium Tank");
        boolean v2Med = class2.equals("Medium Tank");
        if (v1Med && v2Med) return 0;
        boolean v1Light = class1.equals("Light Tank");
        if (v1Light && v2Med) return 1;
        boolean v2Light = class2.equals("Light Tank");
        if (v1Light && v2Light) return 0;
        if (v1Med   && v2Light) return -1;


        //SPG@bottom
        boolean v1SPG = class1.equals("SPG");
        boolean v2SPG = class2.equals("SPG");
        if (v1SPG  && v2SPG)  return 0;
        if (v1SPG  && !v2SPG) return 1;
        if (!v1SPG && v2SPG)  return -1;
        return class1.compareTo(class2);
    }

    public static ArrayList<Player> sortPlayersByWinrate(ArrayList<Player> players) {
        Collections.sort(players, PlayerWinrateComparator);
        return players;
    }

    public static Player[] sortPlayersByWinrate(Player[] players) {
        List<Player> players_list = Arrays.asList(players);
        Collections.sort(players_list, PlayerWinrateComparator);
        return players;
    }

    public static ArrayList<Player> sortPlayersByEfficiency(ArrayList<Player> players) {
        Collections.sort(players, PlayerEfficiencyComparator);
        return players;
    }

    public static Player[] sortPlayersByEfficiency(Player[] players) {
        List<Player> players_list = Arrays.asList(players);
        Collections.sort(players_list, PlayerEfficiencyComparator);
        return players;
    }

    public static ArrayList<Vehicle> sortVehiclesByTier(ArrayList<Vehicle> vehicles) {
        Collections.sort(vehicles, VehicleTierComparator);
        return vehicles;
    }

    public static Vehicle[] sortVehiclesByTier(Vehicle[] vehicles) {
        List<Vehicle> players_list = Arrays.asList(vehicles);
        Collections.sort(players_list, VehicleTierComparator);
        return vehicles;
    }

    public static ArrayList<Vehicle> sortVehiclesByNation(ArrayList<Vehicle> vehicles) {
        Collections.sort(vehicles, VehicleNationComparator);
        return vehicles;
    }

    public static Vehicle[] sortVehiclesByNation(Vehicle[] vehicles) {
        List<Vehicle> players_list = Arrays.asList(vehicles);
        Collections.sort(players_list, VehicleNationComparator);
        return vehicles;
    }

     public static ArrayList<Vehicle> sortVehiclesByClass(ArrayList<Vehicle> vehicles) {
        Collections.sort(vehicles, VehicleClassComparator);
        return vehicles;
    }

    public static Vehicle[] sortVehiclesByClass(Vehicle[] vehicles) {
        List<Vehicle> players_list = Arrays.asList(vehicles);
        Collections.sort(players_list, VehicleClassComparator);
        return vehicles;
    }

    public static void handleExecutionException(ExecutionException e, final GUI gui) {
        final Throwable t = e.getCause();
        if (t instanceof ExecutionException) { // comes from GetPlayerData (doubled)
            // not in EDT !! -> fix this
            final Throwable gpdT = t.getCause();
            final StringWriter sw = new StringWriter();
            gpdT.printStackTrace(new PrintWriter(sw));
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run(){
                    gui.errorPanel("Unknown error. Please report:\n"
                    + gpdT.getMessage() + '\n' + sw.toString(),
                    " Unknown execution error");
                }
            });
        } else if (t instanceof ProgrammException) {
            ProgrammException pe = (ProgrammException)t;
            pe.publish();
        } else if (t instanceof IOException) {
            // TODO: differentiate between no connection and server down
            IOException ioe = (IOException)t;
            StringWriter sw = new StringWriter();
            ioe.printStackTrace(new PrintWriter(sw));
            gui.errorPanel("Couldn't retrieve data.\n"
                    + "Please check if you can connect to the World of Tanks website.\n"
                    + "StackTrace:\n" + t.getMessage() + '\n' + sw.toString(),
                    " Connection error");
        } else {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            gui.errorPanel("Unknown error. Please report:\n"
                    + t.getMessage() + '\n' + sw.toString(),
                    " Unknown execution error");

        }
        gui.inputReset();
    }
}
