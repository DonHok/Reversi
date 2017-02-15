package reversi.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import reversi.model.Board;
import reversi.model.Player;
import reversi.model.ReversiBoard;

/**
 * GUI for the Reversi game. Is a window that contains buttons for to edit
 * the a object that implements the {@code Board}-Interface and a grid with
 * the same size as the {@code Board}.
 * Starts the game with a default difficulty setting of 3 and the human player
 * as the starter.
 */
public class ReversiGui extends JFrame {

    /**
     * Final version ID of the GUI.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Maximum level supported by the game.
     */
    private final static int MAX_LEVEL = 5;

    /**
     * Minimum level supported by the game.
     */
    private final static int MIN_LEVEL = 1;

    /**
     * Board saved in this object, that will be used to execute the operation
     * issued via the the buttons on the GUI.
     * Initialized with the default setting.
     */
    private Board playingField = new ReversiBoard(Player.HUMAN);

    /**
     * Stack to save the last boards where the player has the turn, in order
     * to perform a undo-operation.
     */
    private Stack<Board> undoStack = new Stack<>();

    /**
     * Array of all slots on the GUI grid.
     */
    private Slot[] boardRepresentation = new Slot[Board.SIZE * Board.SIZE];

    /**
     * Label to visually represent the number of human owned tiles, that are
     * currently on the board.
     */
    private JLabel humanTiles = new JLabel();

    /**
     * Label to visually represent the number of ai owned tiles, that are
     * currently on the board.
     */
    private JLabel machineTiles = new JLabel();

    /**
     * Button that will perform the undo operation on the game.
     */
    private JButton undo;

    /**
     * Indicates if a additional thread to compute the machine move is running.
     */
    private boolean threadIsRunning = false;

    /**
     * Indicates if the game the GUI is using is won.
     */
    private boolean gameIsWon = false;

    /**
     * Thread, that will compute the next move the ai will do in the game.
     */
    private MachineThread aiComputation;

    /**
     * The ai level that is currently used in this game.
     */
    private volatile int currentLevel = 3;

    /**
     * Creates a new object of the {@code ReversiGUI} class, with a 850x850
     * pixel size, all buttons and the game model initialized and tooltips
     * and visible for the user.
     */
    public ReversiGui() {
        super("Reversi");
        JPanel upperNumberLabel = new JPanel();
        JPanel sideNumberLabel = new JPanel();
        JPanel boardGrid = new JPanel();
        JPanel taskBar = new JPanel();

        JComboBox<String> level = initializeLvl();

        JButton newComand = createButton("NEW", 'N',
                "Start a new game. ALT + N", new NewListener());
        JButton switchComand = createButton("SWITCH", 'S',
                "Switches starter and starts a new game. ALT + S",
                new SwitchListener());
        JButton quit = createButton("QUIT", 'Q', "Closes the program. ALT + Q",
                new QuitListener());
        undo = createButton("UNDO", 'U',
                "Nullifies the last human move. ALT + N", new UndoListener());
        taskBar.setLayout(new FlowLayout());
        attachComponents(taskBar, humanTiles, level, newComand, switchComand,
                undo, quit, machineTiles);

        humanTiles.setToolTipText("Number of human tiles on the field");
        humanTiles.setForeground(Color.BLUE);
        machineTiles.setToolTipText("Number of machine Tiles on the field");
        machineTiles.setForeground(Color.RED);

        boardGrid.setLayout(new GridLayout(Board.SIZE, Board.SIZE));
        initiateBoard(boardGrid);

        upperNumberLabel.setLayout(new GridLayout(1, Board.SIZE));
        upperNumberLabel.add(new JLabel(""));
        sideNumberLabel.setLayout(new GridLayout(Board.SIZE, 1));
        initiateNumberScale(upperNumberLabel, sideNumberLabel);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(upperNumberLabel, BorderLayout.NORTH);
        c.add(sideNumberLabel, BorderLayout.WEST);
        c.add(boardGrid, BorderLayout.CENTER);
        c.add(taskBar, BorderLayout.SOUTH);
        initiateNewGame(Player.HUMAN);
        updateCompleteField();
    }

    /**
     * Initiates a scale label that shows the numbers from 1..Board.SIZE.
     *
     * @param scaleLabel The JPanel object, that should be used as label.
     */
    private void initiateNumberScale(JPanel... scaleLabel) {
        for (int i = 1; i <= Board.SIZE; ++i) {
            for (JPanel label : scaleLabel) {
                label.add(new JLabel(Integer.toString(i)));
            }
        }
    }

