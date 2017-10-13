package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;

import javax.swing.*;
import java.awt.*;

public class ShipUI extends Decorator implements EditablePiece {

    public static final String ID = "ship-ui;";
    private KeyStroke uiCommand = KeyStroke.getKeyStroke('M', java.awt.event.InputEvent.ALT_MASK);
    private KeyCommand[] commands;
    public ShipUI() {
        this(null);
    }

    public ShipUI(GamePiece piece) {
        setInner(piece);
    }

    @Override
    public void mySetState(String s) {

    }

    @Override
    public String myGetState() {
        return "";
    }

    @Override
    protected KeyCommand[] myGetKeyCommands() {
        if (commands == null) {
            commands = new KeyCommand[]{new KeyCommand("Ship UI", uiCommand, this)};
        }
        return commands;
    }

    @Override
    public Command myKeyEvent(KeyStroke keyStroke) {
        return null;
    }


    @Override
    public Command keyEvent(KeyStroke stroke) {

        if(uiCommand.equals(stroke))
        {
            Util.logToChat("Triggered");
        }
        /*
        String hotKey = HotKeyConfigurer.getString(stroke);

        // check to see if the this code needs to respond to the event
        if(hotKey.equals("ALT M") && stroke.isOnKeyRelease()){



            // get the text from the damage card
            GamePiece damageCard = getInner();

            // get the location of the damage card
            Point aPoint = damageCard.getPosition();

            // get a new crit token
            GamePiece critToken = newPiece(findPieceSlotByName("Crit"));

            // check to see if the damage card is flipped
            if(damageCard.getProperty("isFlipped") != null &&((String) damageCard.getProperty("isFlipped")).equals("1") ) {

                // If the card is flipped, change the text on the crit token
                critToken.setProperty("critID", damageCard.getLocalizedName());
            }

            // spawn the new token on the board
            spawnPiece(critToken, aPoint, damageCard.getMap());



            Command c = null;
            if (magnifyCommand.equals(stroke)) {
                GamePiece target = Decorator.getOutermost(this);
                String oldState = target.getState();
                // Prompt user for magnification factor
                String s = JOptionPane.showInputDialog("Enter magnification:");
                if (s != null) {
                    try {
                        mag = Double.valueOf(s).doubleValue();
                        mag = Math.max(mag, minMag);
                        mag = Math.min(mag, maxMag);
                        c = new ChangePiece(target.getId(), oldState, target.getState());
                    }
                    catch (NumberFormatException ex) {
                    }
                }
            }
            return c;
        }*/

        return piece.keyEvent(stroke);
    }

    public void draw(Graphics graphics, int i, int i1, Component component, double v) {
        this.piece.draw(graphics, i, i1, component, v);
    }

    public Rectangle boundingBox() {
        return this.piece.boundingBox();
    }

    public Shape getShape() {
        return this.piece.getShape();
    }

    public String getName() {
        return this.piece.getName();
    }

    @Override
    public String myGetType() {
        return ID;
    }

    public String getDescription() {
        return "Ship UI (mic.ShipUI)";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
    }
/*
    private void spawnPiece(GamePiece piece, Point position, Map playerMap) {
        Command placeCommand = playerMap.placeOrMerge(piece, position);
        placeCommand.execute();
        GameModule.getGameModule().sendAndLog(placeCommand);
    }

    private PieceSlot findPieceSlotByName(String name) {
        for(PieceSlot ps : GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class)){
            if(ps.getConfigureName().equals(name)) {
                return ps;
            }
        }
        return null;
    }
    */
}
