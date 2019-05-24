package eparon.minesweeper.Game;

@SuppressWarnings("WeakerAccess")
public class Difficulty {

    public static final double EASY = 6.0;
    public static final double NORMAL = 4.5;
    public static final double HARD = 3.6;
    public static final double EXTREME = 3.0;

    public static int valueToPosition (double d) {
        return (int)(6 - d);
    }

    public static double positionToValue (int i) {
        double[] difArr = new double[]{EASY, NORMAL, HARD, EXTREME};
        return difArr[i];
    }

}