    /**
     * Initiates the representation of the reversi board by adding
     * a slot(with its index) to each spot of the grid.
     *
     * @param grid JPanel with GridLayout and size of Board.SIZE x Board.SIZE.
     */
    private void initiateBoard(JPanel grid) {
        for (int i = 1; i <= Board.SIZE * Board.SIZE; i++) {
            int row = (int) Math.ceil(((double) i / Board.SIZE));
            int col = i - Board.SIZE * (row - 1);
            Slot toAdd = new Slot(row, col);
            toAdd.addMouseListener(new SlotMouseListener());
            boardRepresentation[i - 1] = toAdd;
            grid.add(toAdd);
        }
    }

    /**
     * Initializes a new drag and drop menu to change the level of the game.
     *
     * @return Drag and drop menu to change the level.
     */
    private JComboBox<String> initializeLvl() {
        JComboBox<String> toReturn = new JComboBox<>();
        for (int i = MIN_LEVEL; i <= MAX_LEVEL; i++) {
            toReturn.addItem("Level " + i);
        }
        toReturn.addActionListener(new LevelListener());
        toReturn.setToolTipText("Switches the AI level.");
        return toReturn;
    }

    /**
     * Updates the visual the visual representation of the board in the slots,
     * that were changed and upadtes the number of tiles each player has on the
     * board.
     */
    private void updateCompleteField() {
        humanTiles.setText("" + playingField.getNumberOfHumanTiles());
        machineTiles.setText("" + playingField.getNumberOfMachineTiles());
        int k = 0;

        // Go through the whole board and repaint every slot with a new owner.
        for (int i = 0; i < Board.SIZE; ++i) {
            for (int j = 0; j < Board.SIZE; ++j) {
                if (!boardRepresentation[k]
                        .isOwner(playingField.getSlot(i, j))) {
                    boardRepresentation[k].update(playingField.getSlot(i, j));
                }
                ++k;
            }
        }
    }

    /**
     * Starts a new game, by creating a new board object with the saved
     * parameters.
     *
     * @param starter The player, that will be the starter of the new game.
     */
    private void initiateNewGame(Player starter) {
        playingField = new ReversiBoard(starter);
        undoStack = new Stack<>();
        updateCompleteField();
        playingField.setLevel(currentLevel);
        gameIsWon = false;
        undo.setEnabled(false);
    }

    /**
     * Creates a new {@code JButton} with a name, a shortcut, a tool-
     * tip text and a listener for the action that will be performed
     * on this button.
     *
     * @param name     The name the new button should have.
     * @param mnemonic The shortcut(ALT+ ..) for this button.
     * @param toolTip  Tooltip text for the user, when he hovers over the button
     *                 with his mouse.
     * @param action   Listener that will be executed, if the user performs an
     *                 action on this button.
     * @return A new JButton objects with all these parameters set.
     */
    private JButton createButton(String name, char mnemonic, String toolTip,
                                 ActionListener action) {
        JButton creation = new JButton(name);
        creation.setMnemonic(mnemonic);
        creation.setToolTipText(toolTip);
        creation.addActionListener(action);
        return creation;
    }

    /**
     * Method that attaches any number of components to a {@code JPanel}.
     *
     * @param target The JPanel, the components should be attached onto.
     * @param toAdd  The components that should be attached onto the JPanel.
     */
    private void attachComponents(JPanel target, JComponent... toAdd) {
        for (JComponent addition : toAdd) {
            target.add(addition);
        }
    }

