package mic;


import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static mic.Util.*;


/**
 * Created by Mic on 2018-07-27.
 */
public class AutoSquadSpawn2e extends AbstractConfigurable {


    private List<JButton> spawnButtons = Lists.newArrayList();
    private List<JButton> selfReplButtonList = Lists.newArrayList();
    private List<JPanel> selfReplPanelList = Lists.newArrayList();


    private void spawnPiece(GamePiece piece, Point position, Map playerMap) {
        Command placeCommand = playerMap.placeOrMerge(piece, position);
        placeCommand.execute();
        GameModule.getGameModule().sendAndLog(placeCommand);
    }
    private void spawnForPlayer(int playerIndex) {

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
        frame.setPreferredSize(new Dimension(1200,1300));

        //Panel which will include a Combo box for selecting the source of the xwing-data to use
        final JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        JPanel sourcePanel = new JPanel();
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.X_AXIS));
        sourcePanel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
        JPanel sourceInfoPanel = new JPanel();
        sourceInfoPanel.setLayout(new BoxLayout(sourceInfoPanel, BoxLayout.Y_AXIS));
        sourceInfoPanel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);


        JLabel sourceExplanationLabel = new JLabel("This is a rough preliminary version of the 2nd edition squad autospawn window.");
        final JComboBox galacticEmpireComboBox = new JComboBox();
        JButton firstButton = new JButton("click me");
        firstButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent evt) {
            selfReplicate(rootPanel, frame);
        }
        });
        selfReplButtonList.add(new JButton("click me"));


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
        rootPanel.add(galacticEmpireComboBox);
        rootPanel.add(firstButton);
        JScrollPane jSP = new JScrollPane(rootPanel);
        frame.add(rootPanel);
        frame.add(jSP);

        String userInput = "";

        userInput = JOptionPane.showInputDialog(frame, rootPanel, "2.0 Squad AutoSpawn for player " + Integer.toString(playerInfo.getSide()), JOptionPane.PLAIN_MESSAGE);

        if("ok".equals(userInput)){
            XWS2Pilots alphaClassTest = XWS2Pilots.loadFromRemote();
            for(XWS2Pilots.Pilot2e pilot : alphaClassTest.getPilots()){
                logToChat("pilot " + pilot.getName());
            }
        }
    }

    private void selfReplicate(final JPanel rootPanel, final JFrame frame) {
        JButton another = new JButton("Click me");
        another.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent evt) {
            selfReplicate(rootPanel, frame);
        }
        });
        rootPanel.add(another);
        rootPanel.setSize(new Dimension(rootPanel.getWidth(), rootPanel.getHeight() + another.getHeight()));
        rootPanel.updateUI();
        frame.setSize(new Dimension(frame.getWidth(), frame.getHeight() + another.getHeight()));
        frame.validate();
        frame.invalidate();
        frame.pack();
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

    private Map getPlayerMap(int playerIndex) {
        for (Map loopMap : GameModule.getGameModule().getComponentsOf(Map.class)) {
            if (("Player " + Integer.toString(playerIndex)).equals(loopMap.getMapName())) {
                return loopMap;
            }
        }
        return null;
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
