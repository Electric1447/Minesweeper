package eparon.minesweeper.Game;

public class Cell {

    private int value;
    private boolean revealed;
    private boolean flagged;

    /**
     * Constructor for class Cell
     */
    public Cell () {
        this.value = 0;
        this.revealed = false;
        this.flagged = false;
    }

    public int getValue () {
        return this.value;
    }

    public boolean isRevealed () {
        return this.revealed;
    }

    public boolean isFlagged () {
        return this.flagged;
    }

    public void setValue (int v) {
        this.value = v;
    }

    public void setRevealed (boolean c) {
        this.revealed = c;
    }

    public void setFlagged (boolean f) {
        this.flagged = f;
    }

    /**
     * This method returns if this cell is a bomb.
     *
     * @return boolean This returns if this cell is a bomb.
     */
    public boolean isBomb () {
        return this.value == -1;
    }

    /**
     * This function resets this cell's state.
     */
    public void resetState () {
        this.value = 0;
        this.revealed = false;
        this.flagged = false;
    }

    /**
     * This function add 1 to the value of this cell.
     */
    public void plusplus () {
        this.value++;
    }

}
