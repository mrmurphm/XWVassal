package mic;


import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import mic.ota.XWOTAUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;

import static mic.Util.*;


/**
 * Created by Mic on 2018-07-27.
 *
 * This creates a small Spawn 2.0 button in every player's window and can only be activated by the corresponding signed player, like always
 * New to this edition - a crude, data populated squad builder where you can add/clone ship+pilot combinations and add an arbitrarily large amount of upgrades for each, with buttons for deletion for the 2+ instances of each
 * (e.g. first ship+pilot does not have a delete button, but subsequent ship+pilots do. Same goes for upgrades, per ship+pilot)
 *
 * TO DO:
 * 1) make it generate a XWS2 squad json when the UI selections have been made in the comboboxes
 * 2) make it spawn a squad (starting with placeholder items first, next with cards)
 * 3) offer to save a XWS2 squad locally to disk for now, since the early 2.0 builders might not have squad saving management
 */
public class AutoSquadSpawn2e extends AbstractConfigurable {


    private VassalXWSPieceLoader2e slotLoader = new VassalXWSPieceLoader2e();

    private static java.util.Map<String, String> xwingdata2ToYasb2 = ImmutableMap.<String, String>builder()
            .put("Rebel Alliance","rebelalliance")
            .put("Galactic Empire","galacticempire")
            .put("Scum and Villainy","scumandvillainy")
            .put("First Order","firstorder")
            .put("Resistance","resistance")
            .put("Galactic Republic","galacticrepublic")
            .put("CIS","cis")
            .build();

//keepsake for this whole class' behavior inside the player window - they must be kept track so they can be removed safely later
    private List<JButton> spawnButtons = Lists.newArrayList();



    private void spawnPiece(GamePiece piece, Point position, Map playerMap) {
        Command placeCommand = playerMap.placeOrMerge(piece, position);
        placeCommand.execute();
        GameModule.getGameModule().sendAndLog(placeCommand);
    }

    private Command spawnPieceCommand(GamePiece piece, Point position, Map playerMap) {
        return playerMap.placeOrMerge(piece, position);
    }


