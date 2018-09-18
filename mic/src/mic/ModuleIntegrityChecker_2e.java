package mic;


import mic.ota.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Mic on 24/08/18.
 *
 * Checks:
 * -if every xwing-data2 pilot or upgrade has a correspondant local image
 * -if every xwing-data2 ship has a ship token prepared, as well as dial
 */

public class ModuleIntegrityChecker_2e {
    private static final Logger logger = LoggerFactory.getLogger(ModuleIntegrityChecker_2e.class);

    public String testString = "";

    public ArrayList<OTAMasterUpgrades.OTAUpgrade> checkUpgrades(boolean onlyDetectOne, XWS2Upgrades allUpgrades)
    {
        mic.LoggerUtil.logEntry(logger,"checkUpgrades");
        // get list of upgrades from OTAMasterUpgrades
        OTAMasterUpgrades omu = new OTAMasterUpgrades();
        omu.flushData();
        Collection<OTAMasterUpgrades.OTAUpgrade> upgrades = omu.getAllUpgrades(2);


        ArrayList<OTAMasterUpgrades.OTAUpgrade> upgradeList = new ArrayList<OTAMasterUpgrades.OTAUpgrade>();
        Iterator<OTAMasterUpgrades.OTAUpgrade> i = upgrades.iterator();

        OTAMasterUpgrades.OTAUpgrade upgrade = null;
        while(i.hasNext())
        {
            upgrade = (OTAMasterUpgrades.OTAUpgrade)i.next();

            if(XWS2Upgrades.getSpecificUpgrade(upgrade.getXws(), allUpgrades) != null) {
                boolean exists = XWOTAUtils.imageExistsInModule(upgrade.getImage());
                upgrade.setStatus(exists);
                boolean existsInOTA = XWOTAUtils.imageExistsInOTA("upgrades",upgrade.getImage(),OTAContentsChecker.OTA_RAW_BRANCH_URL_2E);
                upgrade.setStatusOTA(existsInOTA);

                // either you have it already, or can get it. If you don't have it and can't get it, then fuggedaboutit
                boolean gonnaDL = false;
                if(exists == false && existsInOTA) gonnaDL = true;

                if(exists || gonnaDL) {
                    upgradeList.add(upgrade);

                    //speeds up the parsing right away if at least 1 object is detected as needing a download
                    if(onlyDetectOne && gonnaDL) return upgradeList;
                }
            }
        }
        mic.LoggerUtil.logExit(logger,"checkUpgrades");
        return upgradeList;
    }

