package mic;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import mic.ota.XWOTAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

import static mic.Util.logToChat;

/**
 * Created by Mic on 7/08/2018.
 *
 * Can be used to set a dial to a specific ship at autospawn-time
 */
public class StemDial2e extends Decorator implements EditablePiece {
    public static final String ID = "stemdial2e";

    // English names of the maneuvers
    // DialGen move / English Name
    private static Map<String, String> maneuverNames = ImmutableMap.<String, String>builder()
            .put("O","Stop")
            .put("A","Reverse Left Bank")
            .put("S","Reverse")
            .put("D","Reverse Right Bank")
            .put("E","Tallon Roll Left")
            .put("T","Hard Left")
            .put("B","Bank Left")
            .put("F","Forward")
            .put("N","Bank Right")
            .put("Y","Hard Right")
            .put("L","Segnor's Loop Left")
            .put("K","K-Turn")
            .put("P","Segnor's Loop Right")
            .put("R","Tallon Roll Right")
            .build();

    // maneuver images for the dial
    // DialGen format / image name
    private static Map<String, String> dialManeuverImages = ImmutableMap.<String, String>builder()
            .put("0OB", "Move_0_B.png")
            .put("0OW", "Move_0_W.png")
            .put("0OR", "Move_0_R.png")
            .put("1AB", "Move_1_RLB1_B.png")
            .put("1AW", "Move_1_RLB1_W.png")
            .put("1AR", "Move_1_RLB1_R.png")
            .put("1SB", "Move_1_R_B.png")
            .put("1SW", "Move_1_R_W.png")
            .put("1SR", "Move_1_R_R.png")
            .put("1DB", "Move_1_RRB1_B.png")
            .put("1DW", "Move_1_RRB1_W.png")
            .put("1DR", "Move_1_RRB1_R.png")
            .put("1EB","Move_1_TR_R_B.png")
            .put("1EW","Move_1_TR_R_W.png")
            .put("1ER","Move_1_TR_R_R.png")
            .put("1TB", "Move_1_H-L_B.png")
            .put("1TW", "Move_1_H-L_W.png")
            .put("1TR", "Move_1_H-L_R.png")
            .put("1BB", "Move_1_G-L_B.png")
            .put("1BW", "Move_1_G-L_W.png")
            .put("1BR", "Move_1_G-L_R.png")
            .put("1FB", "Move_1_S_B.png")
            .put("1FW", "Move_1_S_W.png")
            .put("1FR", "Move_1_S_R.png")
            .put("1NB", "Move_1_G-R_B.png")
            .put("1NW", "Move_1_G-R_W.png")
            .put("1NR", "Move_1_G-R_R.png")
            .put("1YB", "Move_1_H-R_B.png")
            .put("1YW", "Move_1_H-R_W.png")
            .put("1YR", "Move_1_H-R_R.png")
            .put("1LB","Move_1_TR_R_B.png")
            .put("1LW","Move_1_TR_R_W.png")
            .put("1LR","Move_1_SL_L_R.png")
            .put("1KB","Move_1_U_B.png")
            .put("1KW","Move_1_U_W.png")
            .put("1KR","Move_1_U_R.png")
            .put("1PB","Move_1_SL_R_B.png")
            .put("1PW","Move_1_SL_R_W.png")
            .put("1PR","Move_1_SL_R_R.png")
            .put("1RB","Move_1_TR_L_B.png")
            .put("1RW","Move_1_TR_L_W.png")
            .put("1RR","Move_1_TR_L_R.png")
            .put("2SB", "Move_2_R_B.png")
            .put("2SW", "Move_2_R_W.png")
            .put("2SR", "Move_2_R_R.png")
            .put("2EB", "Move_2_TR_R_B.png")
            .put("2EW", "Move_2_TR_R_W.png")
            .put("2ER", "Move_2_TR_R_R.png")
            .put("2TB", "Move_2_H-L_B.png")
            .put("2TW", "Move_2_H-L_W.png")
            .put("2TR", "Move_2_H-L_R.png")
            .put("2BB", "Move_2_G-L_B.png")
            .put("2BW", "Move_2_G-L_W.png")
            .put("2BR", "Move_2_G-L_R.png")
            .put("2FB", "Move_2_S_B.png")
            .put("2FW", "Move_2_S_W.png")
            .put("2FR", "Move_2_S_R.png")
            .put("2NB", "Move_2_G-R_B.png")
            .put("2NW", "Move_2_G-R_W.png")
            .put("2NR", "Move_2_G-R_R.png")
            .put("2YB", "Move_2_H-R_B.png")
            .put("2YW", "Move_2_H-R_W.png")
            .put("2YR", "Move_2_H-R_R.png")
            .put("2LB", "Move_2_SL_L_B.png")
            .put("2LW", "Move_2_SL_L_W.png")
            .put("2LR", "Move_2_SL_L_R.png")
            .put("2KB", "Move_2_U_B.png")
            .put("2KW", "Move_2_U_W.png")
            .put("2KR", "Move_2_U_R.png")
            .put("2PB", "Move_2_SL_R_B.png")
            .put("2PW", "Move_2_SL_R_W.png")
            .put("2PR", "Move_2_SL_R_R.png")
            .put("2RB", "Move_2_TR_L_B.png")
            .put("2RW", "Move_2_TR_L_W.png")
            .put("2RR", "Move_2_TR_L_R.png")
            .put("3EB", "Move_3_TR_R_B.png")
            .put("3EW", "Move_3_TR_R_W.png")
            .put("3ER", "Move_3_TR_R_R.png")
            .put("3TB", "Move_3_H-L_B.png")
            .put("3TW", "Move_3_H-L_W.png")
            .put("3TR", "Move_3_H-L_R.png")
            .put("3BB", "Move_3_G-L_B.png")
            .put("3BW", "Move_3_G-L_W.png")
            .put("3BR", "Move_3_G-L_R.png")
            .put("3FB", "Move_3_S_B.png")
            .put("3FW", "Move_3_S_W.png")
            .put("3FR", "Move_3_S_R.png")
            .put("3NB", "Move_3_G-R_B.png")
            .put("3NW", "Move_3_G-R_W.png")
            .put("3NR", "Move_3_G-R_R.png")
            .put("3YB", "Move_3_H-R_B.png")
            .put("3YW", "Move_3_H-R_W.png")
            .put("3YR", "Move_3_H-R_R.png")
            .put("3LB", "Move_3_SL_L_B.png")
            .put("3LW", "Move_3_SL_L_W.png")
            .put("3LR", "Move_3_SL_L_R.png")
            .put("3KB", "Move_3_U_B.png")
            .put("3KW", "Move_3_U_W.png")
            .put("3KR", "Move_3_U_R.png")
            .put("3PB", "Move_3_SL_R_B.png")
            .put("3PW", "Move_3_SL_R_W.png")
            .put("3PR", "Move_3_SL_R_R.png")
            .put("3RB", "Move_3_TR_L_B.png")
            .put("3RW", "Move_3_TR_L_W.png")
            .put("3RR", "Move_3_TR_L_R.png")
            .put("4FB", "Move_4_S_B.png")
            .put("4FW", "Move_4_S_W.png")
            .put("4FR", "Move_4_S_R.png")
            .put("4KB", "Move_4_U_B.png")
            .put("4KW", "Move_4_U_W.png")
            .put("4KR", "Move_4_U_R.png")
            .put("5FB", "Move_5_S_B.png")
            .put("5FW", "Move_5_S_W.png")
            .put("5FR", "Move_5_S_R.png")
            .put("5KB", "Move_5_U_B.png")
            .put("5KW", "Move_5_U_W.png")
            .put("5KR", "Move_5_U_R.png")
            .build();

