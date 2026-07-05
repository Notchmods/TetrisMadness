package tetris;// Tetris.java

import ch.aplu.jgamegrid.*;
import tetris.utility.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.awt.event.KeyEvent;
import java.awt.*;
import java.util.List;
import javax.print.DocFlavor;
import javax.swing.*;

public class Tetris extends JFrame implements GGActListener {
    public static final String statisticsFilePath = "statistics.txt";
    private Actor currentBlock = null;  // Currently active block
    private Actor blockPreview = null;   // block in preview window
    private int score = 0;
    private Random random = new Random(0);
    private Random feature2Random = new Random();

    private Map<String,Integer> block_played =new HashMap<>();

    private boolean isAuto = false;

    private int MANUAL_SIMULATION_PERIOD = 300;
    private int MANUAL_DROP_SIMULATION_PERIOD = 50;
    private int AUTO_SIMULATION_PERIOD = 50;

    private int NUM_BLOCK_TYPES = 10;

    // For testing mode, the block will be moved automatically based on the blockActions.
    // L is for Left, R is for Right, T is for turning (rotating), and D for down
    private String[] blockActions = null;
    private int blockActionIndex = 0;

    public String[] Locations;
    private int locationIndex=0;

    public String[] Speed;
    private int speedIndex=0;

    private String[] blockPieces = null;
    private int blockPieceIndex = 0;
    Logger logger = new Logger();

    private boolean feature1Active=false;
    private boolean feature2Active=false;

    /**
     * Initialise object
     */
    private void initWithProperties(Properties properties) {
        random = new Random(30006);
        isAuto = Boolean.parseBoolean(properties.getProperty("isAuto"));

        //Turns on or off features based on the property specs if auto tests is on
        if(isAuto){
            if(properties.getProperty("features.1").equals("Active")){
                feature1Active=true;
            }

            if(properties.getProperty("features.2").equals("Active")) {
                feature2Active =true;
            }
        }else{
            //If the game is manually controlled then all features are enabled
            feature1Active=true;
            feature2Active =true;
        }

        //Get speed
        String speeds= properties.getProperty("speed","");
        Speed= speeds.contains(";") ? speeds.split(";") : speeds.split(",");

        //Get properties related to actions, pieces and locations
        String blockActionProperty = properties.getProperty("actions", "");
        blockActions = blockActionProperty.split(",");

        String blockPieceProperty = properties.getProperty("pieces", "");
        blockPieces = blockPieceProperty.split(",");

        //Get location from properties
        String locationsCoord=properties.getProperty("locations");
        Locations=locationsCoord.split(";");
    }

    //Get next location
    public Location getNextLoc(){
        int x=0,y=0;
        if(locationIndex< Locations.length){
            String loc=Locations[locationIndex++];

            if(!loc.isEmpty()){
                String[] locPart=loc.split("-");
                x=Integer.parseInt(locPart[0]);
                y=Integer.parseInt(locPart[1]);
            }
        }
        //System.out.println("New Location"+x+y);
        return new Location(x,y);
    }

    public int getNextSpeed(){
        if(hasNextSpeed()){
            return Integer.parseInt(Speed[speedIndex++]);
        }
        return -1;
    }

    public boolean hasNextLocation(){
        return locationIndex< Locations.length&&!Locations[locationIndex].isEmpty();
    }

    public boolean hasNextSpeed(){
        return speedIndex< Speed.length&& !Speed[speedIndex].isEmpty();
    }

    public Tetris(Properties properties) {
        // Initialise value
        initWithProperties(properties);
        blockActionIndex = 0;


        for (int i=0; i<NUM_BLOCK_TYPES; i++){
            block_played.put(BlockPieces.values()[i].getBlockName(),0);
        }

        // Set up the UI components. No need to modify the UI Components
        tetrisComponents = new TetrisComponents();
        tetrisComponents.initComponents(this);
        gameGrid1.addActListener(this);

        gameGrid1.setSimulationPeriod(defaultSimulationPeriod());

        // Add the first block to start
        currentBlock = createRandomTetrisBlock();

        this.update_played(currentBlock);

        if(!isAuto){
            int limit = 12;
            if (currentBlock instanceof O) {
                limit = 14;
            }
            else if (!(currentBlock instanceof I)) {
                limit = 13;
            }
            int locationX = feature2Random.nextInt(limit);
            feature2Random = new Random();
            int locationY = feature2Random.nextInt(14);
            gameGrid1.addActor(currentBlock, new Location(locationX, locationY));
        }else{
            gameGrid1.addActor(currentBlock, getNextLoc());
        }


        gameGrid1.doRun();

        // Do not lose keyboard focus when clicking this window
        gameGrid2.setFocusable(false);
        setTitle("SWEN30006 Tetris Madness");
        score = 0;
        showScore(score);
    }