    public ArrayList<OTAMasterConditions.OTACondition> checkConditions(boolean onlyDetectOne, List<XWS2Upgrades.Condition> allConditions)
    {
        mic.LoggerUtil.logEntry(logger,"checkConditions");
        // get list of conditions from OTAMasterUpgrades
        OTAMasterConditions omc = new OTAMasterConditions();
        omc.flushData();
        Collection<OTAMasterConditions.OTACondition> conditions = omc.getAllConditions(2);

        ArrayList<OTAMasterConditions.OTACondition> conditionList = new ArrayList<OTAMasterConditions.OTACondition>();
        Iterator<OTAMasterConditions.OTACondition> i = conditions.iterator();

        OTAMasterConditions.OTACondition condition = null;
        while(i.hasNext())
        {
            condition = (OTAMasterConditions.OTACondition)i.next();

            /*
            Util.logToChat("in modintCheck2e iterating through conditions, checking this entry " + condition.getImage());
            Util.logToChat("found the upgrade in xwing-data2? " + XWS2Upgrades.getSpecificConditionByXWS(condition.getXws(), allConditions));
            Util.logToChat("status " + condition.getStatus());
            Util.logToChat("image exists in OTA " + XWOTAUtils.imageExistsInOTA("conditions",condition.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL_2E));
*/
            if(XWS2Upgrades.getSpecificConditionByXWS(condition.getXws(), allConditions) != null)
            {
                boolean conditionExists = XWOTAUtils.imageExistsInModule(condition.getImage());
                boolean conditionTokenExists = XWOTAUtils.imageExistsInModule(condition.getTokenImage());

                condition.setStatus(conditionExists);
                condition.setTokenStatus(conditionTokenExists);

                boolean conditionExistsInOTA = XWOTAUtils.imageExistsInOTA("conditions",condition.getImage(),OTAContentsChecker.OTA_RAW_BRANCH_URL_2E);
                condition.setStatusOTA(conditionExistsInOTA);
                boolean conditionTokenExistsInOTA = XWOTAUtils.imageExistsInOTA("conditions", condition.getTokenImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL_2E);
                condition.setTokenStatusOTA(conditionTokenExists);

                // either you have it already, or can get it. If you don't have it and can't get it, then fuggedaboutit
                boolean gonnaDL = false;
               // if((conditionExists == false && conditionExistsInOTA) || (conditionTokenExists == false && conditionTokenExistsInOTA)) gonnaDL = true;
                if(!conditionExists && (conditionExistsInOTA || conditionTokenExistsInOTA))
                {
                    gonnaDL = true;
                }

                if((conditionExists ||  conditionExistsInOTA) ||
                        (conditionTokenExists || conditionTokenExistsInOTA))
                {
                                        conditionList.add(condition);
                                        if(onlyDetectOne && gonnaDL) return conditionList;
                }
            }
        }
        mic.LoggerUtil.logExit(logger,"checkConditions");
        return conditionList;
    }
/*
    public ArrayList<OTAMasterDialHides.OTADialHide> checkDialHides(boolean onlyDetectOne)
    {
        mic.LoggerUtil.logEntry(logger,"checkDialHides");
        OTAMasterDialHides omdh = new OTAMasterDialHides();
        omdh.flushData();
        Collection<OTAMasterDialHides.OTADialHide> dialHides = omdh.getAllDialHides();


        ArrayList<OTAMasterDialHides.OTADialHide> dialHideList = new ArrayList<OTAMasterDialHides.OTADialHide>();
        Iterator<OTAMasterDialHides.OTADialHide> i = dialHides.iterator();

        OTAMasterDialHides.OTADialHide dialHide = null;
        while(i.hasNext())
        {
            dialHide = i.next();

            // check to be sure this ship exists in xwing-data or dispatcher and the image exists in OTA
            // if it doesn't, just skip this entry from OTA
            if(MasterShipData.getShipData(dialHide.getXws()) != null)
            {

                dialHide.setStatus(XWOTAUtils.imageExistsInModule(dialHide.getImage()));
                if(dialHide.getStatus() ||  XWOTAUtils.imageExistsInOTA("dial",dialHide.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL))
                {
                    dialHideList.add(dialHide);
                    if(onlyDetectOne) return dialHideList;
                }

            }
        }
        mic.LoggerUtil.logExit(logger,"checkDialHides");
        return dialHideList;
    }
*/
/*
    public ArrayList<OTAMasterPilots.OTAPilot> checkPilots(boolean onlyDetectOne, List<XWS2Pilots> allShips)
    {
        // get list of pilots from OTAMasterPilots
        OTAMasterPilots omp = new OTAMasterPilots();
        omp.flushData();
        Collection<OTAMasterPilots.OTAPilot> OTAPilots = omp.getAllPilotImagesFromOTA(2);
        ArrayList<OTAMasterPilots.OTAPilot> pilotListToReturn = new ArrayList<OTAMasterPilots.OTAPilot>();

        for(XWS2Pilots xwd2ship : allShips)
        {
            for(XWS2Pilots.Pilot2e xwd2pilot : xwd2ship.getPilots())
            {
                String localFileNameConstructed = "P2e_" + Canonicalizer.getCleanedName(xwd2pilot.getFaction()) + "_"
                        + Canonicalizer.getCleanedName(xwd2ship.getName()) + "_"
                        + xwd2pilot.getXWS() + ".jpg";
                boolean existsLocally = XWOTAUtils.imageExistsInModule(localFileNameConstructed);

                //if it exists locally, do nothing, all is good, go to the next one if you want to fast track detect1missing
                if(existsLocally && onlyDetectOne) continue;

                //detectonlyone: if it doesn't exist locally, then, check if you can get it from the OTA
                OTAMasterPilots.OTAPilot otaPilot = OTAMasterPilots.getSpecificOTAPilot(xwd2pilot.getXWS());
                boolean existsInOTA = XWOTAUtils.imageExistsInOTA("pilots",otaPilot.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL_2E);

                otaPilot.setStatus(existsLocally);
                otaPilot.setStatusOTA(existsInOTA);

                if(onlyDetectOne){
                    if(!existsLocally && existsInOTA){
                        pilotListToReturn.add(otaPilot);
                        return pilotListToReturn;
                    }
                    else {
                        continue;
                    }
                }
                pilotListToReturn.add(otaPilot); //general check, add the otaPilot to the list to return, whatever its status
            }
        }
        return pilotListToReturn;
    }
*/


public ArrayList<OTAMasterPilots.OTAPilot> checkPilots(boolean onlyDetectOne, List<XWS2Pilots> allShips)
{
    mic.LoggerUtil.logEntry(logger,"checkPilots");
    // get list of pilots from OTAMasterPilots
    OTAMasterPilots omp = new OTAMasterPilots();
    omp.flushData();
    Collection<OTAMasterPilots.OTAPilot> pilots = omp.getAllPilotImagesFromOTA(2);

    ArrayList<OTAMasterPilots.OTAPilot> pilotListToReturn = new ArrayList<OTAMasterPilots.OTAPilot>();
    Iterator<OTAMasterPilots.OTAPilot> i = pilots.iterator();

    while(i.hasNext())
    {
        OTAMasterPilots.OTAPilot pilot = (OTAMasterPilots.OTAPilot)i.next();

        if(XWS2Pilots.getSpecificPilot(pilot.getPilotXws(), allShips)!=null) {

            boolean exists = XWOTAUtils.imageExistsInModule(pilot.getImage());
            pilot.setStatus(exists);
            boolean existsInOTA = XWOTAUtils.imageExistsInOTA("pilots",pilot.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL_2E);
            pilot.setStatusOTA(existsInOTA);

            // either you have it already, or can get it. If you don't have it and can't get it, then fuggedaboutit
            boolean gonnaDL = false;
            if(exists == false && existsInOTA) gonnaDL = true;

            if(exists || gonnaDL) {
                pilotListToReturn.add(pilot);
                if(onlyDetectOne && gonnaDL) return pilotListToReturn;
            }
        }
    }
    mic.LoggerUtil.logExit(logger,"checkPilots");
    return pilotListToReturn;
}

public ArrayList<OTAMasterShips.OTAShip> checkShips(boolean onlyDetectOne, List<XWS2Pilots> allShips)
    {
        mic.LoggerUtil.logEntry(logger,"checkShips");
        // get list of ships from OTAMasterShips
        OTAMasterShips oms = new OTAMasterShips();
        oms.flushData();
        Collection<OTAMasterShips.OTAShip> ships = oms.getAllShips(2);

        ArrayList<OTAMasterShips.OTAShip> shipList = new ArrayList<OTAMasterShips.OTAShip>();
        Iterator<OTAMasterShips.OTAShip> i = ships.iterator();

        while(i.hasNext())
        {
            OTAMasterShips.OTAShip ship = (OTAMasterShips.OTAShip)i.next();

            if(XWS2Pilots.getSpecificShipFromShipXWS(ship.getXws(), allShips)!=null)
            {

                boolean exists = XWOTAUtils.imageExistsInModule(ship.getImage());
                ship.setStatus(exists);
                boolean existsInOTA = XWOTAUtils.imageExistsInOTA("ships",ship.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL_2E);
                ship.setStatusOTA(existsInOTA);

                // either you have it already, or can get it. If you don't have it and can't get it, then fuggedaboutit
                boolean gonnaDL = false;
                if(exists == false && existsInOTA) gonnaDL = true;

                if(exists || gonnaDL) {
                    shipList.add(ship);
                    if(onlyDetectOne && gonnaDL) return shipList;
                }
            }
        }
        mic.LoggerUtil.logExit(logger,"checkShips");
        return shipList;
    }

public ArrayList<OTAShipBase> checkShipBases(boolean onlyDetectOne, List<XWS2Pilots> allShips)
    {
        mic.LoggerUtil.logEntry(logger,"checkShipBases");
        OTAMasterShips oms = new OTAMasterShips();
        oms.flushData();
        Collection<OTAMasterShips.OTAShip> ships = oms.getAllShips(2);

        ArrayList<OTAShipBase> shipList = new ArrayList<OTAShipBase>();
        Iterator<OTAMasterShips.OTAShip> i1 = ships.iterator();

        while(i1.hasNext())
        {
            OTAMasterShips.OTAShip ship = (OTAMasterShips.OTAShip)i1.next();

            if(XWS2Pilots.getSpecificShipFromShipXWS(ship.getXws(), allShips) != null)
            {
                // check to see which factions to generate this ship base for
                List<String> factions = ship.getFactions();

                Iterator<String> i2 = factions.iterator();
                while (i2.hasNext()) {

                    String factionName = i2.next();
                    String shipBaseImageName = XWOTAUtils.buildShipBaseImageName(factionName, ship.getXws(), ship.getIdentifier(), 2);

                    OTAShipBase shipBase = new OTAShipBase();
                    shipBase.setFaction(factionName);
                    shipBase.setIdentifier(ship.getIdentifier());
                    shipBase.setShipBaseImageName(shipBaseImageName);
                    shipBase.setshipImageName(ship.getImage());
                    shipBase.setShipName(XWS2Pilots.getSpecificShipFromShipXWS(ship.getXws(), allShips).getName());
                    shipBase.setShipXws(ship.getXws());
                    boolean exists = XWOTAUtils.imageExistsInModule(shipBaseImageName);
                    shipBase.setStatus(exists);
                    boolean existsInOTA = XWOTAUtils.imageExistsInOTA("ships",ship.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL_2E);
                    shipBase.setStatusOTA(existsInOTA);

                    // either you have it already, or can get it. If you don't have it and can't get it, then fuggedaboutit
                    //the ship base can't exist in OTA, instead, we check the isolated ship gfx a few lines above
                    boolean gonnaDL = false;
                    if(exists == false && existsInOTA) gonnaDL = true;

                    if(exists || gonnaDL) {
                        shipList.add(shipBase);
                        if(onlyDetectOne && gonnaDL) return shipList;
                    }
                }
            }
        }
        mic.LoggerUtil.logExit(logger,"checkShipBases");
        return shipList;
    }
/*
    public ArrayList<OTADialMask> checkDialMasks(boolean onlyDetectOne)
    {
        mic.LoggerUtil.logEntry(logger,"checkDialMasks");
        OTAMasterDialHides omdh = new OTAMasterDialHides();
        omdh.flushData();
        Collection<OTAMasterDialHides.OTADialHide> dialHides = omdh.getAllDialHides();

        MasterShipData msd = new MasterShipData();
        msd.loadData();

        ArrayList<OTADialMask> dialMaskList = new ArrayList<OTADialMask>();
        Iterator<OTAMasterDialHides.OTADialHide> dialHideIterator = dialHides.iterator();

        while(dialHideIterator.hasNext())
        {
            OTAMasterDialHides.OTADialHide dialHide = dialHideIterator.next();

            // check to be sure this ship exists in xwing-data or dispatcher
            // if it doesn't, just skip this entry from OTA
            if(MasterShipData.getShipData(dialHide.getXws()) != null)
            {
                // check to see which factions to generate this dial mask
                List<String> factions = dialHide.getFactions();

                Iterator<String> factionIterator = factions.iterator();
                while (factionIterator.hasNext()) {

                    String factionName = factionIterator.next();

                    String dialMaskImageName = XWOTAUtils.buildDialMaskImageName(factionName, dialHide.getXws());


                    OTADialMask dialMask = new OTADialMask();
                    dialMask.setFaction(factionName);
                    dialMask.setDialHideImageName(dialHide.getImage());
                    dialMask.setDialMaskImageName(dialMaskImageName);
                    dialMask.setShipName(MasterShipData.getShipData(dialHide.getXws()).getName());
                    dialMask.setShipXws(dialHide.getXws());
                    dialMask.setStatus(XWOTAUtils.imageExistsInModule(dialMaskImageName));

                    if(dialMask.getStatus() || XWOTAUtils.imageExistsInOTA("dial",dialHide.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL)) {
                        dialMaskList.add(dialMask);
                        if(onlyDetectOne) return dialMaskList;
                    }


                }
            }


        }
        mic.LoggerUtil.logExit(logger,"checkDialMasks");
        return dialMaskList;

    }
*/
/*
    public ArrayList<OTAMasterActions.OTAAction> checkActions(boolean onlyDetectOne)
    {
        mic.LoggerUtil.logEntry(logger,"checkActions");
        boolean addToList = false;
        // get list of Actions from OTAMasterActions
        OTAMasterActions oma = new OTAMasterActions();
        oma.flushData();
        Collection<OTAMasterActions.OTAAction> actions = oma.getAllActions();

        ArrayList<OTAMasterActions.OTAAction> actionList = new ArrayList<OTAMasterActions.OTAAction>();
        Iterator<OTAMasterActions.OTAAction> i = actions.iterator();
        OTAMasterActions.OTAAction action = null;
        while(i.hasNext())
        {

            action = i.next();
            action.setStatus(XWOTAUtils.imageExistsInModule(action.getImage()));

            if(action.getStatus() || XWOTAUtils.imageExistsInOTA("actions",action.getImage(), OTAContentsChecker.OTA_RAW_BRANCH_URL)){
                actionList.add(action);
                if(onlyDetectOne) return actionList;
            }


        }


        mic.LoggerUtil.logExit(logger,"checkActions");
        return actionList;
    }
*/
/*
    public void checkAll(){
        mic.LoggerUtil.logEntry(logger,"checkAll");
        MasterPilotData mpd = new MasterPilotData();
        mpd.loadData();

        for(MasterPilotData.PilotData pd : mpd){
            testString+=pd.getFaction();
        }
        mic.LoggerUtil.logExit(logger,"checkAll");
    }
*/
    public String getTestString(){
        return testString;
    }

    public class PilotResults {
        private String faction;
        private String ship;
        private String pilot;
        private String image;
        private boolean existsLocally;

        public PilotResults()
        {
            super();
        }

        public void setFaction(String faction)
        {
            this.faction = faction;
        }
        public void setShip(String ship)
        {
            this.ship = ship;
        }
        public void setPilot(String pilot)
        {
            this.pilot = pilot;
        }
        public void setImage(String image)
        {
            this.image = image;
        }
        public void setExistsLocally(boolean existsLocally)
        {
            this.existsLocally = existsLocally;
        }
        public String getFaction()
        {
            return faction;
        }
        public String getShip()
        {
            return ship;
        }
        public String getPilot()
        {
            return pilot;
        }
        public String getImage()
        {
            return image;
        }
        public boolean existsLocally()
        {
            return existsLocally;
        }

    }
}
