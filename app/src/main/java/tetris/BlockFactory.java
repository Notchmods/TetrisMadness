package tetris;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;

public class BlockFactory {

    public static Block BlockCreate(BlockPieces types, Tetris tetris) {
       switch(types){
           case I:
               return new I(tetris);
           case J:
               return new J(tetris);
           case L:
               return new L(tetris);
           case O:
               return new O(tetris);
           case S:
               return new S(tetris);
           case T:
               return new T(tetris);
           case Z:
               return new Z(tetris);
           case Cross:
               return new Cross(tetris);
           case Plus:
               return new Plus(tetris);
           case Slash:
               return new Slash(tetris);
           default:
               throw new IllegalStateException("Unexpected value: " + types);
       }
    }

    }