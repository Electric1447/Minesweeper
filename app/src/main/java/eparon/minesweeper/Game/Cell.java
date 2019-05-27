package eparon.minesweeper.Game;

public class Cell {

    private int value;
    private boolean clicked;
    private boolean flagged;

    public Cell () {
        this.value = 0;
        this.clicked = false;
        this.flagged = false;
    }

    public int getValue () {
        return this.value;
    }

    public boolean isClicked () {
        return this.clicked;
    }

    public boolean isFlagged () {
        return this.flagged;
    }

    public void setValue (int v) {
        this.value = v;
    }

    public void setClicked (boolean c) {
        this.clicked = c;
    }

    public void setFlagged (boolean f) {
        this.flagged = f;
    }

    public boolean isBomb () {
        return this.value == -1;
    }

    public void resetState () {
        this.value = 0;
        this.clicked = false;
        this.flagged = false;
    }

    public void plusplus () {
        this.value++;
    }

}