    //Main interface via a Java Swing JFrame. The complexity has outgrown an InputDialog - we now use ActionListener on the JComboBox and JButton to react to the user commands
    private void spawnForPlayer(final int playerIndex) {
        final List<XWS2Pilots> allShips = XWS2Pilots.loadFromRemote();
        final XWS2Upgrades allUpgrades = XWS2Upgrades.loadFromRemote();
        final List<XWS2Upgrades.Condition> allConditions = XWS2Upgrades.loadConditionsFromRemote();

/*
        logToChat("*** -- pilot_images.json");
        logToChat("[");
        int i=0, j=0;
        for(XWS2Pilots ship : allShips){
            for(XWS2Pilots.Pilot2e pilot : ship.getPilots()){
                String shipStr = Canonicalizer.getCleanedName(XWS2Pilots.getSpecificShipFromPilotXWS2(pilot.getXWS(),allShips).getName());
                String pilotStr = pilot.getXWS();
                String factionStr = Canonicalizer.getCleanedName(XWS2Pilots.getSpecificShipFromPilotXWS2(pilot.getXWS(),allShips).getFaction());
                String imageStr = "P2e_" + factionStr + "_" + shipStr + "_" + pilotStr + ".jpg";
                logToChat("{");
                logToChat("\"shipxws\":\""+shipStr+"\",");
                logToChat("\"pilotxws\":\""+pilotStr+"\",");
                logToChat("\"faction\":\""+factionStr+"\",");
                logToChat("\"image\":\""+imageStr+"\"");
                if(i == (allShips.size()-1) && j == (ship.getPilots().size()-1)) logToChat("}");
                else logToChat("},");
                j++;
            }
            i++;
            j=0;
        }
        logToChat("]");
        logToChat("*** -- pilot_images.json end");


        logToChat("*** -- ship_images.json");
        logToChat("[");
        i=0;
        List<String> processedShipXWS = Lists.newArrayList();
        HashMap<String, List<String>> shipFactionMap = new HashMap<String, List<String>>();
        for(XWS2Pilots ship : allShips) {
            //checks if this ship already has an entry in the shipFaction map, if so, add the faction in the value of the map
            if (shipFactionMap.containsKey(Canonicalizer.getCleanedName(ship.getName()))) {
                List<String> tempFactionsToAppend = shipFactionMap.get(Canonicalizer.getCleanedName(ship.getName()));
                tempFactionsToAppend.add(Canonicalizer.getCleanedName(ship.getFaction()));
                shipFactionMap.put(Canonicalizer.getCleanedName(ship.getName()), tempFactionsToAppend);
            } else {
                List<String> tempFactionsToAppend = Lists.newArrayList();
                tempFactionsToAppend.add(Canonicalizer.getCleanedName(ship.getFaction()));
                shipFactionMap.put(Canonicalizer.getCleanedName(ship.getName()), tempFactionsToAppend);
            }
        }
        for(String shipXWS : shipFactionMap.keySet())
        {
            String imageStr = "S2e_"+shipXWS+".png";
            String factionsStr = "";
            int k=0;
            for(String faction : shipFactionMap.get(shipXWS))
            {
                factionsStr += faction;
                if(k != shipFactionMap.get(shipXWS).size()-1) factionsStr += "\",\"";
                k++;
            }
            logToChat("{");
            logToChat("\"xws\":\""+shipXWS+"\",");
            logToChat("\"image\":\""+imageStr+"\",");
            logToChat("\"identifier\": \"standard\",");
            logToChat("\"faction\":[\""+factionsStr+"\"]");
            if(i == (shipFactionMap.keySet().size()-1)) logToChat("}");
            else logToChat("},");
            i++;
        }
        logToChat("]");
        logToChat("*** -- ship_images.json end");
*/

/*
        logToChat("*** -- upgrade_images.json");
        logToChat("[");
        int i=0;
        for(XWS2Upgrades.OneUpgrade upgrade : allUpgrades.getUpgrades()) {
            String slotStr = upgrade.getSides().get(0).getType();
            String upgradeStr = upgrade.getXws();
            String imageStr = "U2e_" + upgradeStr + ".jpg";

            logToChat("{");
            logToChat("\"slot\":\"" + slotStr + "\",");
            logToChat("\"xws\":\""+upgradeStr+"\",");
            logToChat("\"image\":\""+imageStr+"\"");
            if(i == allUpgrades.getUpgrades().size()-1) logToChat("}");
            else logToChat("},");
            i++;
        }
        logToChat("]");
        logToChat("*** -- upgrade_images.json end");


        logToChat("*** -- condition_images.json");
        logToChat("[");
        i=0;
        for(XWS2Upgrades.Condition condition : allConditions) {

            String xwsStr = condition.getXws();
            String imageStr = "C2e_" + xwsStr + ".jpg";
            String tokenStr = "CT2e_" + xwsStr + ".png";

            logToChat("{");
            logToChat("\"image\":\"" + imageStr + "\",");
            logToChat("\"tokenimage\":\""+tokenStr+"\",");
            logToChat("\"xws\":\""+xwsStr+"\"");
            if(i == allConditions.size()-1) logToChat("}");
            else logToChat("},");
            i++;
        }

        logToChat("]");
        logToChat("*** -- condition_images.json end");
*/
        final List<String> factionsWanted = Lists.newArrayList();

        Map playerMap = getPlayerMap(playerIndex);
        if (playerMap == null) {
            logToChat("Unexpected error, couldn't find map for player side " + playerIndex);
            return;
        }

        XWPlayerInfo playerInfo = getCurrentPlayer();
        if (playerInfo.getSide() != playerIndex) {
            JOptionPane.showMessageDialog(playerMap.getView(), "Cannot spawn squads for other players");
            return;
        }

        final JFrame frame = new JFrame();
        final JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JCheckBox empireCheck = new JCheckBox("Empire");
        empireCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        empireCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(empireCheck.isSelected()) factionsWanted.add("Galactic Empire");
                else factionsWanted.remove("Galactic Empire");
            }
        });

        final JCheckBox allianceCheck = new JCheckBox("Alliance");
        allianceCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        allianceCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(allianceCheck.isSelected()) factionsWanted.add("Rebel Alliance");
                else factionsWanted.remove("Rebel Alliance");
            }
        });

        final JCheckBox scumCheck = new JCheckBox("Scum and Villainy");
        scumCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        scumCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(scumCheck.isSelected()) factionsWanted.add("Scum and Villainy");
                else factionsWanted.remove("Scum and Villainy");
            }
        });

        final JCheckBox firstorderCheck = new JCheckBox("First Order");
        firstorderCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        firstorderCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(firstorderCheck.isSelected()) factionsWanted.add("First Order");
                else factionsWanted.remove("First Order");
            }
        });

        final JCheckBox resistanceCheck = new JCheckBox("Resistance");
        resistanceCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        resistanceCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(resistanceCheck.isSelected()) factionsWanted.add("Resistance");
                else factionsWanted.remove("Resistance");
            }
        });
        final JCheckBox republicCheck = new JCheckBox("Galactic Republic");
        republicCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        republicCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(republicCheck.isSelected()) factionsWanted.add("Galactic Republic");
                else factionsWanted.remove("Galactic Republic");
            }
        });

        final JCheckBox cisCheck = new JCheckBox("CIS");
        cisCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        cisCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cisCheck.isSelected()) factionsWanted.add("CIS");
                else factionsWanted.remove("CIS");
            }
        });



        JButton builderButton = new JButton("Internal Squad Builder");
        builderButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        builderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(factionsWanted.isEmpty()) {
                    JFrame warnFrame = new JFrame();
                    JOptionPane.showMessageDialog(warnFrame, "Check at least 1 faction before opening the internal builder");
                    return;
                }
                internalSquadBuilder(playerIndex, factionsWanted, allShips, allUpgrades, allConditions);
            }
        });

        JPanel builderPanel = new JPanel();
        builderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        builderPanel.setLayout(new BoxLayout(builderPanel,BoxLayout.X_AXIS));
        builderPanel.add(empireCheck);
        builderPanel.add(allianceCheck);
        builderPanel.add(scumCheck);
        builderPanel.add(firstorderCheck);
        builderPanel.add(resistanceCheck);
        builderPanel.add(republicCheck);
        builderPanel.add(cisCheck);
        builderPanel.add(builderButton);

        final JTextArea entryArea = new JTextArea("Enter a valid XWS squad here.");
        entryArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        entryArea.setPreferredSize(new Dimension(850,150));
        entryArea.setMaximumSize(new Dimension(850,150));
        entryArea.setLineWrap(true);
        entryArea.setAutoscrolls(true);

        JLabel method1Label = new JLabel("Method 1 for spawning a list - click on a suggested web builder, export to XWS and paste it here:");
        method1Label.setFont(new Font("Dialog", Font.PLAIN, 18));
        JButton clearTextAreaButton = new JButton("Clear");
        clearTextAreaButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        clearTextAreaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                entryArea.setText("");
                entryArea.invalidate();
            }
        });
        JButton xwsSpawnButton = new JButton("Spawn Squad from XWS");
        xwsSpawnButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        xwsSpawnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                XWSList2e xwsList = loadListFromRawJson(entryArea.getText());
                try{
                    validateList(xwsList, allShips);
                } catch (Exception exc) {
                    logToChat("Unable to load raw JSON list '%s': %s", entryArea.getText(), exc.toString());
                    return;
                }
                if (xwsList == null || xwsList.getPilots() == null || xwsList.getPilots().size() == 0) {
                    logToChat("raw JSON list has no detected pilots in it.");
                    return;
                }
                DealWithXWSList(xwsList, playerIndex, allShips, allUpgrades, allConditions);
                frame.dispose();
            }
        });
        JLabel method2Label = new JLabel("Method 2 for spawning a list - use the internal squad builder (allows illegal, cross-faction lists if needed)");
        method2Label.setFont(new Font("Dialog", Font.PLAIN, 18));
        method2Label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel method1ButtonsPanel = new JPanel();
        method1ButtonsPanel.setLayout(new BoxLayout(method1ButtonsPanel, BoxLayout.X_AXIS));
        method1ButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        method1ButtonsPanel.add(clearTextAreaButton);
        method1ButtonsPanel.add(xwsSpawnButton);


        JPanel method1LinksPanel = new JPanel();
        method1LinksPanel.setLayout(new BoxLayout(method1LinksPanel, BoxLayout.Y_AXIS));
        method1LinksPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Suggested2eBuilders suggestions = new Suggested2eBuilders();
        suggestions.loadData();
        for(Suggested2eBuilders.Builder builder : suggestions.getSuggestions()){
            try{
                JLabel linkDescLabel = new JLabel("       " + builder.getDescription());
                linkDescLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                SwingLink newLink = new SwingLink(builder.getName(),builder.getURL());
                newLink.setAlignmentX(Component.LEFT_ALIGNMENT);

                method1LinksPanel.add(newLink);
                method1LinksPanel.add(linkDescLabel);

            }catch (Exception e){
                logToChat("spawn2e line 362 can't make the swing link");
            }
        }


        JPanel method1Panel = new JPanel();
        method1Panel.setLayout(new BoxLayout(method1Panel, BoxLayout.Y_AXIS));
        method1Panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        method1Panel.add(method1Label);
        method1Panel.add(method1LinksPanel);
        method1Panel.add(entryArea);
        method1Panel.add(method1ButtonsPanel);


        JPanel method2Panel = new JPanel();
        method2Panel.setLayout(new BoxLayout(method2Panel, BoxLayout.Y_AXIS));
        method2Panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        method2Panel.add(method2Label);
        method2Panel.add(builderPanel);

        rootPanel.add(method1Panel);
        rootPanel.add(Box.createRigidArea(new Dimension(0,8)));
        rootPanel.add(new JSeparator());
        rootPanel.add(Box.createRigidArea(new Dimension(0,8)));
        rootPanel.add(method2Panel);

        frame.add(rootPanel);
        frame.setSize(900,500);
        frame.setTitle("2.0 Squad Autospawn for player " + Integer.toString(playerIndex));
        frame.pack();
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();


    }

    private void internalSquadBuilder(final int playerIndex, final List<String> factionsWanted, final List<XWS2Pilots> allShips, final XWS2Upgrades allUpgrades, final List<XWS2Upgrades.Condition> allConditions){


        final JFrame frame = new JFrame();
        //Panel which will include a Combo box for selecting the source of the xwing-data to use
        final JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        JPanel sourcePanel = new JPanel();
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.X_AXIS));
        JPanel sourceInfoPanel = new JPanel();
        sourceInfoPanel.setLayout(new BoxLayout(sourceInfoPanel, BoxLayout.Y_AXIS));

        JPanel oneShipPanel = new JPanel();

        JLabel sourceExplanationLabel = new JLabel("You can build a list in this popup window. Its validity will not be checked. Illegal upgrades may be chosen and cross faction is possible.");
        final JTextArea entryArea = new JTextArea("Enter a valid XWS squad here.");
        entryArea.setPreferredSize(new Dimension(850,150));
        entryArea.setMaximumSize(new Dimension(850,150));
        entryArea.setLineWrap(true);
        entryArea.setAutoscrolls(true);

        final JComboBox ShipComboList = new JComboBox();
        ShipComboList.setToolTipText("Select a ship.");
        ShipComboList.addItem("Select a ship.");
        final JComboBox PilotComboList = new JComboBox();
        PilotComboList.setToolTipText("Select a pilot");

        populateShipComboBox(ShipComboList, factionsWanted, allShips);

        ShipComboList.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                PilotComboList.removeAllItems();
                for(XWS2Pilots ship : allShips){
                    String[] parts = (ShipComboList.getSelectedItem()).toString().split("_");

                    if(ship.getFaction().equals(parts[0]) && ship.getName().equals(parts[1]))
                    {
                        for(XWS2Pilots.Pilot2e pilot : ship.getPilots())
                        {
                            PilotComboList.addItem(pilot.getName() + "_" + pilot.getXWS());
                        }
                    }
                }
            }
        });
        final JPanel upgradeWholeBoxForShipPanel = new JPanel();
        upgradeWholeBoxForShipPanel.setLayout(new BoxLayout(upgradeWholeBoxForShipPanel, BoxLayout.Y_AXIS));
        addUpgradeEntry(true, upgradeWholeBoxForShipPanel, frame, allUpgrades);

        JButton addShipButton = new JButton("Add Ship");
        addShipButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent evt) {
            copyOrCloneShipButtonBehavior(factionsWanted, false, ShipComboList, PilotComboList, rootPanel, frame, allShips, allUpgrades);
        }
        });
        JButton cloneShipButton = new JButton("Clone Ship");
        cloneShipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyOrCloneShipButtonBehavior(factionsWanted, true, ShipComboList, PilotComboList, rootPanel, frame, allShips, allUpgrades);
            }
        });


        sourcePanel.add(sourceExplanationLabel);

        //make it editable further down the line once it's properly tested
        //aComboBox.setEditable(true);
        JPanel explanationPanel = new JPanel();
        explanationPanel.setLayout(new BoxLayout(explanationPanel, BoxLayout.Y_AXIS));
        explanationPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rootPanel.add(sourcePanel);
        rootPanel.add(sourceInfoPanel);
        rootPanel.add(Box.createRigidArea(new Dimension(0,8)));
        rootPanel.add(new JSeparator());
        rootPanel.add(Box.createRigidArea(new Dimension(0,8)));
        rootPanel.add(explanationPanel);

        rootPanel.add(entryArea);

        JButton createXWS2Button = new JButton("Export to XWS");
        createXWS2Button.setToolTipText("XWS is a community-defined text format used by squad builders (web, apps, etc.) in order to exchange squads.");
        createXWS2Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String theFaction = "mixed";
                if (factionsWanted.size() == 1 && xwingdata2ToYasb2.containsKey(factionsWanted.get(0))) {
                    theFaction = xwingdata2ToYasb2.get(factionsWanted.get(0));
                }
                generateXWS(rootPanel, entryArea, theFaction);
            }
        });
        JButton validateButton = new JButton("Spawn List");
        validateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                XWSList2e xwsList = loadListFromRawJson(entryArea.getText());
                try{
                validateList(xwsList, allShips);
                } catch (Exception exc) {
                    logToChat("Unable to load raw JSON list '%s': %s", entryArea.getText(), exc.toString());
                    return;
                }
                if (xwsList == null || xwsList.getPilots() == null || xwsList.getPilots().size() == 0) {
                    logToChat("raw JSON list has no detected pilots in it.");
                    return;
                }
                DealWithXWSList(xwsList, playerIndex, allShips, allUpgrades, allConditions);
                frame.dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        buttonPanel.add(createXWS2Button);
        buttonPanel.add(validateButton);

        rootPanel.add(buttonPanel);

        oneShipPanel.add(ShipComboList);
        oneShipPanel.add(PilotComboList);
        oneShipPanel.add(addShipButton);
        oneShipPanel.add(cloneShipButton);

        rootPanel.add(oneShipPanel);
        rootPanel.add(upgradeWholeBoxForShipPanel);

        frame.add(rootPanel);
        frame.setSize(900,500);
        frame.setTitle("2.0 Squad Autospawn for player " + Integer.toString(playerIndex));
        frame.pack();
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();
    }


    private Map getPlayerMap(int playerIndex) {
        for (Map loopMap : GameModule.getGameModule().getComponentsOf(Map.class)) {
            if (("Player " + Integer.toString(playerIndex)).equals(loopMap.getMapName())) {
                return loopMap;
            }
        }
        return null;
    }

    private void DealWithXWSList(XWSList2e xwsList, int playerIndex, List<XWS2Pilots> allPilots, XWS2Upgrades allUpgrades, List<XWS2Upgrades.Condition> allConditions) {

        XWOTAUtils.checkOnlineOrder66();
        Map playerMap = getPlayerMap(playerIndex);
        if (playerMap == null) {
            logToChat("Unexpected error, couldn't find map for player side " + playerIndex);
            return;
        }

        // If the list includes a yv666 with Hound's Tooth upgrade or modified YT-1300 with escape craft, add the necessary stuff
        //xwsList = handleHoundsToothIshThings(xwsList);

        VassalXWSListPieces2e pieces = slotLoader.loadListFromXWS(xwsList, allPilots, allUpgrades, allConditions);
        List<GamePiece> shipBases = Lists.newArrayList();

        //reference constant positions or displacements
        Point pilotStartPosition = new Point(150, 659);
        Point dialstartPosition = new Point(300, 100);
        Point tlStartPosition = new Point(300, 290);
        Point configStartPosition = new Point(200, 220);
        Point tokensStartPosition = new Point(300, 220);
        int optDisplacePerConfig = 250;
        int upgradeYDisplace = 10;
        int upgradeComeBackLeft = 170;
        int nbOfConfigs = 0;
        int nbOfUpgrades = 0;

        int lastUpgradeFudge = 50;
        int shipBaseY = 110;

        //track keepers
        int totalTokenWidth = 0;
        int totalPilotHeight = 0;
        int totalDialsWidth = 0;
        int totalTLWidth = 0;

        //related to the pesky condition locations
        Point conditionStartPosition = new Point(0, 0); // gonna be calculated later
        int pilotWidthForCondition = 0;
        int totalConditionAndUpgradeDisplacementForCondition = 0;


        //receptors of relative coordinates; refreshed at any time a cluster of those is needed
        List<Point> chargeLocations = Lists.newArrayList(); // list of coordinates to place charge tokens above their pilot or upgrade; if both charge and force are present, gotta play nice together
        List<Point> forceChargeLocations = Lists.newArrayList(); // ditto for force charges

        // get the charge and force charge token slots
        PieceSlot chargePieceSlot = null;
        PieceSlot forceChargePieceSlot = null;

        List<PieceSlot> allSlots = GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class);

        for(PieceSlot pieceSlot : allSlots )
        {
            String slotName = pieceSlot.getConfigureName();
            if(slotName.equals("Charge2e") && chargePieceSlot == null){
                chargePieceSlot = pieceSlot;
                continue;
            }
            if(slotName.equals("Force2e") && forceChargePieceSlot == null){
                forceChargePieceSlot = pieceSlot;
                continue;
            }
        }

        Command entireSpawnCommand = logToChatCommand("");

        for (VassalXWSPilotPieces2e ship : pieces.getShips()) {

            // --- UPGRADE CARD SCANS ----
            //figure out how many extra force charges a ship must have
            //figure out if there are config cards and count 'em
            int extraForceFromUpgrade = 0;
            int howManyConfigUpgradeCards = 0;
            int countHowManyNonConfigurationUpgrades = 0;
            for(VassalXWSPilotPieces2e.Upgrade up : ship.getUpgrades()){
                try{
                    extraForceFromUpgrade += up.getUpgradeData().sides.get(0).getForce().getValue();
                }
                catch(Exception e){
                }
                if(up.getUpgradeData().sides.get(0).getType().equals("Configuration")) howManyConfigUpgradeCards++;
                else countHowManyNonConfigurationUpgrades++;
            }
            // ======================================================
            // Generate the ship base pieces
            // ======================================================
            GamePiece shipPiece = GamePieceGenerator2e.generateShip(ship, extraForceFromUpgrade);
            shipBases.add(shipPiece);

            // ======================================================
            // Generate the Pilot Pieces
            // ======================================================

            //verifies if there's a configuration upgrade card - if so, shift the pilot card start position to the right
            boolean hasConfiguration = false;
            for(VassalXWSPilotPieces2e.Upgrade up : ship.getUpgrades())
            {
             if(up.getUpgradeData().sides.get(0).getType().equals("Configuration")) hasConfiguration = true;
            }

            GamePiece pilotPiece = GamePieceGenerator2e.generatePilot(ship);

            int pilotWidth = (int) pilotPiece.boundingBox().getWidth();
            int pilotHeight = (int) pilotPiece.boundingBox().getHeight();
            pilotWidthForCondition += pilotWidth;

            int pilotPosX = (int) pilotStartPosition.getX()+howManyConfigUpgradeCards*optDisplacePerConfig;
            int pilotPosY = (int) pilotStartPosition.getY() + totalPilotHeight;
            entireSpawnCommand.append(spawnPieceCommand(pilotPiece, new Point(pilotPosX,pilotPosY),playerMap));

            totalPilotHeight += pilotHeight + 50;

            if (ship.getPilotData().getForceData().getValue() > 0) {
                int force = ship.getPilotData().getForceData().getValue();
                for(int k = 0; k < force + extraForceFromUpgrade; k++) {
                    GamePiece forcePiece = newPiece(forceChargePieceSlot);
                    entireSpawnCommand.append(spawnPieceCommand(forcePiece, new Point(pilotPosX + - (int)(pilotPiece.boundingBox().width/2.0) + (int)(forcePiece.boundingBox().width/2.0) + k * forcePiece.boundingBox().width,
                            pilotPosY - (int) pilotPiece.boundingBox().height/2), playerMap));
                }
            }

            // ======================================================
            // Generate the Dial
            // ======================================================

            GamePiece dialPiece = GamePieceGenerator2e.generateDial(ship);

            int dialWidth = 0;
            try {
                dialWidth = (int) dialPiece.boundingBox().getWidth();
            } catch (Exception e)
            {
                logToChat("Couldn't find the dial infor for this ship: " + ship.getShipData().getName());
                continue;
            }
            entireSpawnCommand.append(spawnPieceCommand(dialPiece, new Point((int) dialstartPosition.getX() + totalDialsWidth, (int) dialstartPosition.getY()), playerMap));
            totalDialsWidth += dialWidth;


            // ======================================================
            // Generate the Upgrades
            // ======================================================

            //This is where the first upgrade starts.  Current value puts half of the upgrade under the pilot, assuming
            // the pilot is at the far left of the screen

            //int totalUpgradeWidth = 251*ship.getUpgrades().size();
            int totalUpgradeWidth = 260;
            int savedTotalUpgradeWidth = 0;

            VassalXWSPilotPieces2e.Upgrade upgrade = new VassalXWSPilotPieces2e.Upgrade("",null);
            if(ship.getUpgrades().size()!=0) {
                //do configuration cards


                    //find the config cards among the upgrade cards
                    int configsDoneSoFar = 0;
                    for (int j = 0; j < howManyConfigUpgradeCards + countHowManyNonConfigurationUpgrades; j++) {

                        try {
                            upgrade = ship.getUpgrades().get(j);
                        } catch (Exception e) {
                        }

                        if (upgrade == null) break;
                        GamePiece upgradePiece = GamePieceGenerator2e.generateUpgrade(upgrade);

                        if (upgrade.getUpgradeData().sides.get(0).getType().equals("Configuration")) {
                            savedTotalUpgradeWidth += upgradePiece.boundingBox().width;
                            totalConditionAndUpgradeDisplacementForCondition += upgradePiece.boundingBox().width - upgradeComeBackLeft;

                            int placeUpgradeX = configStartPosition.x + configsDoneSoFar * (upgradePiece.boundingBox().width - upgradeComeBackLeft);
                            int placeUpgradeY = configStartPosition.y + configsDoneSoFar * upgradeYDisplace + totalPilotHeight;
                            entireSpawnCommand.append(spawnPieceCommand(upgradePiece, new Point(placeUpgradeX, placeUpgradeY), playerMap));
                            XWS2Upgrades.Charge testIfHasCharge = upgrade.getUpgradeData().getSides().get(0).getCharges();
                            if (testIfHasCharge != null)
                            {
                                for(int chargeIncr = 0; chargeIncr < upgrade.getUpgradeData().getSides().get(0).getCharges().getValue(); chargeIncr++){
                                    chargeLocations.add(new Point(
                                            placeUpgradeX + chargeIncr * 60,
                                            placeUpgradeY - upgradePiece.getShape().getBounds().height/2 - 10
                                    ));
                                }
                            }
                            configsDoneSoFar++;
                        }
                    }
                    nbOfConfigs = configsDoneSoFar;

                //do non-configuration cards
                for (int i = ship.getUpgrades().size()-1; i > -1; i--) {
                    //for (VassalXWSPilotPieces2e.Upgrade upgrade : ship.getUpgrades()) {

                    try {
                        upgrade = ship.getUpgrades().get(i);
                    } catch (Exception e) {
                    }

                    if (upgrade == null) break;
                    if(upgrade.getUpgradeData().sides.get(0).getType().equals("Configuration")) continue;
                    GamePiece upgradePiece = GamePieceGenerator2e.generateUpgrade(upgrade);

                    savedTotalUpgradeWidth += upgradePiece.boundingBox().width;
                    totalUpgradeWidth = i * (upgradePiece.boundingBox().width - upgradeComeBackLeft);
                    totalConditionAndUpgradeDisplacementForCondition += upgradePiece.boundingBox().width - upgradeComeBackLeft;

                    int placeUpgradeX = (int) pilotStartPosition.getX() + pilotWidth + totalUpgradeWidth - upgradeComeBackLeft + lastUpgradeFudge;
                    int placeUpgradeY = (int) configStartPosition.getY() + totalPilotHeight + i*upgradeYDisplace;
                    entireSpawnCommand.append(spawnPieceCommand(upgradePiece, new Point(placeUpgradeX, placeUpgradeY), playerMap));
                    XWS2Upgrades.Charge testIfHasCharge = upgrade.getUpgradeData().getSides().get(0).getCharges();
                    nbOfUpgrades++;
                    if (testIfHasCharge != null)
                    {
                        for(int chargeIncr = 0; chargeIncr < upgrade.getUpgradeData().getSides().get(0).getCharges().getValue(); chargeIncr++){
                            chargeLocations.add(new Point(
                                    placeUpgradeX + chargeIncr * 60,
                                    placeUpgradeY - upgradePiece.getShape().getBounds().height/2 - 10
                            ));
                        }
                    }

                    totalUpgradeWidth -= (upgradePiece.boundingBox().width - upgradeComeBackLeft);
                }
            }
            totalUpgradeWidth = savedTotalUpgradeWidth;

            // ======================================================
            //Generate the Conditions
            // ======================================================
            int extraXFromConditions = 0;


            for (VassalXWSPilotPieces2e.Condition condition: ship.getConditions()) {
                GamePiece conditionPiece = GamePieceGenerator2e.generateCondition(condition);

                conditionStartPosition.x = pilotStartPosition.x + totalConditionAndUpgradeDisplacementForCondition + conditionPiece.boundingBox().width + upgradeComeBackLeft;
                conditionStartPosition.y = pilotStartPosition.y + totalPilotHeight - pilotHeight - 50;


                entireSpawnCommand.append(spawnPieceCommand(conditionPiece, new Point(
                                conditionStartPosition.x + extraXFromConditions,
                                conditionStartPosition.y),
                        playerMap));


                // spawn the condition token
                GamePiece conditionTokenPiece = GamePieceGenerator2e.generateConditionToken(condition);
                entireSpawnCommand.append(spawnPieceCommand(conditionTokenPiece, new Point(
                                conditionStartPosition.x + extraXFromConditions,
                                conditionStartPosition.y - conditionTokenPiece.boundingBox().height/2 - conditionPiece.boundingBox().height/2 + 15),
                        playerMap));

                extraXFromConditions += conditionPiece.boundingBox().getWidth();

            } //loop to next condition
            totalConditionAndUpgradeDisplacementForCondition = 0;



            // ======================================================
            // Add all of the appropriate tokens
            // ======================================================

            //do charges
            for(Point p: chargeLocations){
                GamePiece chargePiece = newPiece(chargePieceSlot);
                entireSpawnCommand.append(spawnPieceCommand(chargePiece, p, playerMap));
            }

            for (GamePiece token : ship.getTokensForDisplay()) {

                PieceSlot pieceSlot = new PieceSlot(token);

                if ("Lock".equals(pieceSlot.getConfigureName())) {//if a target lock token, place elsewhere
                    entireSpawnCommand.append(spawnPieceCommand(token, new Point(
                                    (int) tokensStartPosition.getX() + totalTLWidth,
                                    (int) tlStartPosition.getY()),
                            playerMap));
                    totalTLWidth += token.boundingBox().getWidth();
                }else {

                    entireSpawnCommand.append(spawnPieceCommand(token, new Point(
                                    (int) tokensStartPosition.getX() + totalTokenWidth,
                                    (int) tokensStartPosition.getY()),
                            playerMap));
                    totalTokenWidth += token.boundingBox().getWidth();
                }
            }// loop to next token*/

        }//loop ships

        int shipBaseX = (int) dialstartPosition.getX() + totalDialsWidth - 30;
        for (GamePiece piece : shipBases) {
            int halfBase = (int) (piece.getShape().getBounds2D().getWidth() / 2.0);
            entireSpawnCommand.append(spawnPieceCommand(piece, new Point(shipBaseX + halfBase, shipBaseY), playerMap));
            shipBaseX += piece.getShape().getBounds2D().getWidth() + 10.0;
        }


        String listName = xwsList.getName();
        logToChat("The '" + "Base 2.0 Game" + "' game mode was used to spawn a list%s loaded from %s",
                listName != null ? " '" + listName + "'" : "", xwsList.getXwsSource());

        if(entireSpawnCommand != null) {
            entireSpawnCommand.execute();
            GameModule.getGameModule().sendAndLog(entireSpawnCommand);
        }




    }

    private void generateXWS(JPanel rootPanel, JTextArea entryArea, String factionString) {

        StringBuilder outputSB = new StringBuilder();
        outputSB.append("{\"description\":\"\",\"faction\":\""+factionString+"\",\"name\":\"New Squadron\",\"pilots\":[{");
        //String output = "{\"description\":\"\",\"faction\":\""+factionString+"\",\"name\":\"New Squadron\",\"pilots\":[{";

        List<ReadShipInfo> stuffToXWS = Lists.newArrayList();

        //start at rootpanel. The ship+pilot is going to be under a JPanel
        //their upgrades are going to be under a JPanel/JPanel
        ReadShipInfo currentShip = new ReadShipInfo();

        boolean aShipWasDetected = false;

        for(Component c : rootPanel.getComponents())
        {
            //a JPanel is found - will probably find a ship
            if(c.getClass().toString().equals(JPanel.class.toString())){
                int comboBoxSeenCount = 0;
                int upgradeSeenCount = 0;
                //check for a ship under rootPanel/JPanel hopefully oneShipPanel, or wholeUpgradePanel
                for(Component d : ((JPanel)c).getComponents()){
                    //found a combobox. might be ship or pilot.
                    if(d.getClass().toString().equals(JComboBox.class.toString()))
                    {
                        //the ship hasn't been selected, so get out of this rootPanel/JPanel loop
                        if(((JComboBox) d).getSelectedItem().equals("Select a ship.")) break;
                        else {
                            if(comboBoxSeenCount==0) {
                                if(aShipWasDetected){
                                    //looped to new ship
                                    stuffToXWS.add(currentShip);
                                    currentShip = new ReadShipInfo();
                                    aShipWasDetected = false;
                                }
                                comboBoxSeenCount = 1;
                                String[] parts = (((JComboBox) d).getSelectedItem()).toString().split("_");
                                if(parts.length > 1) currentShip.setTypeName(Canonicalizer.getCleanedName(parts[1]));
                                else currentShip.setTypeName(parts[0]);
                            }
                            else if(comboBoxSeenCount== 1)
                            {
                                comboBoxSeenCount = 2;
                                String[] parts = (((JComboBox) d).getSelectedItem()).toString().split("_");
                                currentShip.setShipName(parts[0]);
                                if(parts.length > 1) currentShip.setShipPilotXWS2(parts[1]);
                                else currentShip.setShipPilotXWS2(parts[0]);
                                aShipWasDetected = true;
                                comboBoxSeenCount=0;
                            }
                        }
                    }
                    //getting to rootPanel/JPanela series of upgrades wholeUpgradePanel/anUpgradePanel
                    else if(d.getClass().toString().equals(JPanel.class.toString()))
                    {
                        String detectedType = "";
                        String detectedUpg = "";

                        for(Component e : ((JPanel) d).getComponents()){
                            if(e.getClass().toString().equals(JComboBox.class.toString()))
                            {
                                if(((JComboBox) e).getSelectedItem().equals("Select Upgrade Type.")) break;
                                else {
                                    if(upgradeSeenCount==0){
                                        detectedType = (((JComboBox) e).getSelectedItem()).toString();
                                        upgradeSeenCount=1;
                                    }else if(upgradeSeenCount==1){
                                        detectedUpg = (((JComboBox) e).getSelectedItem()).toString();
                                        upgradeSeenCount=2;
                                    }
                                    if(upgradeSeenCount==2){
                                        currentShip.addUpgrade(detectedType, detectedUpg);
                                        upgradeSeenCount=0;
                                    }
                                }
                            }
                        } //end of the for loop of a series of upgrades, maybe found some, maybe didn't.
                        //gets only 1 upgrade per ship HERE
                    } //end if of a series of upgrades, maybe found some, maybe didn't.
                    //makes too many ship entries HERE

                }//end of for loop
            } //end of rootPanel/JPanel (of a ship)
        }//end of rootPanel

//adds the last ship
        if(aShipWasDetected){
            //last ship to add at the end of everything
            stuffToXWS.add(currentShip);
        }

        StringBuilder pilotStringSB;
        StringBuilder xws2StringSB;
        StringBuilder upgradeTypeStringSB;


        for(int i=0; i< stuffToXWS.size(); i++){ //parse all ship/pilot entries
            //String shipString ="\"ship\":\"" + stuffToXWS.get(i).getShipType() + "\",";

            pilotStringSB = new StringBuilder();
            xws2StringSB = new StringBuilder();
            pilotStringSB.append("\"name\":\"").append(Canonicalizer.getCleanedName(stuffToXWS.get(i).getShipName())).append("\",");
            //String pilotString = "\"name\":\"" + Canonicalizer.getCleanedName(stuffToXWS.get(i).getShipName()) + "\",";
            xws2StringSB.append("\"id\":\"").append(stuffToXWS.get(i).getShipPilotXWS2()).append("\",");
            //String xws2String = "\"id\":\"" + stuffToXWS.get(i).getShipPilotXWS2() + "\",";

            outputSB.append(pilotStringSB).append(xws2StringSB).append("\"upgrades\":{");
            //String upgradesStartString = "\"upgrades\":{";
            //output+= pilotString + xws2String + upgradesStartString;

            for(int j=0; j<stuffToXWS.get(i).getUpgradeBins().size(); j++) //parse all upgrade types
            {
                upgradeTypeStringSB = new StringBuilder();
                upgradeTypeStringSB.append("\"").append(stuffToXWS.get(i).getUpgradeBins().get(j).getType()).append("\":[");

                //String upgradeTypeString = "\""+ stuffToXWS.get(i).getUpgradeBins().get(j).getType() +"\":[";
                outputSB.append(upgradeTypeStringSB);
                //output += upgradeTypeString;
                for(int k=0; k<stuffToXWS.get(i).getUpgradeBins().get(j).getUpgrades().size(); k++){ // parse upgrades within a type

                    outputSB.append("\"").append(stuffToXWS.get(i).getUpgradeBins().get(j).getUpgrades().get(k)).append("\"");
                   // String upgradeString = "\""+stuffToXWS.get(i).getUpgradeBins().get(j).getUpgrades().get(k)+"\"";
                    //output+=upgradeString;
                    if(k!=stuffToXWS.get(i).getUpgradeBins().get(j).getUpgrades().size()-1)
                    {
                        outputSB.append(",");
                        //output+=","; //not the last upgrade in an upg type
                    }
                }
                outputSB.append("]");
                //output+="]"; //marks the end of an upgrade type
                if(j!=stuffToXWS.get(i).getUpgradeBins().size()-1)
                {
                    outputSB.append(",");
                    //output+=","; //not the last upgrade type in the upgrades
                }
            }

            outputSB.append("}");
          //  String upgradesEndString = "}"; //marks the end of upgrades
          //  output += upgradesEndString;

            outputSB.append("}");
           // output += "}"; //marks the end of a ship/pilot entry
            if(i!=stuffToXWS.size()-1)
            {
                outputSB.append(",{");
                //output+= ",{"; //not the last ship/pilot entry
            }
        }
        outputSB.append("],\"vendor\":{\"yasb\":{\"builder\":\"Internal Vassal Squad Builder\",\"builder_url\":\"none\",\"link\":\"none\"}},\"version\":\"2.0.0\"}");
        //output+="],\"vendor\":{\"yasb\":{\"builder\":\"Internal Vassal Squad Builder\",\"builder_url\":\"none\",\"link\":\"none\"}},\"version\":\"2.0.0\"}";

        entryArea.setText(outputSB.toString());
        //entryArea.setText(output);
    }

    //Helper method that will populate the leftmost combobox for an upgrade - lists the types of upgrades (should be fairly stable)
    private void populateUpgradeTypes(JComboBox upgradeTypesComboBox, XWS2Upgrades allUpgrades) {
        List<String> upgradeTypesSoFar = Lists.newArrayList();
        for(XWS2Upgrades.OneUpgrade up : allUpgrades.getUpgrades())
        {
            for(XWS2Upgrades.side side : up.getSides())
            {
                if(upgradeTypesSoFar.contains(side.getType()) == false) upgradeTypesSoFar.add(side.getType());
            }
        }
        for(String s : upgradeTypesSoFar)
        {
            upgradeTypesComboBox.addItem(s);
        }
    }


    //helper method that adds a panel containing 2 new comboboxes (upgrade type + upgrade within that type), the [x] delete button. Should be used as well by the Add/clone Pilot method
    private void addUpgradeEntry(boolean wantAddButtonAsFirstUpgrade, final JPanel theUpgPanelHolderVert, final JFrame frame, final XWS2Upgrades allUpgrades) {
        final JPanel anUpgradePanel = new JPanel();
        anUpgradePanel.setLayout(new BoxLayout(anUpgradePanel, BoxLayout.X_AXIS));
        anUpgradePanel.add(Box.createRigidArea(new Dimension(25,0)));
        final JComboBox upgradeTypesComboBox = new JComboBox();
        upgradeTypesComboBox.addItem("Select Upgrade Type.");
        populateUpgradeTypes(upgradeTypesComboBox, allUpgrades);

        final JComboBox upgradesComboBox = new JComboBox();

        upgradeTypesComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                upgradesComboBox.removeAllItems();
                for(XWS2Upgrades.OneUpgrade ups : allUpgrades.getUpgrades()){
                    if(upgradeTypesComboBox.getSelectedItem().equals(ups.getSides().get(0).getType())) upgradesComboBox.addItem(ups.getXws());
                }
            }
        });

        JButton addUpg = new JButton("Add");
        addUpg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUpgradeEntry(false, theUpgPanelHolderVert, frame, allUpgrades);
            }
        });
        JButton remUpg = new JButton("[X]");
        remUpg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(Component c : theUpgPanelHolderVert.getComponents())
                {
                    if((Component)anUpgradePanel == c) theUpgPanelHolderVert.remove(c);
                }
                frame.setSize(frame.getWidth(), frame.getHeight() - anUpgradePanel.getHeight());
                frame.pack();
                theUpgPanelHolderVert.invalidate();
                frame.invalidate();
            }
        });
        anUpgradePanel.add(upgradeTypesComboBox);
        anUpgradePanel.add(upgradesComboBox);
        if(wantAddButtonAsFirstUpgrade == false) anUpgradePanel.add(remUpg);
        else anUpgradePanel.add(addUpg);

        theUpgPanelHolderVert.add(anUpgradePanel);
        frame.setSize(new Dimension(frame.getWidth(), frame.getHeight() + anUpgradePanel.getHeight()));
        frame.validate();
        frame.invalidate();
        frame.pack();
    }


    private void populateShipComboBox(JComboBox shipComboBox, final List<String> factionsWanted, List<XWS2Pilots> allShips)
    {
        for(XWS2Pilots ship : allShips)
        {
            //if(ship.getFaction().equals(factionsWanted)) shipComboBox.addItem(ship.getName());
            if(factionsWanted.contains(ship.getFaction())) shipComboBox.addItem(ship.getFaction() + "_" + ship.getName());
        }
    }
    //Reacts to both "Add Ship" and "Clone Ship" buttons
    private void copyOrCloneShipButtonBehavior(final List<String> factionsWanted, final boolean wantCloning, final JComboBox toCopyShip,
                                               final JComboBox toCopyPilot, final JPanel rootPanel,
                                               final JFrame frame, final List<XWS2Pilots> allShips, final XWS2Upgrades allUpgrades) {
        if(toCopyShip.getSelectedItem().toString().equals("Select a ship.")) {
            JFrame warnFrame = new JFrame();
            JOptionPane.showMessageDialog(warnFrame, "Please select a ship and a pilot before cloning.");
            return;
        }
        final JComboBox shipComboList = new JComboBox();
        shipComboList.setToolTipText("Select a ship.");
        shipComboList.addItem("Select a ship.");

        populateShipComboBox(shipComboList, factionsWanted, allShips);
        if(wantCloning==true){
            shipComboList.setSelectedItem(toCopyShip.getSelectedItem());
        }

        final JComboBox pilotComboList = new JComboBox();
        pilotComboList.setToolTipText("Select a pilot");
        pilotComboList.addItem("");
        for(XWS2Pilots ship : allShips)
        {
            String[] parts = (shipComboList.getSelectedItem()).toString().split("_");
            if(ship.getFaction().equals(parts[0]) && ship.getName().equals(parts[1]))
            {
                for(XWS2Pilots.Pilot2e pilot : ship.getPilots())
                {
                    pilotComboList.addItem(pilot.getName() + "_" + pilot.getXWS());
                }
            }
        }
        if(wantCloning==true){
            pilotComboList.setSelectedItem(toCopyPilot.getSelectedItem());
        }

        shipComboList.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                pilotComboList.removeAllItems();
                for(XWS2Pilots ship : allShips){
                    String[] parts = (shipComboList.getSelectedItem()).toString().split("_");

                    if(ship.getFaction().equals(parts[0]) && ship.getName().equals(parts[1]))
                    {
                        for(XWS2Pilots.Pilot2e pilot : ship.getPilots())
                        {
                            pilotComboList.addItem(pilot.getName() + "_" + pilot.getXWS());
                        }
                    }
                }

            }
        });


        final JPanel anotherShipPanel = new JPanel();

        anotherShipPanel.add(shipComboList);
        anotherShipPanel.add(pilotComboList);

        final JButton cloneButton = new JButton("Clone Ship");
        final JButton removeButton = new JButton("Remove Ship");


        final JPanel anotherUpgradeWholeThing = new JPanel();
        anotherUpgradeWholeThing.setLayout(new BoxLayout(anotherUpgradeWholeThing, BoxLayout.Y_AXIS));
        boolean wantFirstUpgradeEntryWithAddButton = true;
        addUpgradeEntry(wantFirstUpgradeEntryWithAddButton, anotherUpgradeWholeThing,frame,allUpgrades);


        cloneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyOrCloneShipButtonBehavior(factionsWanted,true, shipComboList,pilotComboList, rootPanel, frame, allShips, allUpgrades);
            }
        });
        removeButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent evt) {
            for(Component c : rootPanel.getComponents())
            {
                if((Component)anotherShipPanel == c) rootPanel.remove(c);
                if((Component)anotherUpgradeWholeThing == c) rootPanel.remove(c);
            }
            frame.setSize(frame.getWidth(), frame.getHeight() - anotherShipPanel.getHeight());
            frame.pack();
            rootPanel.invalidate();
            frame.invalidate();
        }
        });

        anotherShipPanel.add(cloneButton);
        anotherShipPanel.add(removeButton);

        rootPanel.add(anotherShipPanel);
        rootPanel.add(anotherUpgradeWholeThing);
        rootPanel.setSize(new Dimension(rootPanel.getWidth(), rootPanel.getHeight() + anotherUpgradeWholeThing.getHeight()));
        anotherUpgradeWholeThing.updateUI();
        rootPanel.updateUI();
        frame.setSize(new Dimension(frame.getWidth(), frame.getHeight() + anotherUpgradeWholeThing.getHeight()));
        frame.validate();
        frame.invalidate();
        frame.pack();
    }



    public static class ReadShipInfo{
        private String shipName=""; //name of pilot; for menus and UIs
        private String shipType=""; //name of ship
        private String shipPilotXWS2=""; //unique xws2 key for pilot

        private List<ReadUpgradesInfo> upgradeBins = Lists.newArrayList();

        public ReadShipInfo(){}

        public String getShipName() { return shipName; }
        public String getShipType() { return shipType; }
        public String getShipPilotXWS2() { return shipPilotXWS2; }

        public List<ReadUpgradesInfo> getUpgradeBins() { return upgradeBins; }

        public void setShipName(String name) { this.shipName = name; }
        public void setTypeName(String type) { this.shipType = type; }
        public void setShipPilotXWS2(String xws2)
        {
            this.shipPilotXWS2 = xws2;
        }
        public void addUpgrade(String type, String upgrade){
            boolean notFound = true;
            for(ReadUpgradesInfo bin : upgradeBins)
            {
                if(bin.getType().equals(type)){
                    bin.addUpgrade(upgrade);
                    notFound = false;
                }
            }
            if(notFound == true){
                upgradeBins.add(new ReadUpgradesInfo(type, upgrade));
            }
        }
    }

    public static class ReadUpgradesInfo{
        private String type="";
        private List<String> upgrades = Lists.newArrayList();

        public ReadUpgradesInfo(){}
        public ReadUpgradesInfo(String type, String upgrade){
            this.type = type;
            this.upgrades.add(upgrade);
        }

        public String getType(){ return type; }
        public List<String> getUpgrades() { return upgrades; }

        public void setType(String type) { this.type = type; }
        public void addUpgrade(String upgrade) {upgrades.add(upgrade); }
        public void removeUpgrade(String upgrade) { upgrades.remove(upgrade); }
    }


    private void validateList(XWSList2e list, final List<XWS2Pilots> allShips) throws XWSpawnException
    {
        boolean error = false;
        XWSpawnException exception = new XWSpawnException();

        XWSList2e newList = null;
        HashMap<String,String> skippedPilots = new HashMap<String,String>();
        for (XWSList2e.XWSPilot pilot : list.getPilots())
        {
            //Get the stuff from the XWS formatted json
            String pilotXWS2 = pilot.getXws();

            //TO USE when xwing-data2 has them
            // String pilotXWS2 = pilot.getXws2();
            //Check the unique pilot xws2 key in all ships
            boolean signalError = true; // assume you won't find the pilot

            //loop every ship
            for(XWS2Pilots shipFromData : allShips)
            {
                if(shipFromData.getSpecificPilot(pilotXWS2, allShips) == null) continue;
                else {
                    signalError = false; // invalidate the problem in ship finding
                    }
            }
//might not have found the pilot; so, signalError will remain true here
            if(signalError == true)
            {
                error = true;
                // skippedPilots.put(pilot.getXws(),"X");
                skippedPilots.put(pilotXWS2,"X");
            }


        }

        if(error) logToChat("ERROR DETECTED");
        if(error)
        {
            // create a new list, removing the pilots/ships that aren't valid
            newList = new XWSList2e();
            newList.setDescription(list.getDescription());
            newList.setFaction(list.getFaction());
            newList.setName(list.getName());
            newList.setObstacles(list.getObstacles());
            newList.setPoints(list.getPoints());
            newList.setVendor(list.getVendor());
            newList.setVersion(list.getVersion());
            newList.setXwsSource(list.getXwsSource());

            for (XWSList2e.XWSPilot pilot : list.getPilots())
            {
                if(skippedPilots.get(pilot.getXws()) == null)
                {
                    newList.addPilot(pilot);
                }
            }
            exception.setNewList2e(newList);
            // throw the exception
            throw exception;
        }
    }

    private XWSList2e loadListFromRawJson(String userInput) {
        try {
            XWSList2e list = getMapper().readValue(userInput, XWSList2e.class);
            list.setXwsSource("JSON");
            return list;
        } catch (Exception e) {
            logToChat("Unable to load raw JSON list '%s': %s", userInput, e.toString());
            return null;
        }
    }

    public void addTo(Buildable parent) {

        for (int i = 1; i <= 8; i++) {
            final int playerId = i;

            JButton b = new JButton("2.0 Spawn");
            b.setAlignmentY(0.0F);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    spawnForPlayer(playerId);
                }
            });
            spawnButtons.add(b);

            getPlayerMap(i).getToolBar().add(b);
        }
    }

    public void removeFrom(Buildable parent) {
        for (int i = 1; i <= 8; i++) {
            getPlayerMap(i).getToolBar().remove(spawnButtons.get(i - 1));
        }
    }

    // <editor-fold desc="unused vassal hooks">
    @Override
    public String[] getAttributeNames() {
        return new String[]{};
    }

    @Override
    public void setAttribute(String s, Object o) {
        // No-op
    }

    @Override
    public String[] getAttributeDescriptions() {
        return new String[]{};
    }

    @Override
    public Class[] getAttributeTypes() {
        return new Class[]{};
    }

    @Override
    public String getAttributeValueString(String key) {
        return "";
    }

    public Class[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public HelpFile getHelpFile() {
        return null;
    }
    // </editor-fold>

}
