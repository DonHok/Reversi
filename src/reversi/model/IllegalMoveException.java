package reversi.model;

/**
 * Runtime exception used, when a move on a playing board should be executed,
 * that is not possible under the rules of the game.
 */
@SuppressWarnings("serial")
public class IllegalMoveException extends RuntimeException {

    /**
     * Initializes a new illegal move exception without any parameters.
     */
    public IllegalMoveException() {
        super();
    }

    /**
     * Initializes a new illegal move exception  with a String as additional
     * message.
     *
     * @param message The String, that will be a additional message.
     */
    public IllegalMoveException(String message) {
        super(message);
    }

}