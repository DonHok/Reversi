package reversi.gui;

import javax.swing.JFrame;

/**
 * Degenerate class that only serves as entry point to execute the GUI.
 */
public final class ReversiExecution {

    /**
     * Private constructor to ensure no object can be initialized.
     */
    private ReversiExecution() {
    }

    /**
     * {@code Main} method that is used as a entry point when starting the
     * program. Creates a new GUI for the reversi game.
     *
     * @param args Default values given to the shell at start. Not in use.
     */
    public static void main(String[] args) {
        ReversiGui test = new ReversiGui();
        test.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        test.setSize(850, 850);
        test.setVisible(true);
    }

}