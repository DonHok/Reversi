package reversi.model;

/**
 * Wrapper class to store two double values.
 */
class PointPair {

    /**
     * Double value stored in a object of this class.
     */
    private double human;

    /**
     * Double value stored in a object of this class.
     */
    private double cpuAi;

    /**
     * Creates a object of the PointPair class, that stores two
     * double values.
     *
     * @param player First value saved in the object.
     * @param ai Second vales saved in the object.
     */
    PointPair(double player, double ai) {
        human = player;
        cpuAi = ai;
    }

    /**
     * Method to access the first value saved in an object of this class.
     *
     * @return The first saved value as double.
     */
    double getHumanPoints() {
        return human;
    }

    /**
     * Method to access the second value saved in an object of this class.
     *
     * @return The second saved value as double.
     */
    double getAiPoints() {
        return cpuAi;
    }

}