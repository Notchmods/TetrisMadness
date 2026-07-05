package tetris;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.GameGrid;
import ch.aplu.jgamegrid.Location;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public abstract class Block extends Actor{

    protected Tetris tetris;
    public boolean isStarting=true;
    private int nb;
    protected ArrayList<TetroBlock> blocks;
    private Actor nextTetrisBlock;
    protected String autoBlockMove;
    protected int autoBlockIndex;
    private int fallSpeed;

    public void setAutoBlockMove(String autoBlockMove) {
        this.autoBlockMove = autoBlockMove;
    }


    public Block(Tetris tetris) {
        super();
        this.tetris=tetris;
        blocks=new ArrayList<>();
        fallSpeed = tetris.getNextSpeed();
    }

    /**
     * The game is called in a run loop, this method for a block is called every 1/30 seconds as the starting point
     */
    public void act(){
        if (isStarting) {
            for (TetroBlock a : blocks) {
                Location loc =
                        new Location(getX() + a.getRelativeLocation(0).x, getY() + a.getRelativeLocation(0).y);
                gameGrid.addActor(a, loc);
            }
            hightlightLocation(true);
            isStarting = false;
            nb = 0;
        } else if (canAutoPlay()) {
            autoMove();
        } else {
                setDirection(90);
                if (nb == 1)
                    nextTetrisBlock = tetris.createRandomTetrisBlock();

                if (fallSpeed == -1) {
                    fallSpeed = new Random().nextInt(2) + 1;
                }
                for (int i = 0; i < fallSpeed - 1; i++) {advance();}
                    if (!advance()) {
                        if (nb == 0)  // Game is over when tetrisBlock cannot fall down
                            tetris.gameOver();
                        else {
                            tetris.logger.logEvent(toString()); //Log the final position of an object
                            setActEnabled(false);
                            //System.out.println("nextTetrisBlock = " + nextTetrisBlock)
                            //If game isn't in auto mode anymore and uses up initial loc then randomly spawn it
                            if(!tetris.getIsAuto() || !tetris.hasNextLocation()){
                                //If feature 2 is disabled
                                if(!tetris.getFeature2()){
                                    //Original spawn point
                                    gameGrid.addActor(nextTetrisBlock, new Location(6,0));

                                }else{
                                    int limit = 12;
                                    if (nextTetrisBlock instanceof O) {
                                    limit = 14;
                                    }
                                    else if (!(nextTetrisBlock instanceof I)) {
                                        limit = 13;
                                    }
                                    int locationX = new Random().nextInt(limit);
                                    int locationY = new Random().nextInt(14);
                                    gameGrid.addActor(nextTetrisBlock, new Location(locationX, locationY));
                                }
                            }else{
                                gameGrid.addActor(nextTetrisBlock,tetris.getNextLoc());
                            }
                            tetris.moveToNextTetris(nextTetrisBlock);
                            fallSpeed = tetris.getNextSpeed();
                        }
                    }
                nb++;
            }

        if (nb == 4) {
            hightlightLocation(false);
        }
    }


    /**
     * Based on the input in the properties file, the block can move automatically
     */
    public abstract void autoMove();

    /**
     * Turn on and off highlight to show the surrounding rectangle of a block
     * @param isHighlight turn on or off the highlight
     */
    public abstract void hightlightLocation(boolean isHighlight);

    /**
     * Check if the block can be played automatically based on the properties file
     */
    private boolean canAutoPlay() {
        if (autoBlockMove == null || autoBlockMove.isEmpty() || autoBlockMove.equals("-")) {
            return false;
        }
        return autoBlockIndex < autoBlockMove.length();
    }

    void display(GameGrid gg, Location location) {
        for (TetroBlock a : blocks) {
            Location loc =
                    new Location(location.x + a.getRelativeLocation(0).x, location.y + a.getRelativeLocation(0).y);
            gg.addActor(a, loc);
        }
    }

    // Actual actions on the block: move the block left, right, drop and rotate the block
    void left() {
        if (isStarting)
            return;
        setDirection(180);
        advance();
    }

    void right() {
        if (isStarting)
            return;
        setDirection(0);
        advance();
    }

    void drop() {
        if (isStarting)
            return;
        tetris.speedup();
    }

    // Logic to check if the block has been removed (as winning a line) or drop to the bottom
    private boolean advance() {
        boolean canMove = false;
        for (TetroBlock a : blocks) {
            if (!a.isRemoved()) {
                canMove = true;
            }
        }
        for (TetroBlock a : blocks) {
            if (a.isRemoved())
                continue;
            if (!gameGrid.isInGrid(a.getNextMoveLocation())) {
                canMove = false;
                break;
            }
        }

        for (TetroBlock a : blocks) {
            if (a.isRemoved())
                continue;
            TetroBlock block =
                    (TetroBlock) (gameGrid.getOneActorAt(a.getNextMoveLocation(),
                            TetroBlock.class));
            if (block != null && !blocks.contains(block)) {
                canMove = false;
                break;
            }
        }

        if (canMove) {
            move();
            return true;
        }
        return false;
    }

    /**
     * Override Actor.setDirection()
     */
    public void setDirection(double dir) {
        super.setDirection(dir);
        for (TetroBlock a : blocks)
            a.setDirection(dir);
    }

    /**
     * Override Actor.move()
     */
    public void move() {
        if (isRemoved())
            return;
        super.move();
        for (TetroBlock a : blocks) {
            if (a.isRemoved())
                break;
            a.move();
        }
    }

    /**
     * Override Actor.removeSelf()
     */
    public void removeSelf() {
        super.removeSelf();
        for (TetroBlock a : blocks)
            a.removeSelf();
    }




}
