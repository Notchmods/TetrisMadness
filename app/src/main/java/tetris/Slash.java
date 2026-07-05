package tetris;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.GameGrid;
import ch.aplu.jgamegrid.Location;

import java.util.ArrayList;

public class Slash extends Block {
    private final BlockPieces blockPiece = BlockPieces.Slash;
    private Location[][] relativeHighlightLocations = new Location[3][1];
    private Location[][] absoluteHighlightLocations = new Location[3][1];

    public Slash(Tetris tetris) {
        super(tetris);
        this.tetris=tetris;

        //Blocks location
        relativeHighlightLocations[0][0]=new Location(2,0);
        relativeHighlightLocations[1][0]=new Location(1,1);
        relativeHighlightLocations[2][0]=new Location(0,2);

        //render
        for (int i = 0; i < relativeHighlightLocations.length; i++)
            blocks.add(new TetroBlock(blockPiece.getBlockIndex(), relativeHighlightLocations[i]));

    }

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
            default:
                break;
        }
    }

    public String toString() {
        return "Block: " + blockPiece.getBlockName() + ". Location: " + getX() + "-" + getY() + ". Rotation: " + rotationId;
    }

    private int rotationId = 0;


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
}
