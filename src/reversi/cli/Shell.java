package reversi.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Scanner;

import reversi.model.ReversiBoard;
import reversi.model.Board;
import reversi.model.Player;

/**
 * User interface to play the game Reversi(Othello) via the system Shell
 * against an AI.
 */
public final class Shell {

    /**
     * Minimum level of the AI look ahead.
     */
    private static final int MIN_LVL = 1;

    /**
     * Maximum level of the AI look ahead.
     */
    private static final int MAX_LVL = 5;

    /**
     * Lowest index of a slot on the Reversi board.
     */
    private static final int MIN_INDEX = 1;

    /**
     * Level of AI look ahead that is currently set by the user.
     */
    private static int currentLevel = 3;

    /**
     * The {@code Board} object where all operations are executed.
     */
    private static Board playingBoard;

    /**
     * Variable that indicates if the game is already over.
     */
    private static boolean gameIsWon;

    /**
     * Variable that indicates if it is the AI's turn.
     */
    private static boolean aiHasTurn;

    /**
     * Private constructor to ensure no Shell object can be initialized.
     */
    private Shell() {
    }

    /**
     * {@code Main} method that is used as a entry point when starting the
     * program. Reads the commands the user types into the Shell and executes
     * them on the Reversi Board.
     *
     * @param args Default values given to the shell at start. Not in use.
     * @throws IOException Input exception to be handled by the OS.
     */
    public static void main(String[] args) throws IOException {
        BufferedReader shellInput = new BufferedReader(new InputStreamReader(
                System.in));
        boolean quitExecution = false;
        playingBoard = new ReversiBoard(Player.HUMAN);
        playingBoard.setLevel(currentLevel);

        while (!quitExecution) {
            if (aiHasTurn && !gameIsWon) {
                aiTurn();
            }
            System.out.print("othello> ");
            Scanner userInput = new Scanner(shellInput.readLine());
            userInput.useDelimiter("\\s+");

            if (userInput.hasNext()) {
                char firstLetter;
                String command = userInput.next();
                command = command.toLowerCase();
                firstLetter = command.charAt(0);

                switch (firstLetter) {
                    case 'l':
                        cmdLevel(userInput);
                        break;
                    case 'm':
                        cmdMove(userInput);
                        break;
                    case 'n':
                        cmdNew(userInput);
                        break;
                    case 'h':
                        printHelp();
                        break;
                    case 's':
                        cmdSwitch(userInput);
                        break;
                    case 'p':
                        cmdPrint(userInput);
                        break;
                    case 'q':
                        quitExecution = !hasAdditionalInput(userInput);
                        break;
                    default:
                        errorMessage("Invalid command");
                        break;
                }

            } else {
                errorMessage("No valid Input!");
            }
            userInput.close();
        }

    }

    /**
     * Takes a String, attaches it to an error message and prints that
     * message into the Shell.
     *
     * @param message The String that should be attached to the error message.
     */
    private static void errorMessage(String message) {
        System.out.println("Error! " + message);
    }

