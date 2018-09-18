package mic;

import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.MoveTracker;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.counters.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import mic.manuvers.ManeuverPaths;
import mic.manuvers.PathPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Map;

import static mic.Util.*;

/**
 * Created by amatheny on 2/14/17.
 *
 * Second role: to completely intercept every maneuver shortcut and deal with movement AND autobump AND out of bound detection
 */
public class AutoBumpDecorator extends Decorator implements EditablePiece {
    private static final Logger logger = LoggerFactory.getLogger(AutoBumpDecorator.class);
    public static final String ID = "auto-bump;";
    private final FreeRotator testRotator;

    private ShipPositionState prevPosition = null;
    private ManeuverPaths lastManeuver = null;
    private FreeRotator myRotator = null;
    //public CollisionVisualization previousCollisionVisualization = null;
    MapVisualizations previousCollisionVisualization = null;

    private static final Map<String, ManeuverPaths> keyStrokeToManeuver = ImmutableMap.<String, ManeuverPaths>builder()
            .put("SHIFT 1", ManeuverPaths.Str1)
            .put("SHIFT 2", ManeuverPaths.Str2)
            .put("SHIFT 3", ManeuverPaths.Str3)
            .put("SHIFT 4", ManeuverPaths.Str4)
            .put("SHIFT 5", ManeuverPaths.Str5)
            .put("CTRL SHIFT 1", ManeuverPaths.LT1)
            .put("CTRL SHIFT 2", ManeuverPaths.LT2)
            .put("CTRL SHIFT 3", ManeuverPaths.LT3)
            .put("ALT SHIFT 1", ManeuverPaths.RT1)
            .put("ALT SHIFT 2", ManeuverPaths.RT2)
            .put("ALT SHIFT 3", ManeuverPaths.RT3)
            .put("CTRL 1", ManeuverPaths.LBk1)
            .put("CTRL 2", ManeuverPaths.LBk2)
            .put("CTRL 3", ManeuverPaths.LBk3)
            .put("ALT 1", ManeuverPaths.RBk1)
            .put("ALT 2", ManeuverPaths.RBk2)
            .put("ALT 3", ManeuverPaths.RBk3)
            .put("ALT CTRL 1", ManeuverPaths.K1)
            .put("ALT CTRL 2", ManeuverPaths.K2)
            .put("ALT CTRL 3", ManeuverPaths.K3)
            .put("ALT CTRL 4", ManeuverPaths.K4)
            .put("ALT CTRL 5", ManeuverPaths.K5)
            .put("CTRL 6", ManeuverPaths.RevLbk1)
            .put("SHIFT 6", ManeuverPaths.RevStr1)
            .put("SHIFT 7", ManeuverPaths.RevStr2)
            .put("ALT 6", ManeuverPaths.RevRbk1)
            .put("CTRL Q", ManeuverPaths.SloopL1)
            .put("CTRL W", ManeuverPaths.SloopL2)
            .put("CTRL E", ManeuverPaths.SloopL3)
            .put("ALT Q", ManeuverPaths.SloopR1)
            .put("ALT W", ManeuverPaths.SloopR2)
            .put("ALT E", ManeuverPaths.SloopR3)
            .put("CTRL SHIFT E", ManeuverPaths.SloopL3Turn)
            .put("ALT SHIFT E", ManeuverPaths.SloopR3Turn)
            .put("CTRL I", ManeuverPaths.TrollL1)
            .put("CTRL Y", ManeuverPaths.TrollL2)
            .put("CTRL T", ManeuverPaths.TrollL3)
            .put("ALT I", ManeuverPaths.TrollR1)
            .put("ALT Y", ManeuverPaths.TrollR2)
            .put("ALT T", ManeuverPaths.TrollR3)
            .build();

    public AutoBumpDecorator() {
        this(null);
    }

    public AutoBumpDecorator(GamePiece piece) {
        mic.LoggerUtil.logEntry(logger,"AutoBumpDecorator");
        setInner(piece);
        this.testRotator = new FreeRotator("rotate;360;;;;;;;", null);
        mic.LoggerUtil.logExit(logger,"AutoBumpDecorator");
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
        return new KeyCommand[0];
    }

    @Override
    public Command myKeyEvent(KeyStroke keyStroke) {
        return null;
    }

