package reversi.model;

import java.util.List;
import java.util.LinkedList;

/**
 * Node for a game tree to calculate moves. Contains the board, the resulting
 * move, the points along with children.
 */
class Node {

    /**
     * The board saved in this node.
     */
    private Board reversi;

    /**
     * The move that resulted to the board, that is saved in this node.
     */
    private Move executedMove;

    /**
     * List of children nodes.
     */
    private List<Node> children;

    /**
     * The points this node gets, when the game tree is evaluated.
     */
    private double points;

    /**
     * Initializes a new object of the Node class and saves a object of the
     * Board class in it.
     *
     * @param saved The board, that will be saved in this node.
     */
    Node(Board saved) {
        reversi = saved.clone();
    }

    /**
     * Adds a new child to this node.
     *
     * @param toAdd The child, that will be added.
     */
    void addChild(Node toAdd) {
        if (children == null) {
            children = new LinkedList<Node>();
        }
        children.add(toAdd);
    }

    /**
     * Returns the board that is saved in this node object.
     *
     * @return The saved Board.
     */
    Board getBoard() {
        return reversi;
    }

    /**
     * Returns all the children of this Node object.
     *
     * @return Children nodes as List of Node objects.
     */
    List<Node> getChildren() {
        return children;
    }

    /**
     * Saves the move, that resulted to the board saved in this node.
     *
     * @param toSave The move that will be saved.
     */
    void saveMove(Move toSave) {
        executedMove = toSave;
    }

    /**
     * Returns the move, that is saved in this object.
     *
     * @return Saved move.
     */
    Move getMove() {
        return executedMove;
    }

    /**
     * Saves the point value this board gets during evaluation of the tree.
     *
     * @param toSave Points to be saved.
     */
    void savePoints(double toSave) {
        points = toSave;
    }

    /**
     * Returns the point value this Node object got during evaluation.
     *
     * @return Points of this Node as double.
     */
    double getPoints() {
        return points;
    }

    /**
     * Returns a String representation of this node.
     *
     * @return Node as String.
     */
    @Override
    public String toString() {
        return "(" + executedMove.getRow() + ", " + executedMove.getColumn()
                + ") " + points;
    }

    /**
     * Min-Max algorithm to get the best node. Leafs just return their
     * value, while inner Nodes will add the biggest/smallest point value
     * when the AI/HUMAN, has the turn, to their saved points and then
     * return the new value.
     *
     * @return Points saved in this Node when using an MinMax algorithm.
     */
    double minMax() {
        if (children == null || children.isEmpty()) {
            return points;
        } else {
            Node firstChild = children.get(0);
            double temporary = firstChild.minMax();

            // AI has the turn so best result will be picked.
            if (firstChild.nextPlayer().equals(Player.AI)) {
                for (int i = 1; i < children.size(); ++i) {
                    Node child = children.get(i);
                    if (child.minMax() > temporary) {
                        temporary = child.getPoints();
                    }
                }

                // Human has the turn so worst result will be picked.
            } else {
                for (int i = 1; i < children.size(); ++i) {
                    Node child = children.get(i);
                    if (child.minMax() < temporary) {
                        temporary = child.getPoints();
                    }
                }
            }

            savePoints(this.getPoints() + temporary);
            return getPoints();
        }
    }

    /**
     * Returns the player, that has the next move on the board saved in this
     * Node.
     *
     * @return Next player to make a turn.
     */
    private Player nextPlayer() {
        return reversi.next();
    }

}