    /**
     * Method to test if there are additional inputs in a Scanner. Prints
     * a error message, when additional input is present.
     *
     * @param input The Scanner with the user input that has to be tested.
     * @return {@code True}, when additional input is present.
     * Else {@code false}.
     */
    private static boolean hasAdditionalInput(Scanner input) {
        if (input.hasNext()) {
            errorMessage("No additional parameters allowed");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Initializes a new Reversi board with the same parameters as the
     * board used before.
     *
     * @param userInput Scanner, that will be checked for additional input.
     */
    private static void cmdNew(Scanner userInput) {
        if (!hasAdditionalInput(userInput)) {
            Player currentStarter = playingBoard.getFirstPlayer();
            playingBoard = new ReversiBoard(currentStarter);
            playingBoard.setLevel(currentLevel);
            gameIsWon = false;
            aiHasTurn = currentStarter.equals(Player.AI);
        }
    }

    /**
     * Takes a user input String and searches the String for 2 numbers to
     * perform a human move. Index starts at 0.
     *
     * @param userInput The user input that will be used.
     */
    private static void cmdMove(Scanner userInput) {
        if (gameIsWon) {
            errorMessage("Cant execute a move on a won Board");
        } else if (!userInput.hasNextInt()) {
            errorMessage("A int is needed for this command");
        } else {

            int row = userInput.nextInt();
            if (!userInput.hasNextInt()) {
                errorMessage("A int is needed for this command");
            } else {
                int column = userInput.nextInt();
                if (row > ReversiBoard.SIZE || row < MIN_INDEX
                        || column > ReversiBoard.SIZE || column < MIN_INDEX) {
                    errorMessage("Parameters are not within the board");
                } else if (!hasAdditionalInput(userInput)) {
                    humanTurn(row - 1, column - 1);
                }
            }

        }
    }

    /**
     * Changes the level of the board that is used by searching
     * a input String for a number.
     *
     * @param userInput The user input that will be searched.
     */
    private static void cmdLevel(Scanner userInput) {
        if (!userInput.hasNextInt()) {
            errorMessage("A int is needed for this command");
        } else {

            int level = userInput.nextInt();
            if (level > MAX_LVL || level < MIN_LVL) {
                errorMessage("This level setting is not supported");
            } else if (!hasAdditionalInput(userInput)) {
                currentLevel = level;
                playingBoard.setLevel(currentLevel);
            }

        }
    }

    /**
     * Switches the user that will make the opening move of the game(Human
     * or AI) and initializes a new board.
     *
     * @param userInput User input that will be searched for additional input.
     */
    private static void cmdSwitch(Scanner userInput) {
        if (!hasAdditionalInput(userInput)) {
            Player currentStarter = playingBoard.getFirstPlayer();

            if (currentStarter.equals(Player.AI)) {
                playingBoard = new ReversiBoard(Player.HUMAN);
                aiHasTurn = false;
            } else {
                playingBoard = new ReversiBoard(Player.AI);
                aiHasTurn = true;
            }

            playingBoard.setLevel(currentLevel);
            gameIsWon = false;
        }
    }

    /**
     * Prints the String representation of the Board.
     *
     * @param userInput user input that will be searched for additional input.
     */
    private static void cmdPrint(Scanner userInput) {
        if (!hasAdditionalInput(userInput)) {
            System.out.println(playingBoard.toString());
        }
    }

    /**
     * Executes a human move on the board. And checks the result for validity
     * of the move, game over. Uses the index of the Board.
     *
     * @param row Index of the row, where the new token will be set.
     * @param col Index of the column, where the new token will be set.
     */
    private static void humanTurn(int row, int col) {
        Board executed = playingBoard.move(row, col);

        if (executed != null && executed.gameOver()) {
            winMessage(executed);
            playingBoard = executed;
            aiHasTurn = true;
        } else {
            if (executed == null) {
                row = row + 1;
                col = col + 1;
                errorMessage("Invalid move at " + "("
                        + row + ", " + col + ")");
                aiHasTurn = false;
            } else {
                if (checkEquality(executed, playingBoard)) {
                    System.out.println("Human has to miss a turn");
                }
                playingBoard = executed;
                aiHasTurn = true;
            }
        }

    }

    /**
     * Executes a move by the AI. The move will be calculated by using
     * the board´s algorithm and the level setting.
     */
    private static void aiTurn() {
        Board executed = playingBoard.machineMove();

        if (executed.gameOver()) {
            winMessage(executed);
        }

        if (checkEquality(executed, playingBoard)) {
            System.out.println("The bot has to miss a turn");
        }

        playingBoard = executed;
        aiHasTurn = false;
    }

    /**
     * Prints a win message depending.
     *
     * @param withWiner Board with a game over state.
     */
    private static void winMessage(Board withWiner) {
        if (withWiner.getWinner().equals(Player.HUMAN)) {
            System.out.println("You have won!");
        } else if (withWiner.getWinner().equals(Player.AI)) {
            System.out.println("Machine has won.");
        } else {
            System.out.println("Tie game!");
        }
        gameIsWon = true;
    }

    /**
     * Checks to boards for their equality by comparing each spot individually.
     *
     * @param left  The first board that will be used to compare.
     * @param right The second board that will be used to compare.
     * @return {@code true}, when they are equal, else {@code false}.
     */
    private static boolean checkEquality(Board left, Board right) {
        for (int i = 0; i < Board.SIZE; ++i) {
            for (int j = 0; j < Board.SIZE; ++j) {
                if (!(left.getSlot(i, j) == (right.getSlot(i, j)))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Prints text to give the user a detailed documentation upon using the
     * 'HELP' command.
     */
    private static void printHelp() {
        System.out.println("Reversi UI\n"
                + "This program dosent allow additional  "
                + "parameters except HELP \n"
                + "Following commands are available"
                + "(in lower- and uppercase)\n" + "NEW - starts new game \n"
                + "LEVEL lvl - sets difficulty \n"
                + "MOVE row col - places a token at the position\n"
                + "SWITCH - starts a new game and switches the "
                + "player order \n" + "PRINT - prints a visual representation "
                + "of the board \n" + "HELP - Help text \n"
                + "QUIT - end the programm");
    }

}