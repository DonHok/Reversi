package reversi.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Class that contains the logic to play the game Reversi (also known as
 * Othello).
 */
public class ReversiBoard implements Board {

    /**
     * Values of each field on the board to evaluate the state of the board.
     */
    private final static double[][] FIELD_VALUE = {
            {9999, 5, 500, 200, 200, 500, 5, 9999},
            {5, 1, 50, 150, 150, 50, 1, 5},
            {500, 50, 250, 100, 100, 250, 50, 500},
            {200, 150, 100, 50, 50, 100, 150, 200},
            {200, 150, 100, 50, 50, 100, 150, 200},
            {500, 50, 250, 100, 100, 250, 50, 500},
            {5, 1, 50, 150, 150, 50, 1, 5},
            {9999, 5, 500, 200, 200, 500, 5, 9999}};

    /**
     * Difficulty setting of the AI. Sets the amount of moves the AI
     * will look ahead.
     */
    private int difficultySetting;

    /**
     * The player, that has the first turn in the game.
     */
    private Player startingPlayer;

    /**
     * The player, that currently has the turn.
     */
    private Player currentPlayer;

    /**
     * The 'board' that is used to play on.
     */
    private Token[][] board;

    /**
     * Root used when building a game tree.
     */
    private Node root;

    /**
     * Initializes a new Reversi board, with a set player, that has the
     * opening move, and the size defined by the Board interface.
     *
     * @param starter Player that will get the opening turn.
     */
    public ReversiBoard(Player starter) {
        startingPlayer = starter;
        this.board = new Token[SIZE][SIZE];
        for (int i = 0; i < SIZE; ++i) {
            for (int j = 0; j < SIZE; ++j) {
                board[i][j] = Token.FREE;
            }
        }
        board[SIZE / 2][SIZE / 2] = Token.RED;
        board[SIZE / 2 - 1][SIZE / 2 - 1] = Token.RED;
        board[SIZE / 2 - 1][SIZE / 2] = Token.BLUE;
        board[SIZE / 2][SIZE / 2 - 1] = Token.BLUE;
        Token.BLUE.attachPlayer(starter);
        currentPlayer = starter;
        if (starter.equals(Player.HUMAN)) {
            Token.RED.attachPlayer(Player.AI);
        } else {
            Token.RED.attachPlayer(Player.HUMAN);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (board == null) {
            return "";
        } else {
            StringBuilder stringRep = new StringBuilder();
            for (Token[] i : board) {
                for (int j = 0; j < SIZE; ++j) {
                    Token currentSpot = i[j];
                    Player owner = currentSpot.getPlayer();
                    if (currentSpot.equals(Token.FREE)) {
                        stringRep.append(".");
                    } else if (owner.equals(Player.HUMAN)) {
                        stringRep.append("X");
                    } else if (owner.equals(Player.AI)) {
                        stringRep.append("O");
                    }
                    if (j != (SIZE - 1)) {
                        stringRep.append(" ");
                    }
                }
                stringRep.append("\n");
            }

            // Deletes the unwanted last newline char
            stringRep = stringRep.deleteCharAt(2 * (SIZE * SIZE) - 1);

            return stringRep.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getFirstPlayer() {
        return this.startingPlayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getSlot(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            return null;
        } else {
            return board[row][col].getPlayer();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfHumanTiles() {
        int toReturn = 0;
        for (int i = 0; i < SIZE; ++i) {
            for (int j = 0; j < SIZE; ++j) {
                if (board[i][j].getPlayer() == Player.HUMAN) {
                    toReturn++;
                }
            }
        }
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfMachineTiles() {
        int toReturn = 0;
        for (int i = 0; i < SIZE; ++i) {
            for (int j = 0; j < SIZE; ++j) {
                if (board[i][j].getPlayer() == Player.AI) {
                    toReturn++;
                }
            }
        }
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player next() {
        if (currentPlayer.equals(Player.AI)) {
            return Player.HUMAN;
        } else {
            return Player.AI;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLevel(int level) {
        if (level > 0) {
            this.difficultySetting = level;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean gameOver() {
        if (getNumberOfHumanTiles() + getNumberOfMachineTiles()
                == SIZE * SIZE) {
            return true;
        } else {
            ReversiBoard nextBoard = (ReversiBoard) this.clone();
            nextBoard.currentPlayer = this.next();
            List<Move> capability = computePossibleMoves(this);
            List<Move> nextCapability = computePossibleMoves(nextBoard);
            return capability.isEmpty()
                    && nextCapability.isEmpty();
        }
    }

    /**
     * Checks if the game is won and then returns the winner.
     *
     * @return The winner or TIE in case of a tie.
     * @throws IllegalStateException When the method is accessed and the game
     *                               isn't over.
     */
    @Override
    public Player getWinner() {
        if (gameOver()) {
            if (getNumberOfHumanTiles() > getNumberOfMachineTiles()) {
                return Player.HUMAN;
            } else if (getNumberOfMachineTiles() > getNumberOfHumanTiles()) {
                return Player.AI;
            } else {
                return Player.TIE;
            }
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board clone() {
        ReversiBoard copy = null;

        try {
            copy = (ReversiBoard) super.clone();
        } catch (CloneNotSupportedException noClone) {
            throw new Error();
        }

        copy.difficultySetting = this.difficultySetting;
        copy.startingPlayer = this.startingPlayer;
        copy.currentPlayer = this.currentPlayer;
        copy.board = this.board.clone();
        for (int i = 0; i < SIZE; ++i) {
            copy.board[i] = this.board[i].clone();
        }
        return (Board) copy;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board move(int row, int col) throws IllegalMoveException {
        if (gameOver() || currentPlayer.equals(Player.AI)) {
            throw new IllegalMoveException();
        } else if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new IllegalArgumentException("undefined parameters");
        } else if (checkMove(this, row, col, currentPlayer, next())) {
            Move playerMove = new Move(row, col);
            return makeMove(playerMove);
        } else if (computePossibleMoves(this).isEmpty()) {
            ReversiBoard toReturn = (ReversiBoard) this.clone();
            toReturn.currentPlayer = toReturn.next();
            return (Board) toReturn;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board machineMove() throws IllegalMoveException {
        if (gameOver() || currentPlayer.equals(Player.HUMAN)) {
            throw new IllegalMoveException();
        } else if (computePossibleMoves(this).isEmpty()) {
            ReversiBoard toReturn = (ReversiBoard) this.clone();
            toReturn.currentPlayer = toReturn.next();
            return (Board) toReturn;
        } else {
            Move aiMove = calculateBestMove();
            return makeMove(aiMove);
        }
    }

    /**
     * Executes a move on a clone of the current Board.
     *
     * @param toMove The move that is executed.
     * @return A new board object with the executed move.
     */
    private Board makeMove(Move toMove) {
        ReversiBoard toReturn = (ReversiBoard) this.clone();
        toReturn.reverseTiles(toMove.getRow(), toMove.getColumn());
        toReturn.currentPlayer = this.next();
        return (Board) toReturn;
    }

    /**
     * Reverses the tiles when setting a new token on the Board and sets the
     * position the new token is to the player's color.
     * Will search in all directions is they fulfill the requirements
     * and then reverse them when going back to the start position.
     *
     * @param row The row the new token will be set.
     * @param col The column the new token will be set.
     */
    private void reverseTiles(int row, int col) {
        Token playerColor = getColor();
        board[row][col] = playerColor;

        // Go into all 8 possible directions
        for (int i = -1; i < 2; ++i) {
            for (int j = -1; j < 2; ++j) {
                int xPos = row + i;
                int yPos = col + j;
                Player onSpot = this.getSlot(xPos, yPos);

                // Is there a enemy tile in the current directions?
                if (xPos >= 0 && xPos < Board.SIZE && yPos >= 0
                        && yPos < Board.SIZE && onSpot != null
                        && onSpot.equals(next())) {
                    boolean canContinue = true;

                    /* Go further into the direction until you hit a free slot,
                    /* slot with your own color, or the boundaries of the
                     * board.
                     */
                    while (xPos >= 0 && xPos < Board.SIZE && yPos >= 0
                            && yPos < Board.SIZE && canContinue) {
                        Player onSlot = this.getSlot(xPos, yPos);
                        boolean occupied = onSlot != null;
                        if (occupied && onSlot.equals(currentPlayer)) {
                            xPos = xPos - i;
                            yPos = yPos - j;

                            // Go back to the start point and claim all slots
                            while (xPos != row || yPos != col) {
                                board[xPos][yPos] = playerColor;
                                xPos = xPos - i;
                                yPos = yPos - j;
                            }

                            canContinue = false;
                        } else if (!occupied) {
                            canContinue = false;
                        }
                        xPos = i + xPos;
                        yPos = j + yPos;
                    }

                }

            }
        }

    }

    /**
     * Returns the Token type the current player has.
     *
     * @return Token of the current player.
     */
    private Token getColor() {
        Player redPlayer = Token.RED.getPlayer();
        if (currentPlayer.equals(redPlayer)) {
            return Token.RED;
        } else {
            return Token.BLUE;
        }
    }

    /**
     * Calculates the best move performed by the AI, using the state,
     * mobility and potential mobility to evaluate each move and an MinMax
     * algorithm in case there is a look ahead.
     *
     * @return Move with the highest chance to win.
     */
    private Move calculateBestMove() {
        root = new Node(this.clone());
        buildTree(root, difficultySetting);
        List<Node> children = root.getChildren();

        Move bestMove = children.get(0).getMove();
        double max = children.get(0).minMax();
        for (int i = 1; i < children.size(); ++i) {
            Node child = children.get(i);
            double temp = child.minMax();
            if (temp > max) {
                max = child.getPoints();
                bestMove = child.getMove();
            }
        }

        return bestMove;
    }

    /**
     * Builds a game tree recursively using a using the possible moves
     * of a board to create new Nodes until the maximum level(depth) is
     * reached.
     *
     * @param start The Node that is used to build the tree.
     * @param maxDepth The maximum depth/look ahead of the tree. Equals
     *                 the AI level.
     */
    private void buildTree(Node start, int maxDepth) {
        if (start != null && maxDepth > 0) {
            ReversiBoard current = (ReversiBoard) start.getBoard();
            List<Move> moves = computePossibleMoves(current);

            // Does the player have moves?
            if (!moves.isEmpty()) {

                // Execute all possible player moves and add the result.
                for (Move move : moves) {
                    Board temp = current.makeMove(move);
                    Node toAdd = new Node(temp);
                    toAdd.savePoints(evaluateBoard(temp));
                    toAdd.saveMove(move);
                    start.addChild(toAdd);
                }

                // Recursively go one step deeper.
                for (Node goFurther : start.getChildren()) {
                    buildTree(goFurther, (maxDepth - 1));
                }

                // Does the enemy have moves?
            } else if (!current.gameOver()) {
                ReversiBoard switched = (ReversiBoard) current.clone();
                switched.currentPlayer = current.next();
                List<Move> otherMoves = computePossibleMoves(switched);


                for (Move move : otherMoves) {
                    Board temp = switched.makeMove(move);
                    Node toAdd = new Node(temp);
                    toAdd.saveMove(move);
                    toAdd.savePoints(evaluateBoard(temp));
                    start.addChild(toAdd);
                }

                for (Node goFurther : start.getChildren()) {
                    buildTree(goFurther, (maxDepth - 1));
                }
            }

        }
    }

    /**
     * Evaluates a board by evaluating the state, potential mobility and
     * mobility of both human and ai and adding up those numbers.
     *
     * @param toEval The board that will be evaluated.
     * @return The value the board gets according to the way of measuring it.
     * @throws IllegalArgumentExecption When giving the method null.
     */
    private double evaluateBoard(Board toEval) {
        if (toEval == null) {
            throw new IllegalArgumentException("Cant eval null");
        } else {

            double totalTiles = toEval.getNumberOfHumanTiles()
                    + toEval.getNumberOfMachineTiles();
            PointPair potential = potentialMobility(toEval);
            double humanPotential = potential.getHumanPoints();
            double aiPotential = potential.getAiPoints();
            PointPair statePoints = evaluateState(toEval);
            double humanState = statePoints.getHumanPoints();
            double aiState = statePoints.getAiPoints();
            double humanMobility;
            double aiMobility;

            if (toEval.next().equals(Player.AI)) {
                humanMobility = mobility(toEval);
                ReversiBoard temp = (ReversiBoard) toEval.clone();
                temp.currentPlayer = Player.AI;
                aiMobility = mobility(temp);
            } else {
                aiMobility = mobility(toEval);
                ReversiBoard temp = (ReversiBoard) toEval.clone();
                temp.currentPlayer = Player.HUMAN;
                humanMobility = mobility(temp);
            }

            return (aiState - 1.5 * humanState) + (64.0 / totalTiles)
                    * (3.0 * aiMobility - 4.0 * humanMobility)
                    + (64.0 / (2.0 * totalTiles))
                    * (2.5 * aiPotential - 3.0 * humanPotential);
        }
    }

    /**
     * Calculates the mobility of the player with the turn by getting
     * the amount of possible moves.
     *
     * @param toCheck Board that will be used for calculation.
     * @return The amount of possible moves as double.
     */
    private static double mobility(Board toCheck) {
        if (toCheck == null) {
            throw new IllegalArgumentException("No Board to evaluate");
        } else {
            return (double) computePossibleMoves(toCheck).size();
        }
    }

    /**
     * Computes all possible moves a player has on board by searching every
     * free space on board and testing if there is a move allowed.
     *
     * @param toCompute Board that will be used to calculate the moves.
     * @return All possible moves a player has in form of a list.
     */
    private static List<Move> computePossibleMoves(Board toCompute) {
        List<Move> computedMoves = new LinkedList<Move>();
        if (toCompute.getNumberOfHumanTiles() != 0
                || toCompute.getNumberOfMachineTiles() != 0) {
            Player player;
            Player enemy;
            Player nextPlayer = toCompute.next();
            if (nextPlayer.equals(Player.AI)) {
                player = Player.HUMAN;
                enemy = Player.AI;
            } else {
                player = Player.AI;
                enemy = Player.HUMAN;
            }

            for (int i = 0; i < Board.SIZE; ++i) {
                for (int j = 0; j < Board.SIZE; ++j) {
                    if (checkMove(toCompute, i, j, player, enemy)) {

                        Move toAdd = new Move(i, j);
                        computedMoves.add(toAdd);

                    }
                }
            }

        }
        return computedMoves;
    }

    /**
     * Check s a certain spot if there is a move possible, by going into
     * all directions and testing them for validity.
     *
     * @param toCheck The board that will be used to check.
     * @param row The row index of the spot.
     * @param col The column index of the spot.
     * @param player The player that sets the new token.
     * @param enemy The player that dosen't set the new token.
     * @return {@code true} if there is any direction a move is possible.
     *         Else {@code false}
     */
    private static boolean checkMove(Board toCheck, int row, int col,
                                     Player player, Player enemy) {
        if (toCheck.getSlot(row, col) != null) {
            return false;
        } else {

            // Go into all 8 directions and check if one allows a move.
            for (int xAxis = -1; xAxis < 2; ++xAxis) {
                for (int yAxis = -1; yAxis < 2; ++yAxis) {
                    Player onSpot = toCheck.getSlot(row + xAxis, col + yAxis);
                    if (row + xAxis >= 0 && row + xAxis < Board.SIZE
                            && col + yAxis >= 0 && col + yAxis < Board.SIZE
                            && onSpot != null && onSpot.equals(enemy)) {
                        int i = xAxis + xAxis;
                        int j = yAxis + yAxis;
                        boolean canContinue = true;

                        /* Go into this direction until you hit your own token
                         * , leaves the boundaries or hit a free space.
                         */
                        while (row + i >= 0 && row + i < Board.SIZE
                                && col + j >= 0 && col + j < Board.SIZE
                                && canContinue) {
                            Player onSlot = toCheck.getSlot(row + i, col + j);
                            if (onSlot == null) {
                                canContinue = false;

                                // Token of the player hit so the move is possible.
                            } else if (onSlot.equals(player)) {
                                return true;
                            }

                            i = i + xAxis;
                            j = j + yAxis;
                        }

                    }
                }
            }

        }
        return false;
    }

    /**
     * Evaluates the points each player for the state of the board
     * by using the predefined values.
     *
     * @param toEval Board that will be evaluated.
     * @return Both human and ai points in a wrapper class.
     */
    private static PointPair evaluateState(Board toEval) {
        if (toEval == null) {
            throw new IllegalArgumentException("No board to evaluate");
        } else {
            double human = 0;
            double ai = 0;

            for (int i = 0; i < Board.SIZE; ++i) {
                for (int j = 0; j < Board.SIZE; ++j) {
                    Player currentSlot = toEval.getSlot(i, j);
                    if (currentSlot != null
                            && currentSlot.equals(Player.HUMAN)) {
                        human = human + FIELD_VALUE[i][j];
                    } else if (currentSlot != null
                            && currentSlot.equals(Player.AI)) {
                        ai = ai + FIELD_VALUE[i][j];
                    }
                }
            }

            return new PointPair(human, ai);
        }
    }

    /**
     * Evaluates the points each player gets for the potential mobility by
     * adding up the free spaces on the board around tokens of the opposing
     * player.
     *
     * @param toEval The board that will be evaluated.
     * @return Both human and ai points in a wrapper class.
     */
    private static PointPair potentialMobility(Board toEval) {
        if (toEval == null) {
            throw new IllegalArgumentException("No board to evaluate");
        } else {
            double human = 0;
            double ai = 0;

            for (int i = 0; i < Board.SIZE; ++i) {
                for (int j = 0; j < Board.SIZE; ++j) {
                    Player currentSlot = toEval.getSlot(i, j);
                    if (currentSlot != null
                            && currentSlot.equals(Player.HUMAN)) {
                        ai += slotPotential(i, j, toEval);
                    } else if (currentSlot != null
                            && currentSlot.equals(Player.AI)) {
                        human += slotPotential(i, j, toEval);
                    }
                }
            }

            return new PointPair(human, ai);
        }
    }

    /**
     * Evaluates the potential mobility of a single spot by adding up
     * all the free spaces around it.
     *
     * @param row The row index of the spot to be evaluated.
     * @param col The column index of the spot to be evaluated.
     * @param toCheck The Board the spot is on.
     * @return The amount of free fields around one spot.
     */
    private static double slotPotential(int row, int col, Board toCheck) {
        double potential = 0;

        // Look into all 8 directions and sum up all free spaces.
        for (int xAxis = -1; xAxis < 2; ++xAxis) {
            for (int yAxis = -1; yAxis < 2; ++yAxis) {
                int yPos = row + xAxis;
                int xPos = col + yAxis;
                if (yPos >= 0 && yPos < Board.SIZE && xPos >= 0
                        && xPos < Board.SIZE) {
                    if (toCheck.getSlot(yPos, xPos) == null) {
                        potential = potential + 1;
                    }
                }
            }
        }

        return potential;
    }

}