    /**
     * Executes a human move on the board model on the same position
     * as the slot, that was selected by the user in the GUI.
     * If the move is legit, it will be performed, the visual representation
     * will updated and a machine move follows.
     * Else a error message will be printed on the screen.
     *
     * @param tile The slot the user clicked.
     */
    private void makeHumanMove(Object tile) {

        // When the object that caused this action is a slot
        // get its position on the board and perform a move there.
        if (tile instanceof Slot) {
            Slot executingSlot = (Slot) tile;
            int row = executingSlot.getRow();
            int col = executingSlot.getCol();
            Board temporaryBoard = playingField.move(row - 1, col - 1);

            if (temporaryBoard == null) {
                JOptionPane.showMessageDialog(null,
                        "Illegal human move! Please try again.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                undoStack.add(playingField);
                undo.setEnabled(true);
                playingField = temporaryBoard;

                if (playingField.gameOver()) {
                    updateCompleteField();
                    checkWinner();
                } else {
                    updateCompleteField();
                    startAiMove();
                }

            }

        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Method to forcibly stops the computation of a machine move.
     */
    @SuppressWarnings("deprecation")
    private void abortMachineThread() {

        // Stop the thread, uses the deprecated API method.
        aiComputation.stop();

        threadIsRunning = false;
    }

    /**
     * Checks two boards for equality by comparing the owner of each
     * individual slot.
     *
     * @param left  The first board, that will be used for comparison.
     * @param right The second board, that will be used for comparison.
     * @return {@code True}, when each slot has the same owner in both boards.
     * Else {@code false}.
     */
    private static boolean checkEquality(Board left, Board right) {

        // Go through the whole board and compare each slot.
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
     * Checks if a game is won.
     * In case it is, a message informing the user about the winner will be
     * printed.
     *
     * @return {@code True}, if the game is won. Else {@code false}.
     */
    private boolean checkWinner() {
        if (playingField.gameOver()) {
            gameIsWon = true;
            Player winner = playingField.getWinner();

            if (winner.equals(Player.TIE)) {
                JOptionPane.showMessageDialog(null,
                        "The game has ended in a tie.", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (winner.equals(Player.AI)) {
                JOptionPane.showMessageDialog(null,
                        "The machine has won.", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (winner.equals(Player.HUMAN)) {
                JOptionPane.showMessageDialog(null,
                        "Congratulation! You have won!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                throw new IllegalArgumentException();
            }

        }
        return playingField.gameOver();
    }

    /**
     * {@code Listener} for a drag and drop menu, that will change the level
     * of a {@code Board} object.
     * Only supports level settings from {@code MIN_LVL} to {@code MAX_LVL}.
     */
    private class LevelListener implements ActionListener {

        /**
         * Upon selecting a new level in the drag and drop menu this
         * method will change the level of the board saved in the GUI
         * object to the new level the user selected.
         *
         * @param action Action performed by the user(selecting a new setting).
         */
        @Override
        public void actionPerformed(ActionEvent action) {
            Object source = action.getSource();

            // If the object that performed a action is a combo box
            // use the index(0 for level 5, 4 for 5) to get the level.
            if (source instanceof JComboBox) {

                // Due to not being able to test a the generic type of the
                // object and the generic type being of no importance, it
                // will be ignored in this case.
                @SuppressWarnings("rawtypes")
                JComboBox sourceBox = (JComboBox) source;
                int newLevel = sourceBox.getSelectedIndex();

                if (newLevel == -1) {
                    throw new IllegalArgumentException();
                } else {
                    newLevel = newLevel + 1;
                    currentLevel = newLevel;
                    playingField.setLevel(currentLevel);
                }

            } else {
                throw new IllegalArgumentException();
            }
        }

    }

    /**
     * {@code Listener} for a quit-button. Stops the whole program.
     */
    private class QuitListener implements ActionListener {

        /**
         * Stops the whole program, if a action on the button is performed.
         * If any additionally thread is still running, it will be stopped.
         *
         * @param action Action performed by the user on this button.
         */
        @Override
        public void actionPerformed(ActionEvent action) {

            if (threadIsRunning) {
                abortMachineThread();
            }

            dispose();
        }

    }

    /**
     * {@code Listener} for a new-button. Creates a new board object with
     * the same parameters that are currently set.
     */
    private class NewListener implements ActionListener {

        /**
         * Upon performing a action on the button this method
         * creates a new {@code Board} object and starts a ai move, if
         * the ai is the starter.
         * If a additional thread to compute the ai move on the old board, it
         * will be forcibly closed.
         *
         * @param action The action performed by the user on the button.
         */
        @Override
        public void actionPerformed(ActionEvent action) {

            if (threadIsRunning) {
                abortMachineThread();
            }

            initiateNewGame(playingField.getFirstPlayer());
            if (playingField.getFirstPlayer().equals(Player.AI)) {
                startAiMove();
            }
        }

    }

    /**
     * {@code Listener} for a slot on grid, that visually represents the board
     * of the game. Upon clicking a slot a human move will be attempted.
     */
    private class SlotMouseListener implements MouseListener {

        /**
         * Method that performs a action, if the player clicks on the slot.
         * In case the game is already over or ai currently has the turn a
         * error message will printed on the screen.
         * Else a human move will be attempted and the same spot in model as
         * slot has in the visual representation.
         *
         * @param action The mouse click on a certain slot in the grid.
         */
        @Override
        public void mouseClicked(MouseEvent action) {
            if (gameIsWon) {
                JOptionPane.showMessageDialog(null,
                        "The game is already over! Cant set any more tiles!",
                        "Error", JOptionPane.ERROR_MESSAGE);

            } else {

                if (threadIsRunning) {
                    JOptionPane.showMessageDialog(null,
                            "AI currently has the turn!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    makeHumanMove(action.getSource());
                }

            }
        }

        /**
         * Not in use.
         */
        public void mouseReleased(MouseEvent mouseEvent) {

        }

        /**
         * Not in use.
         */
        public void mouseEntered(MouseEvent mouseEvent) {

        }

        /**
         * Not in use.
         */
        public void mouseExited(MouseEvent mouseEvent) {

        }

        /**
         * Not in use.
         */
        public void mousePressed(MouseEvent mouseEvent) {

        }

    }

    /**
     * {@code Listener} used by a undo-button. 'Undos' the last
     * human move upon activating this button.
     */
    private class UndoListener implements ActionListener {

        /**
         * Method that is executed, when a action is performed on the button.
         * If a additional thread to compute the ai move is running, it will be
         * forcibly closed.
         * Undos the last move made by the human and disables the undo button,
         * if there are no more human moves left to undo.
         *
         * @param action The action performed by the user.
         */
        @Override
        public void actionPerformed(ActionEvent action) {

            if (threadIsRunning) {
                abortMachineThread();
            }

            playingField = undoStack.pop();

            if (undoStack.isEmpty()) {
                undo.setEnabled(false);
            }

            updateCompleteField();

            if (gameIsWon) {
                gameIsWon = false;
            }
        }

    }

    /**
     * {@code Listener} used by a switch-Button. Creates a new board and
     * switches the starting player.
     */
    private class SwitchListener implements ActionListener {

        /**
         * Method that is executed, when a action is performed on the button.
         * If a additional thread to compute the ai move is running, it will be
         * forcibly closed.
         * Swtiches the starting player of the game and start a new game.
         *
         * @param action Action performed by the user.
         */
        @Override
        public void actionPerformed(ActionEvent action) {

            if (threadIsRunning) {
                abortMachineThread();
            }

            if (playingField.getFirstPlayer().equals(Player.AI)) {
                initiateNewGame(Player.HUMAN);
            } else {
                initiateNewGame(Player.AI);
                startAiMove();
            }
        }

    }

    /**
     * Starts the computation of a machine move.
     */
    private void startAiMove() {
        threadIsRunning = true;
        aiComputation = new MachineThread();
        aiComputation.start();
    }

    /**
     * Class to compute the ai move in a additional thread.
     */
    private class MachineThread extends Thread {

        /**
         * Performs a machine move on a copy, replaces the board
         * in the GUI object with the result and issues a update on
         * the visual representation.
         * Adds task to the EventQueue in case the game is over or
         * the ai had to skip a turn to inform the user with a dialog.
         * Tests if the user has to skip the next turn and if this is the
         * case, it issues a dialog to inform a user and immediately performs
         * the next move.
         */
        @Override
        public void run() {
            if (threadIsRunning) {
                Board clone = playingField.clone();
                Board result = clone.machineMove();

                if (checkEquality(playingField, result)) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(null,
                                    "AI has to skip a turn!", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }

                playingField = result;
                playingField.setLevel(currentLevel);

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateCompleteField();
                    }
                });

                if (playingField.gameOver()) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            checkWinner();
                        }
                    });
                } else if (!testPossibleHumanMoves()) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(null,
                                    "Human has to skip a turn!", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    playingField = playingField.move(1, 1);
                    playingField.setLevel(currentLevel);
                    MachineThread nextTurn = new MachineThread();
                    nextTurn.run();
                }
                threadIsRunning = false;
            } else {
                throw new IllegalStateException();
            }
        }

        /**
         * Tests if the user can perform a move, by using the features of the
         * game, that a board can only stay the same if a player has to skip a
         * turn.
         */
        private boolean testPossibleHumanMoves() {
            Board temp = playingField.move(1, 1);
            return temp == null || !checkEquality(temp, playingField);
        }

    }

}