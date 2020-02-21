package eparon.minesweeper.Game;

@SuppressWarnings("WeakerAccess")
public class Difficulty {

    public static final double EASY = 6.0;
    public static final double NORMAL = 4.5;
    public static final double HARD = 3.6;
    public static final double EXTREME = 3.0;

    /**
     * This function converts a difficulty value to the difficulty position.
     *
     * @param d difficulty value
     * @return int This returns the given difficulty's position.
     */
    public static int valueToPosition (double d) {
        return (int)(6 - d);
    }

    /**
     * This function converts the difficulty's position to the difficulty value.
     *
     * @param i difficulty position
     * @return int This returns the given difficulty's value.
     */
    public static double positionToValue (int i) {
        double[] difArr = new double[]{EASY, NORMAL, HARD, EXTREME};
        return difArr[i];
    }

}
