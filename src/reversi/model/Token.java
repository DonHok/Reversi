package reversi.model;

/**
 * Enumeration to represent the token pieces on a board.
 */
enum Token {

    /**
     * Token that dosen't occupy any space.
     */
    FREE,

    /**
     * Blue token on a board.
     */
    BLUE,

    /**
     * Red token on a board.
     */
    RED;

    /**
     * The player, who 'owns' the token.
     */
    private Player attachedPlayer;

    /**
     * Changes ownership of a color. Free spaces can't be owned by a player.
     *
     * @param toAttach The player, that claims ownership.
     */
    void attachPlayer(Player toAttach) {
        if (!this.equals(FREE)) {
            attachedPlayer = toAttach;
        }
    }

    /**
     * Returns the owner of the token.
     *
     * @return Owner of the token.
     */
    Player getPlayer() {
        if (!this.equals(FREE)) {
            return attachedPlayer;
        } else {
            return null;
        }
    }
}
