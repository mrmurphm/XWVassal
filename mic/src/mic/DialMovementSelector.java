package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class DialMovementSelector extends Decorator implements EditablePiece {

    public static final String ID = "dial-movement-selector;";


    private KeyStroke rightCommand =
            KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,KeyEvent.BUTTON1_DOWN_MASK);

    private KeyStroke leftCommand =
            KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,KeyEvent.BUTTON1_DOWN_MASK);

    // The list of popup menu commands
    private KeyCommand[] commands;


    public DialMovementSelector() {
        this(null);
    }

    public DialMovementSelector(GamePiece piece) {
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
            commands = new KeyCommand[]{new KeyCommand("Right", rightCommand, this),new KeyCommand("Left", leftCommand, this) };
        }
        return commands;
    }

    @Override
    public Command myKeyEvent(KeyStroke keyStroke) {
        return null;
    }

    @Override
    public Command keyEvent(KeyStroke stroke) {

        if(stroke.getKeyCode() == leftCommand.getKeyCode() && stroke.getKeyEventType() == leftCommand.getKeyEventType())
        {

            GamePiece dial = getInner();

            String layer = (String)dial.getProperty("MoveLayer");
            Integer nextLayer = Integer.valueOf(layer) - 1;

            if(nextLayer.intValue() < 1)
            {
                nextLayer = new Integer(114);
            }

            getInner().setProperty("MoveLayer",nextLayer.toString());

        }else if(stroke.getKeyCode() == rightCommand.getKeyCode() && stroke.getKeyEventType() == rightCommand.getKeyEventType())
        {
            GamePiece dial = getInner();

            String layer = (String)dial.getProperty("MoveLayer");
            Integer nextLayer = Integer.valueOf(layer) + 1;

            if(nextLayer.intValue() > 114)
            {
                nextLayer = new Integer(1);
            }

            getInner().setProperty("MoveLayer",nextLayer.toString());
        }

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
        return "Dial Movement Selector (mic.DialMovementSelector)";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

}