    public StemDial2e(){
        this(null);
    }

    public StemDial2e(GamePiece piece){
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
    public String myGetType() {
        return ID;
    }
    @Override
    protected KeyCommand[] myGetKeyCommands() {
        return new KeyCommand[0];
    }
    @Override
    public Command myKeyEvent(KeyStroke keyStroke) {
        return null;
    }

    public String getDescription() {
        return "Custom StemDial (mic.StemDial)";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
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


    //this is the command that takes a ship xws name, fetches the maneuver info and constructs the dial layer by layer
    public static class DialGenerateCommand extends Command {
        GamePiece piece;
        static String xwsShipName = "";

        List<String> newMoveList;
        String shipName;
        String faction = "";

        DialGenerateCommand(String thisName, GamePiece piece, String thisFaction, List<XWS2Pilots> allShips) {

            // fetch the maneuver array of arrays according to the xws name passed on from autospawn or other means
            xwsShipName = thisName;
            faction = thisFaction;
            for(XWS2Pilots ship : allShips)
            {
                if(ship.getName().equals(thisName)) newMoveList = ship.getDial();
                shipName = ship.getName();
            }
            this.piece = piece;

        }

        DialGenerateCommand(List<String> aMoveList, String aShipName, GamePiece piece, String thisFaction) {
            // more direcct approach where the move list and the ship name are dictated directly without a master list fetch
            faction = thisFaction;
            newMoveList = aMoveList;
            shipName = aShipName;
            this.piece = piece;
        }

        // construct the dial Layers trait (Embellishment class) layer by layer according to the previous Array of Arrays.
        protected void executeCommand() {

            // build the layers for the maneuvers on the dial
            buildManeuvers(piece, newMoveList);

            // build the dial back and dial hide images
            buildDialMask(piece,xwsShipName,faction);

        }

        // build the maneuvers layer
        private void buildManeuvers(GamePiece piece, List moveList)
        {
            // build the type string
            StringBuilder stateString = new StringBuilder();
            StringBuilder moveNamesString = new StringBuilder();

            // start the state string
            stateString.append("emb2;;2;;Right;2;;Left;2;;;;;false;0;-38;");

            // loop through the maneuvers from the xws-data
            int count = 0;
            String moveImage;
            for (String move : newMoveList)
            {

                // look up the image for the maneuver
                moveImage = (String)dialManeuverImages.get(move);
                if(moveImage == null)
                {
                    logToChat("Can't find image for move: " + move);
                }else{

                    count++;
                    if(count != 1)
                    {
                        stateString.append(",");
                        moveNamesString.append(",");
                    }
                    // add the maneuver image to the dial
                    stateString.append(moveImage);

                    // build move names string
                    String speed = move.substring(0,1);
                    String moveCode = move.substring(1,2);
                    String moveName = maneuverNames.get(moveCode);
                    moveNamesString.append(moveName).append(" ").append(speed);

                }
            }

            // add in move names
            stateString.append(";").append(moveNamesString.toString());

            // finish the type string
            stateString.append(";true;Move;;;false;;1;1;true;;46,0;44,0");

            Embellishment myEmb = (Embellishment)Util.getEmbellishment(piece,"Layer - Move");
            //Embellishment myEmb = (Embellishment)Decorator.getDecorator(piece,Embellishment.class);

            myEmb.mySetType(stateString.toString());

        }




        private void buildDialMask(GamePiece piece, String xwsShipName, String faction)
        {
            final String wipResistanceMask = "Dial_Back_Rebel_WIP.png";
            final String wipRebelMask = "Dial_Back_Rebel_WIP.png";
            final String wipFirstOrderMask = "Dial_Back_Empire_WIP.pngg";
            final String wipEmpireMask = "Dial_Back_Empire_WIP.png";
            final String wipScumMask = "Dial_Back_Scum_WIP.png";
            final String wipRepublicMask = "Dial_Back_Republic_WIP.png";
            final String wipCISMask = "Dial_Back_CIS_WIP.png";


            // first get the core faction name from the subfaction (i.e. Resistance => RebelAlliance
            String coreFactionName = null;
            if(faction.equalsIgnoreCase("Rebel Alliance"))
            {
                coreFactionName = "rebelalliance";
            }else if(faction.equalsIgnoreCase("Resistance")){
                coreFactionName = "resistance";
            } else if(faction.equalsIgnoreCase("Galactic Empire")){
                coreFactionName = "galacticempire";
            }else if(faction.equalsIgnoreCase("First Order")) {
                coreFactionName = "firstorder";
            } else if(faction.equalsIgnoreCase("Scum and Villainy")){
                coreFactionName = "scumandvillainy";
            }else if(faction.equalsIgnoreCase("Galactic Republic")){
                coreFactionName = "galacticrepublic";
            }else if(faction.equalsIgnoreCase("CIS")){
                coreFactionName = "cis";
            }

            // get the back image
           // String dialBackImage = dialBackImages.get(xwsShipName+"/"+faction);
            String dialMaskImageName = "DialMask_"+coreFactionName+"_"+xwsShipName+".png";

            // if we don't have the image (unreleased ship), use a WIP image
            if(!XWOTAUtils.imageExistsInModule(dialMaskImageName))
            {
                if(faction.equalsIgnoreCase("Resistance"))
                {
                    dialMaskImageName = wipResistanceMask;
                }else if(faction.equalsIgnoreCase("Rebel Alliance"))
                {
                    dialMaskImageName = wipRebelMask;
                }else if(faction.equalsIgnoreCase("First Order"))
                {
                    dialMaskImageName = wipFirstOrderMask;
                }else if(faction.equalsIgnoreCase("Galactic Empire"))
                {
                    dialMaskImageName = wipEmpireMask;
                }else if(faction.equalsIgnoreCase("Scum and Villainy"))
                {
                    dialMaskImageName = wipScumMask;
                }else if(faction.equalsIgnoreCase("Galactic Republic"))
                {
                    dialMaskImageName = wipRepublicMask;
                }else if(faction.equalsIgnoreCase("CIS"))
                {
                    dialMaskImageName = wipCISMask;
                }
            }


            // get the dial hide image
           // String dialHideImage = dialHideImages.get(xwsShipName);
            String dialHideImageName = "DialHide_"+xwsShipName+".png";

            // if we don't have the image (unreleased ship), use a WIP image
            if(!XWOTAUtils.imageExistsInModule(dialHideImageName))
            {
                dialHideImageName = "Dial_Hide_WIP.png";
            }

            // build the string
            StringBuilder sb = new StringBuilder();
            sb.append("obs;82,130;");
            sb.append(dialMaskImageName);
            sb.append(";Reveal;G");
            sb.append(dialHideImageName);
            sb.append(";;player:;Peek");

            Obscurable myObs = (Obscurable)Decorator.getDecorator(piece,Obscurable.class);
            myObs.mySetType(sb.toString());

        }

        protected Command myUndoCommand() {
            return null;
        }

        //the following class is used to send the info to the other player whenever a dial generation command is issued, so it can be done locally on all machines playing/watching the game
        //only the ship XWS string is sent
        public static class DialGeneratorEncoder implements CommandEncoder {
            private static final Logger logger = LoggerFactory.getLogger(StemDial2e.class);
            private static final String commandPrefix = "DialGeneratorEncoder=";

            public static StemDial2e.DialGenerateCommand.DialGeneratorEncoder INSTANCE = new StemDial2e.DialGenerateCommand.DialGeneratorEncoder();

            public Command decode(String command) {
                if (command == null || !command.contains(commandPrefix)) {
                    return null;
                }
                logger.info("Decoding DialGenerateCommand");

                command = command.substring(commandPrefix.length());
                try {
                    xwsShipName = command;
                } catch (Exception e) {
                    logger.error("Error decoding DialGenerateCommand", e);
                    return null;
                }
                return null;
            }

            public String encode(Command c) {
                if (!(c instanceof DialGenerateCommand)) {
                    return null;
                }
                logger.info("Encoding DialGenerateCommand");
                DialGenerateCommand dialGenCommand = (DialGenerateCommand) c;
                try {
                    return commandPrefix + xwsShipName;
                } catch(Exception e) {
                    logger.error("Error encoding DialGenerateCommand", e);
                    return null;
                }
            }
        }

    }




}


