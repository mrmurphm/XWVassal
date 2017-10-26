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
import java.util.HashMap;

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
        this.piece = piece;
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
            String validMoves = (String)dial.getProperty("ValidMoveLayers");
            String nextLayer = getPreviousLevel(validMoves,layer);
            getInner().setProperty("MoveLayer",nextLayer);

        }else if(stroke.getKeyCode() == rightCommand.getKeyCode() && stroke.getKeyEventType() == rightCommand.getKeyEventType())
        {

            GamePiece dial = this.piece;
            String layer = (String)dial.getProperty("MoveLayer");
            String validMoves = (String)dial.getProperty("ValidMoveLayers");
            String previousLayer = getNextLevel(validMoves,layer);
            dial.setProperty("MoveLayer",previousLayer);

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

    private static HashMap<String,Integer> moveLayers = null;

    private static HashMap<String,String> storedShipLayers = new HashMap<String,String>();

    private static String getNextLevel(String maneuvers, String currentLevel)
    {
        String[] moves = maneuvers.split(",");
        String nextLevel = null;
        int previousIndex ;
        int foundIndex = 0 ;
        boolean found = false;
        for(int i = 0; i< moves.length && !found;i++)
        {
            if(moves[i].equals(currentLevel))
            {
                foundIndex = i;
                found = true;
            }
        }
        if(found)
        {
            previousIndex = foundIndex + 1;
            if(previousIndex > (moves.length-1))
            {
                previousIndex = 0;
            }
            nextLevel = moves[previousIndex];
        }
        return nextLevel;
    }

    private static String getPreviousLevel(String maneuvers, String currentLevel)
    {
        String[] moves = maneuvers.split(",");
        String previousLevel = null;
        int previousIndex;
        int foundIndex = 0;
        boolean found = false;
        for(int i = 0; i< moves.length && !found;i++)
        {
            if(moves[i].equals(currentLevel))
            {
                foundIndex = i;
                found = true;
            }
        }
        if(found)
        {
            previousIndex = foundIndex - 1;
            if(previousIndex < 0)
            {
                previousIndex = moves.length-1;
            }
            previousLevel = moves[previousIndex];
        }
        return previousLevel;
    }

    public static String getFirstLevel(String maneuvers)
    {
        // grab the first level from the string of comma separated lists
        String[] moves = maneuvers.split(",");
        return moves[0];
    }

    private static Integer getLayer(int XWSSpeed, int XWSMove, int XWSColor)
    {
        if(moveLayers == null)
        {
            loadData();
        }

        return moveLayers.get(XWSSpeed + "|" + XWSMove + "|" + XWSColor);
    }

    private static void loadData()
    {
        moveLayers = new HashMap<String, Integer>();

        moveLayers.put("1|10|1",1); // Reverse Left Bank 1 WHITE
        moveLayers.put("1|10|2",2); // Reverse Left Bank 1 GREEN
        moveLayers.put("1|10|3",3); // Reverse Left Bank 1 RED
        moveLayers.put("1|11|1",4); // Reverse Straight 1 WHITE
        moveLayers.put("1|11|2",5); // Reverse Straight 1 GREEN
        moveLayers.put("1|11|3",6); // Reverse Straight 1 RED
        moveLayers.put("1|12|1",7); // Reverse Right Bank WHITE
        moveLayers.put("1|12|2",8); // Reverse Right Bank GREEN
        moveLayers.put("1|12|3",9); // Reverse Right Bank RED
        moveLayers.put("0|2|1",10); // Zero Stop WHITE
        moveLayers.put("0|2|2",11); // Zero Stop GREEN
        moveLayers.put("0|2|3",12); // Zero Stop RED
        moveLayers.put("1|8|1",13); // Tallon Roll Left 1 WHITE
        moveLayers.put("1|8|2",14); // Tallon Roll Left 1 GREEN
        moveLayers.put("1|8|3",15); // Tallon Roll Left 1 RED
        moveLayers.put("1|0|1",16); // Hard Left 1 WHITE
        moveLayers.put("1|0|2",17); // Hard Left 1 GREEN
        moveLayers.put("1|0|3",18); // Hard Left 1 RED
        moveLayers.put("1|1|1",19); // Bank Left 1 WHITE
        moveLayers.put("1|1|2",20); // Bank Left 1 GREEN
        moveLayers.put("1|1|3",21); // Bank Left 1 RED
        moveLayers.put("1|2|1",22); // Forward 1 WHITE
        moveLayers.put("1|2|2",23); // Forward 1 GREEN
        moveLayers.put("1|2|3",24); // Forward 1 RED
        moveLayers.put("1|3|1",25); //Bank Right 1 White
        moveLayers.put("1|3|1",25); //Bank Right 1 White
        moveLayers.put("1|3|2",26); //Bank Right 1 Green
        moveLayers.put("1|3|3",27); //Bank Right 1 Red
        moveLayers.put("1|4|1",28); //Hard Right 1 White
        moveLayers.put("1|4|2",29); //Hard Right 1 Green
        moveLayers.put("1|4|3",30); //Hard Right 1 Red
        moveLayers.put("1|9|1",31); //TR Right 1 White
        moveLayers.put("1|9|2",32); //TR Right 1 Green
        moveLayers.put("1|9|3",33); //TR Right 1 Red
        moveLayers.put("1|6|1",34); //Left Sloop 1 White
        moveLayers.put("1|6|2",35); //Left Sloop 1 Green
        moveLayers.put("1|6|3",36); //Left Sloop 1 Red
        moveLayers.put("1|5|1",37); //K Turn 1 White
        moveLayers.put("1|5|2",38); //K Turn 1 Green
        moveLayers.put("1|5|3",39); //K Turn 1 Red
        moveLayers.put("1|7|1",40); //Right Sloop 1 White
        moveLayers.put("1|7|2",41); //Right Sloop 1 Green
        moveLayers.put("1|7|3",42); //Right Sloop 1 Red
        moveLayers.put("2|8|1",43); //TR Left 2 White
        moveLayers.put("2|8|2",44); //TR Left 2 Green
        moveLayers.put("2|8|3",45); //TR Left 2 Red
        moveLayers.put("2|0|1",46); //Hard Left 2 White
        moveLayers.put("2|0|2",47); //Hard Left 2 Green
        moveLayers.put("2|0|3",48); //Hard Left 2 Red
        moveLayers.put("2|1|1",49); //Bank Left 2 White
        moveLayers.put("2|1|2",50); //Bank Left 2 Green
        moveLayers.put("2|1|3",51); //Bank Left 2 Red
        moveLayers.put("2|2|1",52); //Straight 2 White
        moveLayers.put("2|2|2",53); //Straight 2 Green
        moveLayers.put("2|2|3",54); //Straight 2 Red
        moveLayers.put("2|3|1",55); //Bank Right 2 White
        moveLayers.put("2|3|2",56); //Bank Right 2 Green
        moveLayers.put("2|3|3",57); //Bank Right 2 Red
        moveLayers.put("2|4|1",58); //Hard Right 2 White
        moveLayers.put("2|4|2",59); //Hard Right 2 Green
        moveLayers.put("2|4|3",60); //Hard Right 2 Red
        moveLayers.put("2|9|1",61); //TR Right 2 White
        moveLayers.put("2|9|2",62); //TR Right 2 Green
        moveLayers.put("2|9|3",63); //TR Right 2 Red
        moveLayers.put("2|6|1",64); //Left Sloop 2 White
        moveLayers.put("2|6|2",65); //Left Sloop 2 Green
        moveLayers.put("2|6|3",66); //Left Sloop 2 Red
        moveLayers.put("2|5|1",67); //K Turn 2 White
        moveLayers.put("2|5|2",68); //K Turn 2 Green
        moveLayers.put("2|5|3",69); //K Turn 2 Red
        moveLayers.put("2|7|1",70); //Right Sloop 2 White
        moveLayers.put("2|7|2",71); //Right Sloop 2 Green
        moveLayers.put("2|7|3",72); //Right Sloop 2 Red
        moveLayers.put("3|8|1",73); //TR Left 3 White
        moveLayers.put("3|8|2",74); //TR Left 3 Green
        moveLayers.put("3|8|3",75); //TR Left 3 Red
        moveLayers.put("3|0|1",76); //Hard Left 3 White
        moveLayers.put("3|0|2",77); //Hard Left 3 Green
        moveLayers.put("3|0|3",78); //Hard Left 3 Red
        moveLayers.put("3|1|1",79); //Bank Left 3 White
        moveLayers.put("3|1|2",80); //Bank Left 3 Green
        moveLayers.put("3|1|3",81); //Bank Left 3 Red
        moveLayers.put("3|2|1",82); //Straight 3 White
        moveLayers.put("3|2|2",83); //Straight 3 Green
        moveLayers.put("3|2|3",84); //Straight 3 Red
        moveLayers.put("3|3|1",85); //Bank Right 3 White
        moveLayers.put("3|3|2",86); //Bank Right 3 Green
        moveLayers.put("3|3|3",87); //Bank Right 3 Red
        moveLayers.put("3|4|1",88); //Hard Right 3 White
        moveLayers.put("3|4|2",89); //Hard Right 3 Green
        moveLayers.put("3|4|3",90); //Hard Right 3 Red
        moveLayers.put("3|9|1",91); //TR Right 3 White
        moveLayers.put("3|9|2",92); //TR Right 3 Green
        moveLayers.put("3|9|3",93); //TR Right 3 Red
        moveLayers.put("3|6|1",94); //Left Sloop 3 White
        moveLayers.put("3|6|2",95); //Left Sloop 3 Green
        moveLayers.put("3|6|3",96); //Left Sloop 3 Red
        moveLayers.put("3|5|1",97); //K Turn 3 White
        moveLayers.put("3|5|2",98); //K Turn 3 Green
        moveLayers.put("3|5|3",99); //K Turn 3 Red
        moveLayers.put("3|7|1",100); //Right Sloop 3 White
        moveLayers.put("3|7|2",101); //Right Sloop 3 Green
        moveLayers.put("3|7|3",102); //Right Sloop 3 Red
        moveLayers.put("4|2|1",103); //Straight 4 White
        moveLayers.put("4|2|2",104); //Straight 4 Green
        moveLayers.put("4|2|3",105); //Straight 4 Red
        moveLayers.put("4|5|1",106); //K Turn 4 White
        moveLayers.put("4|5|2",107); //K Turn 4 Green
        moveLayers.put("4|5|3",108); //K Turn 4 Red
        moveLayers.put("5|2|1",109); //Straight 5 White
        moveLayers.put("5|2|2",110); //Straight 5 Green
        moveLayers.put("5|2|3",111); //Straight 5 Red
        moveLayers.put("5|5|1",112); //K Turn 5 White
        moveLayers.put("5|5|2",113); //K Turn 5 Green
        moveLayers.put("5|5|3",114); //K Turn 5 Red

    }

    // get an ordered comma separated list of layer #s that are valid for the given list of maneuvers
    public static String getValidMoveLayers(int[][] maneuvers, String shipXWSName)
    {
        String returnString;
        if(storedShipLayers.get(shipXWSName) != null)
        {
            returnString = storedShipLayers.get(shipXWSName);
        }else {
            boolean[] isValid = new boolean[115];

            for (int i = 0; i < maneuvers.length; i++) {
                for (int j = 0; j < maneuvers[i].length; j++) {
                    if (maneuvers[i][j] != 0) {
                        // this is a valid move
                        // get the layer
                        Integer layer = getLayer(i, j, maneuvers[i][j]);
                        isValid[layer] = true;
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (int i = 1; i < isValid.length; i++) {
                if (isValid[i]) {
                    if (count != 0) {
                        sb.append(",");
                    }

                    sb.append(i);
                    count++;
                }
            }
            returnString = sb.toString();
            storedShipLayers.put(shipXWSName,returnString);
        }
        return returnString;
    }

/*    private static String getValidMoveLayers(String shipXWSName)
    {
        String returnString = null;
        if(storedShipLayers.get(shipXWSName) != null) {
            returnString = storedShipLayers.get(shipXWSName);
        }
        return returnString;
    }*/

}
