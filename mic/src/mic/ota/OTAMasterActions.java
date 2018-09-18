package mic.ota;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import mic.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class OTAMasterActions extends ArrayList<OTAMasterActions.OTAAction> {

    private static final Logger logger = LoggerFactory.getLogger(OTAMasterActions.class);

//    private static String REMOTE_URL = "https://raw.githubusercontent.com/Mu0n/XWVassalOTA/master/json/action_images.json";

    private static Map<String,OTAMasterActions.OTAAction> loadedData = null;

    public static void flushData()
    {
        mic.LoggerUtil.logEntry(logger,"flushData");
        loadedData = null;
        mic.LoggerUtil.logExit(logger,"flushData");
    }

    public Collection<OTAAction> getAllActions()
    {
        mic.LoggerUtil.logEntry(logger,"getAllActions");
        if(loadedData == null)
        {
            loadData();
        }
        mic.LoggerUtil.logExit(logger,"getAllActions");
       // Object[] actions = loadedData.values().toArray();
        return loadedData.values();

    }
/*
    public static OTAMasterActions.OTAAction getAction(String actionName) {
        mic.LoggerUtil.logEntry(logger,"getAction");
        if (loadedData == null) {
            loadData();
        }
        mic.LoggerUtil.logExit(logger,"getAction");
        return loadedData.get(actionName);
    }
*/
    private static void loadData()
    {
        mic.LoggerUtil.logEntry(logger,"loadData");
        // load from
        OTAMasterActions data = Util.loadRemoteJson(OTAContentsChecker.OTA_ACTIONS_JSON_URL, OTAMasterActions.class);
        loadedData = Maps.newHashMap();
        if (data == null) {
            Util.logToChat("Unable to load OTA Actions from the web");
        }else {


            for (OTAMasterActions.OTAAction action : data) {
                loadedData.put(action.getName(), action);
            }
        }
        mic.LoggerUtil.logExit(logger,"loadData");
    }

    public static class OTAAction {

        @JsonProperty("name")
        private String name;


        @JsonProperty("image")
        private String image;

        private boolean status;
        private boolean statusOTA;

        public void setStatus(boolean status)
        {
            this.status = status;
        }

        public boolean getStatus()
        {
            return status;
        }
        public boolean getStatusOTA() { return statusOTA; }
        public String getName() {
            return name;
        }
        public String getImage() {
            return image;
        }


        public void setStatusOTA(boolean statusOTA) {
            this.statusOTA = statusOTA;
        }
    }
}