    private PieceSlot findPieceSlotByID(String gpID) {
        mic.LoggerUtil.logEntry(logger,"findPieceSlotByID");
        for(PieceSlot ps : GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class)){
            if(gpID.equals(ps.getGpId())) return ps;
        }
        mic.LoggerUtil.logExit(logger,"findPieceSlotByID");
        return null;
    }


    private Command spawnRotatedPiece(ManeuverPaths theManeuv) {
        mic.LoggerUtil.logEntry(logger,"spawnRotatedPiece");
        //STEP 1: Collision aide template, centered as in in the image file, centered on 0,0 (upper left corner)
        GamePiece piece = newPiece(findPieceSlotByID(theManeuv.getAide_gpID()));

        //Info Gathering: Position of the center of the ship, integers inside a Point
        double shipx = this.getPosition().getX();
        double shipy = this.getPosition().getY();
        Point shipPt = new Point((int) shipx, (int) shipy); // these are the center coordinates of the ship, namely, shipPt.x and shipPt.y

         //Info Gathering: offset vector (integers) that's used in local coordinates, right after a rotation found in lastManeuver.getTemplateAngle(), so that it's positioned behind nubs properly
        double x=0.0, y=0.0;
        if(whichSizeShip(this)==3){
            x = theManeuv.getAide_xLarge();
            y = theManeuv.getAide_yLarge();
        }
        else if(whichSizeShip(this)==2){
            x = theManeuv.getAide_xMedium();
            y = theManeuv.getAide_yMedium();
        }
        else{
            x = theManeuv.getAide_x();
            y = theManeuv.getAide_y();
        }
        int posx =  (int)x;
        int posy =  (int)y;
        Point tOff = new Point(posx, posy); // these are the offsets in local space for the templates, if the ship's center is at 0,0 and pointing up


        //Info Gathering: gets the angle from ManeuverPaths which deals with degrees, local space with ship at 0,0, pointing up
        double tAngle = lastManeuver.getTemplateAngle();
        double sAngle = this.getRotator().getAngle();

        //STEP 2: rotate the collision aide with both the getTemplateAngle and the ship's final angle,
        FreeRotator fR = (FreeRotator)Decorator.getDecorator(piece, FreeRotator.class);
        fR.setAngle(sAngle - tAngle);

        //STEP 3: rotate a double version of tOff to get tOff_rotated
        double xWork = Math.cos(-Math.PI*sAngle/180.0f)*tOff.getX() - Math.sin(-Math.PI*sAngle/180.0f)*tOff.getY();
        double yWork = Math.sin(-Math.PI*sAngle/180.0f)*tOff.getX() + Math.cos(-Math.PI*sAngle/180.0f)*tOff.getY();
        Point tOff_rotated = new Point((int)xWork, (int)yWork);

        //STEP 4: translation into place
        Command placeCommand = getMap().placeOrMerge(piece, new Point(tOff_rotated.x + shipPt.x, tOff_rotated.y + shipPt.y));
        mic.LoggerUtil.logExit(logger,"spawnRotatedPiece");
        return placeCommand;
    }

    @Override
    public Command keyEvent(KeyStroke stroke) {
        mic.LoggerUtil.logEntry(logger,"keyEvent");
        this.previousCollisionVisualization = new MapVisualizations();

        ManeuverPaths path = getKeystrokePath(stroke);
        // Is this a keystroke for a maneuver? Deal with the 'no' cases first
        if (path == null) {
            //check to see if 'c' was pressed
            if(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, false).equals(stroke) && lastManeuver != null) {
                List<BumpableWithShape> otherShipShapes = getShipsWithShapes();

                    if(lastManeuver != null) {
                        Command placeCollisionAide = spawnRotatedPiece(lastManeuver);
                        placeCollisionAide.execute();
                        GameModule.getGameModule().sendAndLog(placeCollisionAide);
                    }

                boolean isCollisionOccuring = findCollidingEntity(BumpableWithShape.getBumpableCompareShape(this), otherShipShapes) != null ? true : false;
                //backtracking requested with a detected bumpable overlap, deal with it
                if (isCollisionOccuring) {
                    Command innerCommand = piece.keyEvent(stroke);
                    Command bumpResolveCommand = resolveBump(otherShipShapes);
                    return bumpResolveCommand == null ? innerCommand : innerCommand.append(bumpResolveCommand);
                }
            }
            // 'c' keystroke has finished here, leave the method altogether
            mic.LoggerUtil.logExit(logger,"keyEvent");
            return piece.keyEvent(stroke);
        }

        // We know we're dealing with a maneuver keystroke
        if (stroke.isOnKeyRelease() == false) {
            // find the list of other bumpables
            List<BumpableWithShape> otherBumpableShapes = getBumpablesWithShapes();

            //safeguard old position and path
            this.prevPosition = getCurrentState();
            this.lastManeuver = path;

            //This PathPart list will be used everywhere: moving, bumping, out of boundsing
            //maybe fetch it for both 'c' behavior and movement
            final List<PathPart> parts = path.getTransformedPathParts(
                    this.getCurrentState().x,
                    this.getCurrentState().y,
                    this.getCurrentState().angle,
                    whichSizeShip(this)
            );

            //this is the final ship position post-move
            PathPart part = parts.get(parts.size()-1);

            //Get the ship name string for announcements
            String yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
            //Start the Command chain
            Command innerCommand = piece.keyEvent(stroke);
            innerCommand.append(buildTranslateCommand(part, path.getAdditionalAngleForShip()));

            //check for Tallon rolls and spawn the template
            if(lastManeuver == ManeuverPaths.TrollL1  || lastManeuver == ManeuverPaths.TrollL2 || lastManeuver == ManeuverPaths.TrollL3
            || lastManeuver == ManeuverPaths.TrollR1  || lastManeuver == ManeuverPaths.TrollR2 || lastManeuver == ManeuverPaths.TrollR3) {
                Command placeTrollTemplate = spawnRotatedPiece(lastManeuver);
                innerCommand.append(placeTrollTemplate);
            }
            //These lines fetch the Shape of the last movement template used
            FreeRotator rotator = (FreeRotator) (Decorator.getDecorator(Decorator.getOutermost(this), FreeRotator.class));
            Shape lastMoveShapeUsed = path.getTransformedTemplateShape(this.getPosition().getX(),
                    this.getPosition().getY(),
                    whichSizeShip(this),
                    rotator);

            //don't check for collisions in windows other than the main map
            if(!"Contested Sector".equals(getMap().getMapName())) return innerCommand;

            innerCommand.append(logToChatWithTimeCommand("* --- " + yourShipName + " performs move: " + path.getFullName()));

            //Check for template shape overlap with mines, asteroids, debris
            checkTemplateOverlap(lastMoveShapeUsed, otherBumpableShapes);
            //Check for ship bumping other ships, mines, asteroids, debris
            announceBumpAndPaint(otherBumpableShapes);
            //Check if a ship becomes out of bounds
            checkIfOutOfBounds(yourShipName);

            //Add all the detected overlapping shapes to the map drawn components here
            if(this.previousCollisionVisualization != null &&  this.previousCollisionVisualization.getShapes().size() > 0){
                innerCommand.append(this.previousCollisionVisualization);
                this.previousCollisionVisualization.execute();
            }
            mic.LoggerUtil.logExit(logger,"keyEvent");
            return innerCommand;
        }
        //the maneuver has finished. return control of the event to vassal to do nothing
        mic.LoggerUtil.logExit(logger,"keyEvent");
        return piece.keyEvent(stroke);
    }

    private void checkTemplateOverlap(Shape lastMoveShapeUsed, List<BumpableWithShape> otherBumpableShapes) {
        mic.LoggerUtil.logEntry(logger,"checkTemplateOverlap");
        List<BumpableWithShape> collidingEntities = findCollidingEntities(lastMoveShapeUsed, otherBumpableShapes);
        MapVisualizations cvFoundHere = new MapVisualizations(lastMoveShapeUsed);

        int howManyBumped = 0;
        StringBuilder bumpAlertSB;
        String yourShipName;
        for (BumpableWithShape bumpedBumpable : collidingEntities)
        {
            yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
            if (bumpedBumpable.type.equals("Asteroid")) {
                bumpAlertSB = new StringBuilder();
                bumpAlertSB.append("* --- Overlap detected with ").append(yourShipName).append("'s maneuver template and an asteroid.");
                //String bumpAlertString = "* --- Overlap detected with " + yourShipName + "'s maneuver template and an asteroid.";
                logToChatWithTime(bumpAlertSB.toString());
                cvFoundHere.add(bumpedBumpable.shape);
                this.previousCollisionVisualization.add(bumpedBumpable.shape);
                howManyBumped++;
            } else if (bumpedBumpable.type.equals("Debris")) {
                bumpAlertSB = new StringBuilder();
                bumpAlertSB.append("* --- Overlap detected with ").append(yourShipName).append("'s maneuver template and a debris cloud.");
                //String bumpAlertString = "* --- Overlap detected with " + yourShipName + "'s maneuver template and a debris cloud.";
                logToChatWithTime(bumpAlertSB.toString());
                cvFoundHere.add(bumpedBumpable.shape);
                this.previousCollisionVisualization.add(bumpedBumpable.shape);
                howManyBumped++;
            } else if (bumpedBumpable.type.equals("Mine")) {
                bumpAlertSB = new StringBuilder();
                bumpAlertSB.append("* --- Overlap detected with ").append(yourShipName).append("'s maneuver template and a mine.");
               // String bumpAlertString = "* --- Overlap detected with " + yourShipName + "'s maneuver template and a mine.";
                logToChatWithTime(bumpAlertSB.toString());
                cvFoundHere.add(bumpedBumpable.shape);
                this.previousCollisionVisualization.add(bumpedBumpable.shape);
                howManyBumped++;
            }
        }
        if (howManyBumped > 0) {
            this.previousCollisionVisualization.add(lastMoveShapeUsed);
        }
        mic.LoggerUtil.logExit(logger,"checkTemplateOverlap");
    }

    private void checkIfOutOfBounds(String yourShipName) {
        mic.LoggerUtil.logEntry(logger,"checkIfOutOfBounds");
        Rectangle mapArea = new Rectangle(0,0,0,0);
        try{
            Board b = getMap().getBoards().iterator().next();
            mapArea = b.bounds();
            String name = b.getName();
        }catch(Exception e)
        {
            logToChat("Board name isn't formatted right, change to #'x#' Description");
        }
        Shape theShape = BumpableWithShape.getBumpableCompareShape(this);

        if(theShape.getBounds().getMaxX() > mapArea.getBounds().getMaxX()  || // too far to the right
                theShape.getBounds().getMaxY() > mapArea.getBounds().getMaxY() || // too far to the bottom
                theShape.getBounds().getX() < mapArea.getBounds().getX() || //too far to the left
                theShape.getBounds().getY() < mapArea.getBounds().getY()) // too far to the top
        {

            logToChatWithTime("* -- " + yourShipName + " flew out of bounds");
            this.previousCollisionVisualization.add(theShape);
        }
        mic.LoggerUtil.logExit(logger,"checkIfOutOfBounds");
    }

    private void announceBumpAndPaint(List<BumpableWithShape> otherBumpableShapes) {
        mic.LoggerUtil.logEntry(logger,"announceBumpAndPaint");
        Shape theShape = BumpableWithShape.getBumpableCompareShape(this);

        List<BumpableWithShape> collidingEntities = findCollidingEntities(theShape, otherBumpableShapes);

        int howManyBumped = 0;
        String yourShipName;
        StringBuilder bumpAlertSB;
        for (BumpableWithShape bumpedBumpable : collidingEntities) {
            yourShipName = getShipStringForReports(true, this.getProperty("Pilot Name").toString(), this.getProperty("Craft ID #").toString());
            if (bumpedBumpable.type.equals("Ship")) {
                bumpAlertSB = new StringBuilder();
                String otherShipName = getShipStringForReports(false, bumpedBumpable.pilotName, bumpedBumpable.shipName);
                bumpAlertSB.append("* --- Overlap detected with ").append(yourShipName).append(" and ").append(otherShipName).append(". Resolve this by hitting the 'c' key.");
                //String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and " + otherShipName + ". Resolve this by hitting the 'c' key.";
                logToChatWithTime(bumpAlertSB.toString());
                this.previousCollisionVisualization.add(bumpedBumpable.shape);
                howManyBumped++;
            } else if (bumpedBumpable.type.equals("Asteroid")) {
                bumpAlertSB = new StringBuilder();
                bumpAlertSB.append("* --- Overlap detected with ").append(yourShipName).append(" and an asteroid.");
               // String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and an asteroid.";
                logToChatWithTime(bumpAlertSB.toString());
                this.previousCollisionVisualization.add(bumpedBumpable.shape);
                howManyBumped++;
            } else if (bumpedBumpable.type.equals("Debris")) {
                bumpAlertSB = new StringBuilder();
                bumpAlertSB.append("* --- Overlap detected with ").append(yourShipName).append(" and a debris cloud.");
                //String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and a debris cloud.";
                logToChatWithTime(bumpAlertSB.toString());
                this.previousCollisionVisualization.add(bumpedBumpable.shape);
                howManyBumped++;
            } else if (bumpedBumpable.type.equals("Mine")) {
                bumpAlertSB = new StringBuilder();
                bumpAlertSB.append("* --- Overlap detected with ").append(yourShipName).append(" and a mine.");
                //String bumpAlertString = "* --- Overlap detected with " + yourShipName + " and a mine.";
                logToChatWithTime(bumpAlertSB.toString());
                this.previousCollisionVisualization.add(bumpedBumpable.shape);
                howManyBumped++;
            }
        }
        if (howManyBumped > 0) {
            this.previousCollisionVisualization.add(theShape);
        }
        mic.LoggerUtil.logExit(logger,"announceBumpAndPaint");
    }


    /**
     * Iterate in reverse over path of last maneuver and return a command that
     * will move the ship to a non-overlapping position and rotation
     *
     * @return
     */
    private Command resolveBump(List<BumpableWithShape> otherBumpableShapes) {
        mic.LoggerUtil.logEntry(logger,"resolveBump");
        if (this.lastManeuver == null || this.prevPosition == null) {
            return null;
        }
        Shape rawShape = BumpableWithShape.getRawShape(this);
        final List<PathPart> parts = this.lastManeuver.getTransformedPathParts(
                this.prevPosition.x,
                this.prevPosition.y,
                this.prevPosition.angle,
                whichSizeShip(this)
        );



        for (int i = parts.size() - 1; i >= 0; i--) {
            PathPart part = parts.get(i);
            Shape movedShape = AffineTransform
                    .getTranslateInstance(part.getX(), part.getY())
                    .createTransformedShape(rawShape);
            double roundedAngle = convertAngleToGameLimits(part.getAngle());
            movedShape = AffineTransform
                    .getRotateInstance(Math.toRadians(-roundedAngle), part.getX(), part.getY())
                    .createTransformedShape(movedShape);

            BumpableWithShape bumpedBumpable = findCollidingEntity(movedShape, otherBumpableShapes);
            if (bumpedBumpable == null) {
                mic.LoggerUtil.logExit(logger,"resolveBump");
                return buildTranslateCommand(part,0.0f);
            }
        }
        mic.LoggerUtil.logExit(logger,"resolveBump");
        // Could not find a position that wasn't bumping, bring it back to where it was before
        return buildTranslateCommand(new PathPart(this.prevPosition.x, this.prevPosition.y, this.prevPosition.angle), 0.0f);
    }

    /**
     * Builds vassal command to transform the current ship to the given PathPart
     *
     * @param part
     * @return
     */
    private Command buildTranslateCommand(PathPart part, double additionalAngle) {
        mic.LoggerUtil.logEntry(logger,"buildTranslateCommand");
        // Copypasta from VASSAL.counters.Pivot
        ChangeTracker changeTracker = new ChangeTracker(this);
        getRotator().setAngle(part.getAngle() + additionalAngle);

        setProperty("Moved", Boolean.TRUE);

        Command result = changeTracker.getChangeCommand();

        GamePiece outermost = Decorator.getOutermost(this);
        MoveTracker moveTracker = new MoveTracker(outermost);
        Point point = new Point((int) Math.floor(part.getX() + 0.5), (int) Math.floor(part.getY() + 0.5));
        // ^^ There be dragons here ^^ - vassals gives positions as doubles but only lets them be set as ints :(
        this.getMap().placeOrMerge(outermost, point);
        result = result.append(moveTracker.getMoveCommand());
/*
        MovementReporter reporter = new MovementReporter(result);

        Command reportCommand = reporter.getReportCommand();
        if (reportCommand != null) {
            reportCommand.execute();
            result = result.append(reportCommand);
        }

        result = result.append(reporter.markMovedPieces());
*/
        mic.LoggerUtil.logExit(logger,"buildTranslateCommand");
        return result;
    }

    /**
     * Returns the comparision shape of the first bumpable colliding with the provided ship.  Returns null if there
     * are no collisions
     *
     * @param myTestShape
     * @return
     */
    private BumpableWithShape findCollidingEntity(Shape myTestShape, List<BumpableWithShape> otherShapes) {
        mic.LoggerUtil.logEntry(logger,"findCollidingEntity");
        List<BumpableWithShape> allCollidingEntities = findCollidingEntities(myTestShape, otherShapes);
        if (allCollidingEntities.size() > 0) {
            mic.LoggerUtil.logExit(logger,"findCollidingEntity");
            return allCollidingEntities.get(0);
        } else {
            mic.LoggerUtil.logExit(logger,"findCollidingEntity");
            return null;
        }
    }

    /**
     * Returns a list of all bumpables colliding with the provided ship.  Returns an empty list if there
     * are no collisions
     *
     * @param myTestShape
     * @return
     */
    private List<BumpableWithShape> findCollidingEntities(Shape myTestShape, List<BumpableWithShape> otherShapes) {
        mic.LoggerUtil.logEntry(logger,"findCollidingEntities");
        List<BumpableWithShape> shapes = Lists.newLinkedList();
        for (BumpableWithShape otherBumpableShape : otherShapes) {
            if (Util.shapesOverlap(myTestShape, otherBumpableShape.shape)) {
                shapes.add(otherBumpableShape);
            }
        }
        mic.LoggerUtil.logExit(logger,"findCollidingEntities");
        return shapes;
    }



    public void draw(Graphics graphics, int i, int i1, Component component, double v) {
        mic.LoggerUtil.logEntry(logger,"draw");
        this.piece.draw(graphics, i, i1, component, v);
        mic.LoggerUtil.logExit(logger,"draw");
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
        return "Custom auto-bump resolution (mic.AutoBumpDecorator)";
    }

    public void mySetType(String s) {

    }

    public HelpFile getHelpFile() {
        return null;
    }

    /**
     * Returns FreeRotator decorator associated with this instance
     *
     * @return
     */
    private FreeRotator getRotator() {
        if (this.myRotator == null) {
            this.myRotator = ((FreeRotator) Decorator.getDecorator(getOutermost(this), FreeRotator.class));
        }
        return this.myRotator;
    }

    /**
     * Returns a new ShipPositionState based on the current position and angle of this ship
     *
     * @return
     */
    private ShipPositionState getCurrentState() {
        mic.LoggerUtil.logEntry(logger,"getCurrentState");
        ShipPositionState shipState = new ShipPositionState();
        shipState.x = getPosition().getX();
        shipState.y = getPosition().getY();
        shipState.angle = getRotator().getAngle();
        mic.LoggerUtil.logExit(logger,"getCurrentState");
        return shipState;
    }

    /**
     * Finds any maneuver paths related to the keystroke based on the map
     * keyStrokeToManeuver map
     *
     * @param keyStroke
     * @return
     */
    private ManeuverPaths getKeystrokePath(KeyStroke keyStroke) {
        mic.LoggerUtil.logEntry(logger,"getKeystrokePath");
        String hotKey = HotKeyConfigurer.getString(keyStroke);
        if (keyStrokeToManeuver.containsKey(hotKey)) {
            mic.LoggerUtil.logExit(logger,"getKeystrokePath");
            return keyStrokeToManeuver.get(hotKey);
        }
        mic.LoggerUtil.logExit(logger,"getKeystrokePath");
        return null;
    }

    private List<BumpableWithShape> getShipsWithShapes() {
        mic.LoggerUtil.logEntry(logger,"getShipsWithShapes");
        List<BumpableWithShape> ships = Lists.newLinkedList();
        for (BumpableWithShape ship : getShipsOnMap()) {
            if (getId().equals(ship.bumpable.getId())) {
                continue;
            }
            ships.add(ship);
        }
        mic.LoggerUtil.logExit(logger,"getShipsWithShapes");
        return ships;
    }

    private List<BumpableWithShape> getBumpablesWithShapes() {
        mic.LoggerUtil.logEntry(logger,"getBumpablesWithShapes");
        List<BumpableWithShape> bumpables = Lists.newLinkedList();
        for (BumpableWithShape bumpable : getBumpablesOnMap()) {
            if (getId().equals(bumpable.bumpable.getId())) {
                continue;
            }
            bumpables.add(bumpable);
        }
        mic.LoggerUtil.logExit(logger,"getBumpablesWithShapes");
        return bumpables;
    }

    private List<BumpableWithShape> getShipsOnMap() {
        mic.LoggerUtil.logEntry(logger,"getShipsOnMap");
        List<BumpableWithShape> ships = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("this_is_a_ship")) {
                ships.add(new BumpableWithShape((Decorator)piece, "Ship",
                        piece.getProperty("Pilot Name").toString(), piece.getProperty("Craft ID #").toString(),
                        this.getInner().getState().contains("this_is_2pointoh")));
            }
        }
        mic.LoggerUtil.logExit(logger,"getShipsOnMap");
        return ships;
    }

    private List<BumpableWithShape> getBumpablesOnMap() {
        mic.LoggerUtil.logEntry(logger,"getBumpablesOnMap");
        List<BumpableWithShape> bumpables = Lists.newArrayList();

        GamePiece[] pieces = getMap().getAllPieces();
        for (GamePiece piece : pieces) {
            if (piece.getState().contains("this_is_a_ship")) {
                bumpables.add(new BumpableWithShape((Decorator)piece,"Ship",
                        piece.getProperty("Pilot Name").toString(), piece.getProperty("Craft ID #").toString(),
                        this.getInner().getState().contains("this_is_2pointoh")));
            } else if (piece.getState().contains("this_is_an_asteroid")) {
                // comment out this line and the next three that add to bumpables if bumps other than with ships shouldn't be detected yet
                String testFlipString = "";
                try{
                    testFlipString = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
                } catch (Exception e) {}
                bumpables.add(new BumpableWithShape((Decorator)piece, "Asteroid", "2".equals(testFlipString),false));
            } else if (piece.getState().contains("this_is_a_debris")) {
                String testFlipString = "";
                try{
                    testFlipString = ((Decorator) piece).getDecorator(piece,piece.getClass()).getProperty("whichShape").toString();
                } catch (Exception e) {}
                bumpables.add(new BumpableWithShape((Decorator)piece,"Debris","2".equals(testFlipString),false));
            } else if (piece.getState().contains("this_is_a_bomb")) {
                bumpables.add(new BumpableWithShape((Decorator)piece, "Mine", false, false));
            }
        }
        mic.LoggerUtil.logExit(logger,"getBumpablesOnMap");
        return bumpables;
    }

    /**
     * Uses a FreeRotator unassociated with any game pieces with a 360 rotation limit
     * to convert the provided angle to the same angle the ship would be drawn at
     * by vassal
     *
     * @param angle
     * @return
     */
    private double convertAngleToGameLimits(double angle) {
        mic.LoggerUtil.logEntry(logger,"convertAngleToGameLimits");
        this.testRotator.setAngle(angle);
        mic.LoggerUtil.logExit(logger,"convertAngleToGameLimits");
        return this.testRotator.getAngle();

    }


    //1 = small, 2 = medium, 3 = large
    private int whichSizeShip(Decorator ship) {
        mic.LoggerUtil.logEntry(logger,"whichSizeShip");
        if(BumpableWithShape.getRawShape(ship).getBounds().getWidth() > 224) return 3;
        if(BumpableWithShape.getRawShape(ship).getBounds().getWidth() > 167) return 2;
        mic.LoggerUtil.logExit(logger,"whichSizeShip");
        return 1;
    }

    /*
    public static class CollisionVisualization extends Command implements Drawable {
        static final int NBFLASHES = 6;
        static final int DELAYBETWEENFLASHES = 250;

        private final List<Shape> shapes;
        private boolean tictoc = false;
        Color myO = new Color(255,99,71, 150);

        CollisionVisualization() {
            this.shapes = new ArrayList<Shape>();
        }

        CollisionVisualization(Shape shipShape) {
            this.shapes = new ArrayList<Shape>();
            this.shapes.add(shipShape);
        }

        protected void executeCommand() {
            final Timer timer = new Timer();
            final VASSAL.build.module.Map map = VASSAL.build.module.Map.getMapById("Map0");
            logger.info("Rendering CollisionVisualization command");
            this.tictoc = false;
            final AtomicInteger count = new AtomicInteger(0);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try{
                        if(count.getAndIncrement() >= NBFLASHES * 2) {
                            timer.cancel();
                            map.removeDrawComponent(CollisionVisualization.this);
                            return;
                        }
                        draw(map.getView().getGraphics(), map);
                    } catch (Exception e) {
                        logger.error("Error rendering collision visualization", e);
                    }
                }
            }, 0,DELAYBETWEENFLASHES);
        }

        protected Command myUndoCommand() {
            return null;
        }

        public void add(Shape bumpable) {
            this.shapes.add(bumpable);
        }

        public List<Shape> getShapes() {
            return this.shapes;
        }

        public void draw(Graphics graphics, VASSAL.build.module.Map map) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            if(tictoc == false)
            {
                graphics2D.setColor(myO);
                AffineTransform scaler = AffineTransform.getScaleInstance(map.getZoom(), map.getZoom());
                for (Shape shape : shapes) {
                    graphics2D.fill(scaler.createTransformedShape(shape));
                }
                tictoc = true;
            }
            else {
                map.getView().repaint();
                tictoc = false;
            }
        }

        public boolean drawAboveCounters() {
            return true;
        }
    }

    public static class CollsionVisualizationEncoder implements CommandEncoder {
        private static String commandPrefix = "CollisionVis=";

        public Command decode(String command) {
            if (command == null || !command.contains(commandPrefix)) {
                return null;
            }

            logger.info("Decoding CollisionVisualization");

            command = command.substring(commandPrefix.length());

            try {
                String[] newCommandStrs = command.split("\t");
                CollisionVisualization visualization = new CollisionVisualization();
                for (String bytesBase64Str : newCommandStrs) {
                    ByteArrayInputStream strIn = new ByteArrayInputStream(Base64.decodeBase64(bytesBase64Str));
                    ObjectInputStream in = new ObjectInputStream(strIn);
                    Shape shape = (Shape) in.readObject();
                    visualization.add(shape);
                    in.close();
                }
                logger.info("Decoded CollisionVisualization with {} shapes", visualization.getShapes().size());
                return visualization;
            } catch (Exception e) {
                logger.error("Error decoding CollisionVisualization", e);
                return null;
            }
        }

        public String encode(Command c) {
            if (!(c instanceof CollisionVisualization)) {
                return null;
            }
            logger.info("Encoding CollisionVisualization");
            CollisionVisualization visualization = (CollisionVisualization) c;
            try {
                List<String> commandStrs = Lists.newArrayList();
                for (Shape shape : visualization.getShapes()) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(bos);
                    out.writeObject(shape);
                    out.close();
                    byte[] bytes = bos.toByteArray();
                    String bytesBase64 = Base64.encodeBase64String(bytes);
                    commandStrs.add(bytesBase64);
                }
                return commandPrefix + Joiner.on('\t').join(commandStrs);
            } catch (Exception e) {
                logger.error("Error encoding CollisionVisualization", e);
                return null;
            }
        }
    }
*/
    private static class ShipPositionState {
        double x;
        double y;
        double angle;
    }

//    public static void main(String[] args) throws Exception {
//        CollisionVisualization visualization = new CollisionVisualization();
//        Path2D.Double path = new Path2D.Double();
//        path.moveTo(0.0, 0.0);
//        path.lineTo(0.0, 1.0);
//        visualization.add(path);
//        path = new Path2D.Double();
//        path.moveTo(0.0, 0.0);
//        path.lineTo(0.0, 0.10);
//        visualization.add(path);
//
//        CommandEncoder encoder = new CollsionVisualizationEncoder();
//
//        String encoded = encoder.encode(visualization);
//        System.out.println("encoded = " + encoded);
//
//        CollisionVisualization newVis = (CollisionVisualization) encoder.decode(encoded);
//        System.out.println("decoded = " + newVis.getShapes().size())     ;
//    }
}