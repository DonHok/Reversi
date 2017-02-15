package reversi.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import javax.swing.JPanel;
import reversi.model.Player;
import reversi.model.Board;

/**
 * Class to represent a slot on grid for a board game. It saves informations
 * about its owner and position and can be painted to visually represent
 * the player that owns the slot.(Plain green for no owner, green with red
 * circle for AI and green with blue circle for human)
 */
class Slot extends JPanel {

    /**
     * Version number of the final version of the slot object.
     */
    private static final long serialVersionUID = 1L;

    /** The player, that owns this tile. */
    private Player owner;

    /** The row position this slot has on a grid. */
    private int row;

    /** The column position this slot has on a grid. */
    private int column;

    /**
     * Paints this slot with a green background and a black border. When a
     * player has ownership over this slot, a token in his color will be painted
     * in the middle of the slot.
     *
     * @param visuals The basic graphics component for this slot to do basic
     *                rendering.
     */
    @Override
    protected void paintComponent(Graphics visuals) {
        super.paintComponent(visuals);
        int maxHeight = getHeight();
        int maxWidth = getWidth();
        Graphics2D visuals2D = (Graphics2D) visuals;
        visuals2D.setColor(Color.GREEN);
        visuals2D.fillRect(0, 0, maxWidth, maxHeight);
        visuals2D.setColor(Color.BLACK);
        visuals2D.drawLine(0, 0, maxWidth, 0);
        visuals2D.drawLine(0, 0, 0, maxHeight);
        visuals2D.drawLine(maxWidth, maxHeight, maxWidth, 0);
        visuals2D.drawLine(maxWidth, maxHeight, maxWidth, 0);
        Ellipse2D circle = new Ellipse2D.Float(5, 5, maxWidth - 10,
                maxHeight - 10);

        if (owner == Player.AI) {
            visuals2D.setColor(Color.RED);
            visuals2D.fill(circle);
        } else if (owner == Player.HUMAN) {
            visuals2D.setColor(Color.BLUE);
            visuals2D.fill(circle);
        }

    }

    /**
     * Initializes a new object of the slot class with a certain row and
     * column position it has within a grid.
     * The index for both row and column starts at 1.
     *
     * @param row Number of row this slot is located at.
     * @param col Number of column this slot is located at.
     */
    Slot(int row, int col) {
        super();

        if (row <= 0 || col <= 0 || row > Board.SIZE || col > Board.SIZE) {
            throw new IllegalArgumentException();
        } else {
            this.row = row;
            column = col;
        }

    }

    /**
     * Updates the ownership of this slot(setting a token of this player
     * on this slot) and paints the token of the new owner on this slot.
     *
     * @param newOwner The player, that takes over this slot.
     */
    void update(Player newOwner) {
        owner = newOwner;
        repaint();
    }

    /**
     * Tests if a certain player has a token on this slot.
     *
     * @param toTest The player you want to test for ownership.
     * @return {@code True}, when the player has a token on this slot,
     *         otherwise {@code false}.
     */
    boolean isOwner(Player toTest) {
        return owner == toTest;
    }

    /**
     * Returns the row position this slot has in a grid layout.
     * Index starts counting at Position 1.
     *
     * @return The row number of this slot as {@code int}.
     */
    int getRow() {
        return row;
    }

    /**
     * Returns the column position this slot has in a grid layout.
     * Index starts counting at Position 1.
     *
     * @return The column number of this slot as {@code int}.
     */
    int getCol() {
        return column;
    }

}