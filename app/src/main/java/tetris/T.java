// S.java
package tetris;

import ch.aplu.jgamegrid.*;

import java.util.ArrayList;

public class T extends Block {
    private Location[][] rotationLocation = new Location[4][4];
    private final BlockPieces blockPiece = BlockPieces.T;

    private Location[][] relativeHighlightLocations = new Location[3][2];
    private Location[][] absoluteHighlightLocations = new Location[3][2];

    T(Tetris tetris) {
        super(tetris);

        // rotationId 0
        rotationLocation[0][0] = new Location(new Location(0, 0));
        rotationLocation[1][0] = new Location(new Location(1, 0));
        rotationLocation[2][0] = new Location(new Location(2, 0));
        rotationLocation[3][0] = new Location(new Location(1, 1));
        // rotationId 1
        rotationLocation[0][1] = new Location(new Location(0, -1));
        rotationLocation[1][1] = new Location(new Location(0, 0));
        rotationLocation[2][1] = new Location(new Location(0, 1));
        rotationLocation[3][1] = new Location(new Location(-1, 0));
        // rotationId 2
        rotationLocation[0][2] = new Location(new Location(1, 0));
        rotationLocation[1][2] = new Location(new Location(0, 0));
        rotationLocation[2][2] = new Location(new Location(-1, 0));
        rotationLocation[3][2] = new Location(new Location(0, -1));
        // rotationId 3
        rotationLocation[0][3] = new Location(new Location(0, 1));
        rotationLocation[1][3] = new Location(new Location(0, 0));
        rotationLocation[2][3] = new Location(new Location(0, -1));
        rotationLocation[3][3] = new Location(new Location(1, 0));

        for (int i = 0; i < rotationLocation.length; i++)
            blocks.add(new TetroBlock(blockPiece.getBlockIndex(), rotationLocation[i]));

        relativeHighlightLocations[0][0] = new Location(new Location(0, 0));
        relativeHighlightLocations[1][0] = new Location(new Location(1, 0));
        relativeHighlightLocations[2][0] = new Location(new Location(2, 0));
        relativeHighlightLocations[0][1] = new Location(new Location(0, 1));
        relativeHighlightLocations[1][1] = new Location(new Location(1, 1));
        relativeHighlightLocations[2][1] = new Location(new Location(2, 1));
    }

    public String toString() {
        return "Block: " + blockPiece.getBlockName() + ". Location: " + getX() + "-" + getY() + ". Rotation: " + rotationId;
    }

    private int rotationId = 0;

    public void setAutoBlockMove(String autoBlockMove) {
        this.autoBlockMove = autoBlockMove;
    }

    /**
     * Turn on and off highlight to show the surrounding rectangle of a block
     * @param isHighlight turn on or off the highlight
     */
    public void hightlightLocation(boolean isHighlight) {
        for (int i = 0; i < relativeHighlightLocations.length; i++) {
            for (int j = 0; j < relativeHighlightLocations[i].length; j++) {
                if (isHighlight) {
                    Location highlightLocation = relativeHighlightLocations[i][j];
                    Location location = new Location(getX() + highlightLocation.getX(), getY() + highlightLocation.getY());
                    absoluteHighlightLocations[i][j] = location;
                }
                tetris.highlightLocations(absoluteHighlightLocations[i][j], isHighlight);
            }
        }
    }

    /**
     * Based on the input in the properties file, the block can move automatically
     */
    @Override
    public void autoMove() {

        //Blocks will fall once the move has been executed
        if (autoBlockIndex >= autoBlockMove.length()) {
            return;
        }

        //Grab movement from autoBlockMove
        char action = autoBlockMove.charAt(autoBlockIndex);
        autoBlockIndex++;
        switch (action) {
            case 'L':
                left();
                break;
            case 'R':
                right();
                break;
            case 'T':
                rotate();
                break;
            default:
                break;
        }

    }

    void rotate() {
        if (isStarting)
            return;

        int oldrotationId = rotationId; // Save it
        rotationId++;
        if (rotationId == 4)
            rotationId = 0;

        if (canRotate(rotationId)) {
            for (TetroBlock a : blocks) {
                Location loc = new Location(getX() + a.getRelativeLocation(rotationId).x, getY() + a.getRelativeLocation(rotationId).y);
                a.setLocation(loc);
            }
        } else
            rotationId = oldrotationId;  // Restore

    }

    private boolean canRotate(int rotationId) {
        // Check for every rotated tetroBlock within the tetrisBlock
        for (TetroBlock a : blocks) {
            int locationX = getX() + a.getRelativeLocation(rotationId).x;
            int locationY = getY() + a.getRelativeLocation(rotationId).y;
            Location loc = new Location(locationX, locationY);
            TetroBlock block =
                    (TetroBlock) (gameGrid.getOneActorAt(loc, TetroBlock.class));

            if (!tetris.isInsideBoundary(loc)) {
                // outside the grid boundary
                return false;
            }
            if (blocks.contains(block)) {
                // in same tetrisBlock->skip
                continue;
            }
            if (block != null) {
                // Another tetroBlock->not permitted
                return false;
            }
        }
        return true;
    }

}
