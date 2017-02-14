package reversi.model;

/**
 * Wrapper class to save the coordinates of a move.
 */
class Move {

    /**
     * The row index of the move.
     */
    private int row;

    /**
     * The column index of the move.
     */
    private int column;

    /**
     * Initializes a new object of the move class and saves the
     * coordinates of a move.
     *
     * @param row    Row index that will be saved.
     * @param column Column index that will be saved.
     */
    Move(int row, int column) {
        this.row = row;
        this.column = column;
    }

    /**
     * Returns the row index of the moved saved in this object.
     *
     * @return Row index that was saved in this object.
     */
    int getRow() {
        return row;
    }

    /**
     * Returns the column index of the move saved in this object.
     *
     * @return Column index that was saved in this object.
     */
    int getColumn() {
        return column;
    }

}