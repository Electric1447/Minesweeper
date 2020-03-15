package eparon.minesweeper.Game;

public class Cell {

    private int value = 0;
    private boolean revealed = false;
    private boolean flagged = false;

    /**
     * Constructor for class Cell
     */
    public Cell () {
    }

    //region Get&Set region
    public int getValue () {
        return this.value;
    }

    public boolean isRevealed () {
        return this.revealed;
    }

    public boolean isFlagged () {
        return this.flagged;
    }

    public void setValue (int val) {
        this.value = val;
    }

    public void setRevealed (boolean rvl) {
        this.revealed = rvl;
    }

    public void setFlagged (boolean flag) {
        this.flagged = flag;
    }
    //endregion

    /**
     * This method returns if this cell is a bomb.
     *
     * @return boolean; This returns if this cell is a bomb.
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

}
