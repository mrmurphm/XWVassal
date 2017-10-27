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

/**
 * DialMovementSelector
 *
 * This class decorates the generic auto-spawned dials.
 * It handles the cycle commands on the dial (COMMA and PERIOD), only selecting moves that are available to that dial.
 */
public class DialMovementSelector extends Decorator implements EditablePiece {

    public static final String ID = "dial-movement-selector;";

    // command for next move
    private KeyStroke rightCommand = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,KeyEvent.BUTTON1_DOWN_MASK);

    // command for previous move
    private KeyStroke leftCommand = KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,KeyEvent.BUTTON1_DOWN_MASK);

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
        // add the two commands to the menu
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

        // handle the COMMA and PERIOD keystrokes
        if(stroke.getKeyCode() == leftCommand.getKeyCode() && stroke.getKeyEventType() == leftCommand.getKeyEventType())
        {
            GamePiece dial = this.piece;

            // get the current layer (maneuver) from the dial
            String layer = (String)dial.getProperty("MoveLayer");

            // get the comma separated list of valid maneuvers from the dial
            String validMoves = (String)dial.getProperty("ValidMoveLayers");

            // figure out the previous valid maneuver
            String nextLayer = getPreviousLevel(validMoves,layer);

            // set that maneuver on the dial
            getInner().setProperty("MoveLayer",nextLayer);

        }else if(stroke.getKeyCode() == rightCommand.getKeyCode() && stroke.getKeyEventType() == rightCommand.getKeyEventType())
        {
            GamePiece dial = this.piece;

            // get the current layer (maneuver) from the dial
            String layer = (String)dial.getProperty("MoveLayer");

            // get the comma separated list of valid maneuvers from the dial
            String validMoves = (String)dial.getProperty("ValidMoveLayers");

            // figure out the next valid maneuver
            String previousLayer = getNextLevel(validMoves,layer);

            // set that maneuver on the dial
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

    /**
     * Gets the next level (maneuver) from the list of valid maneuvers
     *
     * @param maneuvers comma separated list of valid maneuver IDs
     * @param currentLevel current maneuver ID
     * @return Next maneuver ID (level)
     */
    private static String getNextLevel(String maneuvers, String currentLevel)
    {
        String nextLevel = null;
        int nextIndex ;
        int foundIndex = 0 ;
        boolean found = false;

        // split the list of valid maneuver IDs into an array
        String[] moves = maneuvers.split(",");

        // loop through the valid maneuvers, looking for the current maneuver
        for(int i = 0; i< moves.length && !found;i++)
        {
            if(moves[i].equals(currentLevel))
            {
                // we've found the manuever

                // set the index of the maneuver
                foundIndex = i;
                found = true;
            }
        }

        if(found)
        {
            // find the next maneuver index
            nextIndex = foundIndex + 1;

            // if we've gone beyond the list of valid maneuvers, go back to the beginning of the list
            if(nextIndex > (moves.length-1))
            {
                nextIndex = 0;
            }

            // get the ID of the next maneuver
            nextLevel = moves[nextIndex];
        }
        return nextLevel;
    }

    /**
     * Gets the previous level (maneuver) from the list of valid maneuvers
     *
     * @param maneuvers comma separated list of valid maneuver IDs
     * @param currentLevel current maneuver ID
     * @return Previous maneuver ID (level)
     */
    private static String getPreviousLevel(String maneuvers, String currentLevel)
    {
        String previousLevel = null;
        int previousIndex;
        int foundIndex = 0;
        boolean found = false;

        // split the list of valid maneuver IDs into an array
        String[] moves = maneuvers.split(",");

        // loop through the valid maneuvers, looking for the current maneuver
        for(int i = 0; i< moves.length && !found;i++)
        {
            if(moves[i].equals(currentLevel))
            {
                // we've found the manuever

                // set the index of the maneuver
                foundIndex = i;
                found = true;
            }
        }

        if(found)
        {
            // find the previous maneuver index
            previousIndex = foundIndex - 1;

            // if we've gone beyond the list of valid maneuvers, go back to the end of the list
            if(previousIndex < 0)
            {
                previousIndex = moves.length-1;
            }

            // get the ID of the previous maneuver
            previousLevel = moves[previousIndex];
        }
        return previousLevel;
    }

    /**
     * Gets the first maneuver from the list of valid maneuvers
     *
     * @param maneuvers comma separated list of valid maneuvers
     * @return first maneuver id in the list
     */
    public static String getFirstLevel(String maneuvers)
    {
        // split the comma separated list of maneuver IDs into an array
        String[] moves = maneuvers.split(",");
        return moves[0];
    }

    /**
     * Gets the maneuver ID (layer) for a given entry in the XWS "maneuvers" field
     *
     * manuevers in the XWS are a two dimensional array (X,Y), where:
     * X = roughly the speed (0 = stop, 1 = 1 and -1, 2 = 2, etc)
     * Y = the type of move
     *    0 = 1 Turn Left
     *    1 = Bank Left
     *    2 = Straight
     *    3 = Bank Right
     *    4 = Turn Right
     *    5 = K Turn
     *    6 = Sloop Left
     *    7 = Sloop Right
     *    8 =  Tallon Roll Left
     *    9 = Tallon Roll Right
     *   10 = -1 Left
     *   11 = -1 Straight
     *   12 = -1 Right
     * The value at the X,Y coordinate is the color of the move:
     *    0 = Not a valid move
     *    1 = white
     *    2 = green
     *    3 = red
     *
     * @param XWSSpeed Speed of the maneuver (X coordinate of the array)
     * @param XWSMove   Move (Y coordinate of the array)
     * @param XWSColor  Color (color of the move)
     * @return the maneuver ID (layer) of the move
     */
    private static Integer getLayer(int XWSSpeed, int XWSMove, int XWSColor)
    {
        // if the data hasn't yet been loaded, load it
        if(moveLayers == null)
        {
            loadData();
        }

        // The maneuver IDs are stored in a HashMap with the key being <speed>|<move>|<color>
        return moveLayers.get(XWSSpeed + "|" + XWSMove + "|" + XWSColor);
    }

    /**
     * Load the static maneuver data
     *
     * These maneuver IDs correspond to the layers in the dial layer showing the movement images.
     * If a maneuver needs to be added or moved, you'll need to add/change it here AND in the dial layer of the
     * 3 autogenerated dials.
     *
     * The maneuver ID (layer) also dictates the ORDER of the maneuvers as you cycle through them.
     */
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

    /**
     * Get an ordered comma separated list of layer #s that are valid for the given list of maneuvers
     *
     * @param maneuvers content of the "maneuvers" attribute from the ship XWS
     * @param shipXWSName XWS ship name
     * @return comma separated list of valid maneuvers for this ship
     */
    public static String getValidMoveLayers(int[][] maneuvers, String shipXWSName)
    {
        String returnString;

        // check the local cache to see if we've already done the work for this ship
        if(storedShipLayers.get(shipXWSName) != null)
        {
            // we've already done this ship, so just grab from the cache
            returnString = storedShipLayers.get(shipXWSName);
        }else {
            // we haven't yet done this ship

            // set up an array that mirrors the 115 possible moves, so we can keep track of which ones are valid for this ship
            // all moves start out as INVALID
            boolean[] isValid = new boolean[115];

            // loop through the maneuvers from the XWS
            for (int i = 0; i < maneuvers.length; i++) {
                for (int j = 0; j < maneuvers[i].length; j++) {
                    if (maneuvers[i][j] != 0) {
                        // this is a valid move
                        // get the layer
                        Integer layer = getLayer(i, j, maneuvers[i][j]);
                        // set it to valid
                        isValid[layer] = true;
                    }
                }
            }

            // now build the comma separated list of valid moves
            StringBuilder sb = new StringBuilder();
            int count = 0;

            // loop through the valid array
            for (int i = 1; i < isValid.length; i++) {

                // if it's a valid move, add the ID to the list
                if (isValid[i]) {

                    // if this isn't the first entry, we need to add a comma for separation
                    if (count != 0) {
                        sb.append(",");
                    }

                    sb.append(i);
                    count++;
                }
            }
            returnString = sb.toString();

            // store the list in a HashMap as a cache so we don't have to calculate this ship again this session
            storedShipLayers.put(shipXWSName,returnString);
        }
        return returnString;
    }


}