    /**
     * The game is called in a run loop, this method sleeps for 500 milliseconds
     * and only check if the game is over after 500 milliseconds
     */
    public String runApp() {
        setVisible(true);
        while (gameGrid1.isRunning()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(statisticsFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writer.println("Statistics File");
        record_stats(writer);
        writer.flush();
        return logger.getAllLog();
    }

    /**
     * Get the next block. If there is a value in the properties file,
     * getting the next available block in the list, otherwise getting a random block
     *
     * @return BlockPieces
     */
    private BlockPieces getNextBlock() {
        if (blockPieceIndex < blockPieces.length) {
            BlockPieces piece = BlockPieces.getBlockPiece(blockPieces[blockPieceIndex++]);
            return piece;
        }
        int rnd;
        if(feature1Active||!isAuto){
            rnd=random.nextInt(10);
        }else{
            rnd=random.nextInt(7);
        }

        return BlockPieces.values()[rnd];
    }

    /**
     * create a block and assign to a preview mode
     */
    Actor createRandomTetrisBlock() {
        feature2Random = new Random();
        if (blockPreview != null)
            blockPreview.removeSelf();

        // If the game is in auto test mode, then the block will be moved according to the blockActions
        String currentBlockMove = "";
        if (blockActions.length > blockActionIndex) {
            currentBlockMove = blockActions[blockActionIndex];
        }

        blockActionIndex++;
        Actor t = null;
        BlockPieces randomBlockPiece = getNextBlock();
        //System.out.println("randomBlockPiece = " + randomBlockPiece);

        t = BlockFactory.BlockCreate(randomBlockPiece,this);
        if (isAuto) {
            ((Block) t).setAutoBlockMove(currentBlockMove);
        }

        Block previewBlock = BlockFactory.BlockCreate(randomBlockPiece,this);
        previewBlock.display(gameGrid2, new Location(2, 1));
        blockPreview = previewBlock;

        //Invoke Runtime exceptions if blocks are null
        if(t==null){
            throw new RuntimeException("No block created for piece \n"+randomBlockPiece);
        }
        return t;
    }

    /**
     * highlight for a specific location. Check and use the appropriate color
     *
     * @param location
     * @param highlight
     */
    void highlightLocations(Location location, boolean highlight) {
        if (highlight) {
            gameGrid1.getBg().fillCell(location, Color.YELLOW);
        } else {
            gameGrid1.setGridColor(new java.awt.Color(255, 3, 0));
        }
    }

    private int defaultSimulationPeriod() {
        return isAuto ? AUTO_SIMULATION_PERIOD : MANUAL_SIMULATION_PERIOD;
    }

    public void moveToNextTetris(Actor t) {
        currentBlock = t;
        gameGrid1.setSimulationPeriod(defaultSimulationPeriod());
        this.update_played(t);
    }

    public void speedup() {
        gameGrid1.setSimulationPeriod(MANUAL_DROP_SIMULATION_PERIOD);
    }

    /**
     * Check if a specific location is within the game grid
     *
     * @param location
     * @return
     */
    public boolean isInsideBoundary(Location location) {
        if (location.getX() < 0) {
            return false;
        }

        if (location.getX() >= gameGrid1.getNbHorzCells()) {
            return false;
        }
        return true;
    }

    /**
     * Handle user input to move block. Arrow left to move left,
     * Arrow right to move right, Arrow up to rotate and
     * Arrow down for going down
     */
    private void moveBlock(int keyEvent) {
        if (currentBlock instanceof I) {
            switch (keyEvent) {
                case KeyEvent.VK_UP:
                    ((I) currentBlock).rotate();
                    break;
                case KeyEvent.VK_LEFT:
                    ((I) currentBlock).left();
                    break;
                case KeyEvent.VK_RIGHT:
                    ((I) currentBlock).right();
                    break;
                case KeyEvent.VK_DOWN:
                    ((I) currentBlock).drop();
                    break;
                default:
                    return;
            }
        } else if (currentBlock instanceof J) {
            switch (keyEvent) {
                case KeyEvent.VK_UP:
                    ((J) currentBlock).rotate();
                    break;
                case KeyEvent.VK_LEFT:
                    ((J) currentBlock).left();
                    break;
                case KeyEvent.VK_RIGHT:
                    ((J) currentBlock).right();
                    break;
                case KeyEvent.VK_DOWN:
                    ((J) currentBlock).drop();
                    break;
                default:
                    return;
            }
        } else if (currentBlock instanceof L) {
            switch (keyEvent) {
                case KeyEvent.VK_UP:
                    ((L) currentBlock).rotate();
                    break;
                case KeyEvent.VK_LEFT:
                    ((L) currentBlock).left();
                    break;
                case KeyEvent.VK_RIGHT:
                    ((L) currentBlock).right();
                    break;
                case KeyEvent.VK_DOWN:
                    ((L) currentBlock).drop();
                    break;
                default:
                    return;
            }
        } else if (currentBlock instanceof O) {
            switch (keyEvent) {
                case KeyEvent.VK_UP:
                    ((O) currentBlock).rotate();
                    break;
                case KeyEvent.VK_LEFT:
                    ((O) currentBlock).left();
                    break;
                case KeyEvent.VK_RIGHT:
                    ((O) currentBlock).right();
                    break;
                case KeyEvent.VK_DOWN:
                    ((O) currentBlock).drop();
                    break;
                default:
                    return;
            }
        } else if (currentBlock instanceof S) {
            switch (keyEvent) {
                case KeyEvent.VK_UP:
                    ((S) currentBlock).rotate();
                    break;
                case KeyEvent.VK_LEFT:
                    ((S) currentBlock).left();
                    break;
                case KeyEvent.VK_RIGHT:
                    ((S) currentBlock).right();
                    break;
                case KeyEvent.VK_DOWN:
                    ((S) currentBlock).drop();
                    break;
                default:
                    return;
            }
        } else if (currentBlock instanceof T) {
            switch (keyEvent) {
                case KeyEvent.VK_UP:
                    ((T) currentBlock).rotate();
                    break;
                case KeyEvent.VK_LEFT:
                    ((T) currentBlock).left();
                    break;
                case KeyEvent.VK_RIGHT:
                    ((T) currentBlock).right();
                    break;
                case KeyEvent.VK_DOWN:
                    ((T) currentBlock).drop();
                    break;
                default:
                    return;
            }
        } else if (currentBlock instanceof Z) {
            switch (keyEvent) {
                case KeyEvent.VK_UP:
                    ((Z) currentBlock).rotate();
                    break;
                case KeyEvent.VK_LEFT:
                    ((Z) currentBlock).left();
                    break;
                case KeyEvent.VK_RIGHT:
                    ((Z) currentBlock).right();
                    break;
                case KeyEvent.VK_DOWN:
                    ((Z) currentBlock).drop();
                    break;
                default:
                    return;
            }
        } else if (currentBlock instanceof Cross) {
            switch (keyEvent) {
                case KeyEvent.VK_LEFT:
                    ((Cross) currentBlock).left();
                    break;
                case KeyEvent.VK_RIGHT:
                    ((Cross) currentBlock).right();
                    break;
                case KeyEvent.VK_DOWN:
                    ((Cross) currentBlock).drop();
                    break;
                default:
                    return;
            }
        }
        else if (currentBlock instanceof Plus) {
            switch (keyEvent) {
                case KeyEvent.VK_LEFT:
                    ((Plus) currentBlock).left();
                    break;
                case KeyEvent.VK_RIGHT:
                    ((Plus) currentBlock).right();
                    break;
                case KeyEvent.VK_DOWN:
                    ((Plus) currentBlock).drop();
                    break;
                default:
                    return;
            }

        }
        else if (currentBlock instanceof Slash) {
            switch (keyEvent) {
                case KeyEvent.VK_LEFT:
                    ((Slash) currentBlock).left();
                    break;
                case KeyEvent.VK_RIGHT:
                    ((Slash) currentBlock).right();
                    break;
                case KeyEvent.VK_DOWN:
                    ((Slash) currentBlock).drop();
                    break;
                default:
                    return;
            }

        }
    }

    /**
     * The game is called in a run loop, this method for a tetris is called every
     * 1/30 seconds as the starting point
     */
    public void act() {
        removeFilledLine();
        moveBlock(gameGrid1.getKeyCode());
    }

    /**
     * Check if a line is completely filled and clear the line and update the score
     */
    private void removeFilledLine() {
        for (int y = 0; y < gameGrid1.nbVertCells; y++) {
            boolean isLineComplete = true;
            TetroBlock[] blocks = new TetroBlock[gameGrid1.nbHorzCells];   // One line
            // Calculate if a line is complete
            for (int x = 0; x < gameGrid1.nbHorzCells; x++) {
                blocks[x] =
                        (TetroBlock) gameGrid1.getOneActorAt(new Location(x, y), TetroBlock.class);
                if (blocks[x] == null) {
                    isLineComplete = false;
                    break;
                }
            }
            if (isLineComplete) {
                // If a line is complete, we remove the component block of the shape that belongs to that line
                for (int x = 0; x < gameGrid1.nbHorzCells; x++)
                    gameGrid1.removeActor(blocks[x]);
                ArrayList<Actor> allBlocks = gameGrid1.getActors(TetroBlock.class);
                for (Actor a : allBlocks) {
                    int z = a.getY();
                    if (z < y)
                        a.setY(z + 1);
                }
                gameGrid1.refresh();
                score++;
                showScore(score);
                logger.logEvent("Score: " + score);
            }
        }
    }

    /**
     * Show Score
     */
    private void showScore(final int score) {
        scoreText.setText(score + " points");
    }

    /**
     * Display the game over
     */
    void gameOver() {
        gameGrid1.addActor(new Actor("sprites/gameover.gif"), new Location(5, 5));
        gameGrid1.doPause();
        if (isAuto) {
            gameGrid1.doPause();
        }


    }

    /**
     * Start a new game
     */
    public void startBtnActionPerformed(java.awt.event.ActionEvent evt) {
        gameGrid1.doPause();
        gameGrid1.removeAllActors();
        gameGrid2.removeAllActors();
        gameGrid1.refresh();
        gameGrid2.refresh();
        gameGrid2.delay(getDelayTime());
        blockActionIndex = 0;
        currentBlock = createRandomTetrisBlock();
        if (!isAuto) { 
            int limit = 12;
            if (currentBlock instanceof O) {
                limit = 14;
            }
            else if (!(currentBlock instanceof I)) {
                limit = 13;
            }
            int locationX = feature2Random.nextInt(limit);
            feature2Random = new Random();
            int locationY = feature2Random.nextInt(14);
            gameGrid1.addActor(currentBlock, new Location(locationX, locationY));
        } 
        else {
            gameGrid1.addActor(currentBlock, getNextLoc());
        }
        gameGrid1.doRun();
        gameGrid1.requestFocus();
        score = 0;
        showScore(score);
    }

    private int getDelayTime() {
        if (isAuto) {
            return 500;
        } else {
            return 2000;
        }
    }

    public void update_played(Actor nextTetrisBlock){
        String cur_name= nextTetrisBlock.getClass().getName().split("\\.")[1];

        cur_name = switch (cur_name) {
            case "Cross" -> "X";
            case "Plus" -> "+";
            case "Slash" -> "/";
            default -> cur_name;
        };

        block_played.compute(cur_name,(k,v) ->v+1);

    }

    private void record_stats(PrintWriter statistics) {

        String name;
        statistics.println(String.format("Score: %d",score));
        for (BlockPieces block :  BlockPieces.values()){
            name =block.getBlockName();
            statistics.println(String.format("%s: %d",name,block_played.get(name)));
        }

    }

    public boolean getFeature1(){
        return feature1Active;
    }

    public boolean getFeature2(){
        return feature2Active;
    }

    public boolean getIsAuto(){
        return isAuto;
    }

    public int getSpeedIndex(){
        return speedIndex;
    }

    // AUTO GENERATED - do not modify//GEN-BEGIN:variables
    public ch.aplu.jgamegrid.GameGrid gameGrid1;
    public ch.aplu.jgamegrid.GameGrid gameGrid2;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JPanel jPanel2;
    public javax.swing.JPanel jPanel3;
    public javax.swing.JPanel jPanel4;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JTextArea jTextArea1;
    public javax.swing.JTextField scoreText;
    public javax.swing.JButton startBtn;
    private TetrisComponents tetrisComponents;
    // End of variables declaration//GEN-END:variables

}
