package modularwotclaninfo;

import com.google.gson.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

/**
 *
 * @author Yoyo117 (johnp)
 */
@SuppressWarnings({"ReturnOfCollectionOrArrayField", "UseOfObsoleteCollectionType"})
public class GetPossibleClans extends SwingWorker<Vector<PossibleClan>, Vector<PossibleClan>> {

    private final String searchTagName;
    private final GUI gui;

    private Vector<PossibleClan> possibleClans;

    protected GetPossibleClans(String searchTagName, GUI gui)
    {
        this.searchTagName = searchTagName;
        this.gui = gui;
    }

    @Override
    @SuppressWarnings("empty-statement")
    protected Vector<PossibleClan> doInBackground() throws Exception
    {
        long start = System.currentTimeMillis();
        URL URL = new URL("http://worldoftanks."+gui.getServerRegion()+"/uc/clans/api/1.1/?source_token=Intellect_Soft-WoT_Mobile-unofficial_stats&search="+this.searchTagName+"&offset=0&limit=10");
        URLConnection URLConnection = URL.openConnection();
        URLConnection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
        URLConnection.setRequestProperty("Accept-Language", "en-us;q=0.5,en;q=0.3");
        URLConnection.setRequestProperty("Accept-Encoding", "paco");
        URLConnection.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        URLConnection.setRequestProperty("Connection", "close");
        URLConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        // timeout after 5 seconds
        URLConnection.setConnectTimeout(5000);

        StringBuilder data = new StringBuilder(5000); // TODO: find good length
        try (BufferedReader clanBufferedReader = new BufferedReader(new InputStreamReader(URLConnection.getInputStream(), "UTF8"))) {
            for (String line; (line = clanBufferedReader.readLine()) != null; data.append(line));
        }System.out.println("length: "+data.length());

        JsonParser jsonParser = new JsonParser();
        JsonObject json = jsonParser.parse(data.toString()).getAsJsonObject();

        if (!"ok".equalsIgnoreCase(json.get("status").getAsString())) {
            throw new ClanAPIException(json.get("status_code").getAsString(), this.gui);
        }

        JsonArray results = json.get("data").getAsJsonObject().get("items").getAsJsonArray();
        possibleClans = new Vector<>(results.size());
        for (JsonElement e : results) {
            JsonObject o = e.getAsJsonObject();
            String name = o.get("name").getAsString();
            String tag = o.get("abbreviation").getAsString();
            long ID = o.get("id").getAsLong();
            int member_count = o.get("member_count").getAsInt();
            ImageIcon emblem = new ImageIcon(new URL("http://worldoftanks."+gui.getServerRegion()+o.get("clan_emblem_url").getAsString()));
            possibleClans.add(new PossibleClan(name, tag, ID, member_count, emblem));
        }

        //Collections.sort(possibleClans); // TODO: do we have to sort "better"?
        System.out.println(System.currentTimeMillis()-start);
        return possibleClans;
    }

    @Override // Executed in EDT !!
    protected void done(){
        try {
            Vector<PossibleClan> clans = get();
            this.gui.publishClans(searchTagName, clans);
        } catch (InterruptedException | ExecutionException e) {
            Utils.handleException(e, this.gui);
        }
    }
}
