package mic.ota;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mic.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
public class OTAMasterShips extends ArrayList<OTAMasterShips.OTAShip> {
    private static final Logger logger = LoggerFactory.getLogger(OTAMasterShips.class);
    // private static String REMOTE_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/ship_images.json";

    private static Map<String, OTAMasterShips.OTAShip> loadedData = null;
    public static void flushData()
    {
        loadedData = null;
    }
    public Collection<OTAShip> getAllShips(int edition)
    {
        mic.LoggerUtil.logEntry(logger,"getAllShips");
        if(loadedData == null)
        {
            loadData(edition);
        }
       // Object[] actions = loadedData.values().toArray();
        mic.LoggerUtil.logExit(logger,"getAllShips");
        return loadedData.values();

    }
    public static Map<String, OTAMasterShips.OTAShip> getLoadedData(int edition){
        mic.LoggerUtil.logEntry(logger,"getLoadedData");
        if(loadedData == null)
        {
            loadData(edition);
        }
        mic.LoggerUtil.logExit(logger,"getLoadedData");
        return loadedData; }

    public static OTAMasterShips.OTAShip getShip(String shipxws, String identifier, int edition) {
        mic.LoggerUtil.logEntry(logger,"getShip");
        if (loadedData == null) {
            loadData(edition);
        }
        String shipKey = shipxws+"_"+identifier;
        mic.LoggerUtil.logExit(logger,"getShip");
        return loadedData.get(shipKey);
    }

    private static void loadData(int edition) {
        mic.LoggerUtil.logEntry(logger,"loadData");
        // load from
        OTAMasterShips data = new OTAMasterShips();
        if(edition == 1) data = Util.loadRemoteJson(OTAContentsChecker.OTA_SHIPS_JSON_URL, OTAMasterShips.class);
        else if(edition == 2) {
            data = Util.loadRemoteJson(OTAContentsChecker.OTA_SHIPS_JSON_URL_2E, OTAMasterShips.class);
        }
        loadedData = Maps.newHashMap();
        if (data == null) {
            Util.logToChat("Unable to load OTA pilots from the web");
        }else {

            // <faction>_<shipxws>_<pilotxws>
            for (OTAMasterShips.OTAShip ship : data) {
                String shipKey = ship.getXws()+"_"+ship.getIdentifier();
                loadedData.put(shipKey, ship);
            }
        }
        mic.LoggerUtil.logExit(logger,"loadData");
    }

    public static class OTAShip {

        @JsonProperty("xws")
        private String xws;

        @JsonProperty("identifier")
        private String identifier;

        @JsonProperty("image")
        private String image;

        @JsonProperty("faction")
        private List<String> factions = Lists.newArrayList();

        private boolean status;
        private boolean statusOTA;


        public String getXws() {
            return xws;
        }
        public String getImage() {
            return image;
        }
        public String getIdentifier() {return identifier;}
        public List<String> getFactions() {
            return this.factions;
        }

        public void setStatus(boolean status)
        {
            this.status = status;
        }
        public boolean getStatus()
        {
            return status;
        }
        public boolean getStatusOTA() { return statusOTA; }

        public void setStatusOTA(boolean existsOTA) {
            this.statusOTA = existsOTA;
        }
    }
}
