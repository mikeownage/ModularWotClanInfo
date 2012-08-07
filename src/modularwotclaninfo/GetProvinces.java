package modularwotclaninfo;

import com.google.gson.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.SwingWorker;

/**
 *
 * @author Yoyo117 (johnp)
 */
public class GetProvinces extends SwingWorker<ArrayList<Province>, ArrayList<Province>>{

    private final long clanID;
    private final GUI gui;

    public GetProvinces(long clanID, GUI gui) {
        this.clanID = clanID;
        this.gui = gui;
    }

    @Override
    public ArrayList<Province> doInBackground() throws Exception {
        URL URL = new URL("http://worldoftanks."+gui.getServerRegion()+"/community/clans/"+clanID+"/provinces/?type=table&offset=0&limit=100&order_by=name&search=&echo=1&id=js-provinces-table");
        URLConnection URLConnection = URL.openConnection();
        URLConnection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
        URLConnection.setRequestProperty("Accept-Language", "en-us;q=0.5,en;q=0.3");
        URLConnection.setRequestProperty("Accept-Encoding", "paco");
        URLConnection.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        URLConnection.setRequestProperty("Connection", "close");
        URLConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        // timeout after 12 seconds
        URLConnection.setConnectTimeout(12000);

        StringBuilder data = new StringBuilder(25000);
        try (BufferedReader membersBufferedReader = new BufferedReader(new InputStreamReader(URLConnection.getInputStream(), "UTF8"))) {
            for (String line; (line = membersBufferedReader.readLine()) != null; data.append(line));
        }System.out.println("P:"+data.length());

        JsonParser jsonParser = new JsonParser();
        JsonObject json = jsonParser.parse(data.toString()).getAsJsonObject();

        String result = json.get("result").getAsString();
        if (!"success".equalsIgnoreCase(result)) {
            throw new ProvincesAPIException(result, gui);
        }

        JsonArray json_provinces = json.get("request_data").getAsJsonObject().get("items").getAsJsonArray();

        if (json_provinces.size() == 0) {
            return null; // no need for initializing an ArrayList
        }

        ArrayList<Province> provinces = new ArrayList<>(json_provinces.size());
        for (JsonElement e : json_provinces) {
            JsonObject o = e.getAsJsonObject();
            String name = o.get("name").getAsString();
            int arenaID = o.get("arena_id").getAsInt();
            long primeTime = (long)(o.get("prime_time").getAsDouble())*1000L;
            Date primeTimeDate = new Date(primeTime);
            boolean attacked = o.get("attacked").getAsBoolean();
            int revenue = o.get("revenue").getAsInt();
            int occupyTime = o.get("occupancy_time").getAsInt();
            boolean combatsRunning = o.get("combats_running").getAsBoolean();
            boolean capital = o.get("capital").getAsBoolean();
            String arenaName = o.get("arena_name").getAsString();
            String type = o.get("type").getAsString();
            String ID = o.get("id").getAsString();
            provinces.add(new Province(name, arenaID, primeTimeDate, attacked, revenue,
                    occupyTime, combatsRunning, capital, arenaName, type, ID));
        }
        return provinces;
    }
}
