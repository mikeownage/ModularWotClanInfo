package modularwotclaninfo;

import com.google.gson.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

/**
 *
 * @author Yoyo117 (johnp)
 */
public class GetClanData extends SwingWorker<Clan, Clan> {

    private final GUI gui;

    private final String fallbackInput;
    private final String searchType;
    private PossibleClan bestMatch;

    private ArrayList<Player> players;
    private ArrayList<Province> provinces;

    protected GetClanData(PossibleClan bestMatch, String fallbackInput, String searchType, GUI gui)
    {
        this.bestMatch = bestMatch;
        this.gui = gui;
        this.fallbackInput = fallbackInput;
        this.searchType = searchType;
    }

    @Override
    @SuppressWarnings("empty-statement")
    protected Clan doInBackground() throws Exception
    {
        if (bestMatch == null) { // use fallBack
            //URL URL = new URL("http://worldoftanks."+gui.getServerRegion()+"/uc/clans/?type=table&offset=0&limit=10&order_by="+this.searchType+"&search="+this.clanTagName+"&echo=2&id=clans_index");
            // TODO: does API support orderBy ?!?
            URL URL = new URL("http://worldoftanks."+gui.getServerRegion()+"/uc/clans/api/1.1/?source_token=Intellect_Soft-WoT_Mobile-unofficial_stats&search="+this.fallbackInput.replace(" ", "%20")+"&offset=0&limit=10");
            URLConnection URLConnection = URL.openConnection();
            URLConnection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            URLConnection.setRequestProperty("Accept-Language", "en-us;q=0.5,en;q=0.3");
            URLConnection.setRequestProperty("Accept-Encoding", "paco");
            URLConnection.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
            URLConnection.setRequestProperty("Connection", "close");
            URLConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            // timeout after 10 seconds
            URLConnection.setConnectTimeout(10000);

            BufferedReader clanBufferedReader = null;
            StringBuilder data = new StringBuilder(3000);
            try {
                clanBufferedReader = new BufferedReader(new InputStreamReader(URLConnection.getInputStream(), "UTF8"));
                for (String line; (line = clanBufferedReader.readLine()) != null; data.append(line));
            } finally {
                if (clanBufferedReader != null) clanBufferedReader.close();
            }

            JsonParser jsonParser = new JsonParser();
            JsonObject json = jsonParser.parse(data.toString()).getAsJsonObject();

            if (!"ok".equalsIgnoreCase(json.get("status").getAsString())) {
                throw new ClanAPIException(json.get("status_code").getAsString(), this.gui);
            }

            JsonArray results = json.get("data").getAsJsonObject().get("items").getAsJsonArray();
            JsonObject json_clan = null;
            for (JsonElement e : results) {
                JsonObject o = e.getAsJsonObject();
                if (fallbackInput.equalsIgnoreCase(o.get(this.searchType).getAsString())) {
                    json_clan = o;
                    break;
                }
            }
            if (json_clan == null) {
                throw new ClanNotFoundException(this.gui);
            }
            String name = json_clan.get("name").getAsString();
            String tag = json_clan.get("abbreviation").getAsString();
            long ID = json_clan.get("id").getAsLong();
            int member_count = json_clan.get("member_count").getAsInt();
            ImageIcon emblem = new ImageIcon(new URL("http://worldoftanks.eu"+json_clan.get("clan_emblem_url").getAsString()));
            bestMatch = new PossibleClan(name, tag, ID, member_count, emblem);
        }

        // get provinces
        GetProvinces provinceWorker = new GetProvinces(bestMatch.getID(), this.gui);
        provinceWorker.execute();

        // get members
        //URL = new URL("http://worldoftanks."+gui.getServerRegion()+"/uc/clans/"+clanID+"/members/?type=table&offset=0&limit=100&order_by=name&search=&echo=1&id=clan_members_index");
        URL URL = new URL("http://worldoftanks."+gui.getServerRegion()+"/uc/clans/"+bestMatch.getID()+"/api/1.1/?source_token=Intellect_Soft-WoT_Mobile-unofficial_stats");
        URLConnection URLConnection = URL.openConnection();
        URLConnection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
        URLConnection.setRequestProperty("Accept-Language", "en-us;q=0.5,en;q=0.3");
        URLConnection.setRequestProperty("Accept-Encoding", "paco");
        URLConnection.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        URLConnection.setRequestProperty("Connection", "close");
        URLConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        // timeout after 15 seconds
        URLConnection.setConnectTimeout(15000);

        BufferedReader membersBufferedReader = null;
        StringBuilder members_data = new StringBuilder(10000);
        try {
            membersBufferedReader = new BufferedReader(new InputStreamReader(URLConnection.getInputStream(), "UTF8"));
            for (String line; (line = membersBufferedReader.readLine()) != null; members_data.append(line));
        } finally {
            if (membersBufferedReader != null) membersBufferedReader.close();
        }

        JsonParser jsonParser = new JsonParser();
        JsonObject members_json = jsonParser.parse(members_data.toString()).getAsJsonObject();

        if (!"ok".equalsIgnoreCase(members_json.get("status").getAsString())) {
            throw new ClanAPIException(members_json.get("status_code").getAsString(), this.gui);
        }

        JsonArray members = members_json.get("data").getAsJsonObject().get("members").getAsJsonArray();

        // default SwingWorker thread pool has 10 threads, but we want as much threads
        // as there are members in the clan because of network wait (-> latency):
        // before:  |members|/10 * latency
        //    now:  latency + some thread creation overhead
        ExecutorService threadPool = Executors.newFixedThreadPool(members.size());

        GetPlayerData[] workers = new GetPlayerData[members.size()];
        players = new ArrayList<Player>(members.size());
        for (int i = 0; i < members.size(); i++) {
            JsonObject member = members.get(i).getAsJsonObject();
            workers[i] = new GetPlayerData(member.get("account_id").getAsLong(), this.gui);
            threadPool.submit(workers[i]);
            //workers[i].execute();
        } // TODO: see if there's some sort of "worker pool" with a getAll() or getFirst()

        // In the meantime see if GetProvinces is ready
        this.provinces = provinceWorker.get();

        ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>(5000);
        long start = System.currentTimeMillis();
        for (GetPlayerData w : workers) {
            Player p = w.get();
            players.add(p);
            vehicles.addAll(p.getVehicles());
            // TODO: update progress bar
            // TODO: sort by tier already here ?!? (implementation details)
        }
        threadPool.shutdown();
        vehicles.trimToSize();
        System.out.printf("Vs:"+vehicles.size()+"\nOverall time: %dms%n", System.currentTimeMillis()-start);
        vehicles = Utils.sortVehiclesByTier(vehicles);
        vehicles = Utils.sortVehiclesByClass(vehicles);
        vehicles = Utils.sortVehiclesByNation(vehicles);

        if (players.size() > 20) { // some small logical optimizations
            double avg_top_eff=0D, avg_eff=0D, avg_top_wr=0D, avg_wr=0D;
            players = Utils.sortPlayersByEfficiency(players);
            for (int i = 0; i < players.size(); i++) {
                if (i == 20) avg_top_eff = avg_eff/20D;
                avg_eff += players.get(i).getEfficiency();
            }
            avg_eff /= players.size();

            players = Utils.sortPlayersByWinrate(players);
            for (int i = 0; i < players.size(); i++) {
                if (i == 20) avg_top_wr = avg_wr/20D;
                avg_wr += players.get(i).getAvg_wr();
            }
            avg_wr /= players.size();

            return new Clan(bestMatch.getName(), bestMatch.getClanTag(), bestMatch.getID(),
                players, vehicles, avg_wr, avg_top_wr, avg_eff, avg_top_eff, bestMatch.getEmblem());
        } else {
            double avg_eff=0D, avg_wr=0D; // top=all
            players = Utils.sortPlayersByEfficiency(players);
            for (int i = 0; i < players.size(); i++) {
                avg_eff += players.get(i).getEfficiency();
            }
            avg_eff /= players.size();

            players = Utils.sortPlayersByWinrate(players);
            for (int i = 0; i < players.size(); i++) {
                avg_wr += players.get(i).getAvg_wr();
            }
            avg_wr /= players.size();

            return new Clan(bestMatch.getName(), bestMatch.getClanTag(), bestMatch.getID(),
                players, vehicles, avg_wr, avg_wr, avg_eff, avg_eff, bestMatch.getEmblem());
        }
    }

    @Override // Executed in EDT !!
    protected void done(){
        try {
            Clan clan = get();
            this.gui.publishClanPlayers(clan);
            this.gui.publishClanProvinces(provinces);
        } catch (InterruptedException e) {
            Utils.handleException(e, this.gui);
        } catch (ExecutionException e) {
            Utils.handleException(e, this.gui);
        }
    }


}
