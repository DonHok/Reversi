package reversi.model;

/**
 * Enumeration that represents the 2 players, ai and human, that
 * actively play. Tie is a special case, used as a "wildcard".
 */
public enum Player {
    /**
     * Human player for a game.
     */
    HUMAN,

    /**
     * Computer player for a game.
     */
    AI,

    /**
     * Tie as non of the upper players used for a game.
     */
    TIE;
}