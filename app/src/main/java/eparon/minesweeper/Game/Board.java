package eparon.minesweeper.Game;

import java.util.Random;

public class Board {

    private final Cell[][] cell;
    private final int rows;
    private final int cols;
    private boolean state = true;

    public static final int[][] neighboursLoop = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

    /**
     * Constructor for class Board
     *
     * @param r This is the cell's row
     * @param c This is the cell's column
     */
    public Board (int r, int c) {

        this.rows = r;
        this.cols = c;

        this.cell = new Cell[rows][cols];

        // Reset all cells state.
        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < this.cols; j++)
                this.cell[i][j] = new Cell();
    }

    public Cell getCell (int r, int c) {
        return this.cell[r][c];
    }

    public boolean getState () {
        return this.state;
    }

    public void setState (boolean s) {
        this.state = s;
    }

    /**
     * This function resets the board's state.
     */
    public void resetState () {
        this.state = true;
        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < this.cols; j++)
                this.cell[i][j].resetState();
    }

    /**
     * This function generates the random game board.
     *
     * @param firstCell the position of the first cell the user clicked
     * @param bombs the amount of bombs
     */
    public void startBoardGeneration (int firstCell, int bombs) {

        Random rnd = new Random();
        int counter = 0;

        while (bombs != 0) {
            if (counter >= cols * rows)
                counter = 0;

            if (rnd.nextDouble() < 0.10 && !this.cell[counter / cols][counter % cols].isBomb()
                    && counter != firstCell - 1 - cols && counter != firstCell - cols && counter != firstCell + 1 - cols
                    && counter != firstCell - 1        && counter != firstCell        && counter != firstCell + 1
                    && counter != firstCell - 1 + cols && counter != firstCell + cols && counter != firstCell + 1 + cols) {
                this.cell[counter / cols][counter % cols].setValue(-1);
                bombs--;
            }

            counter++;
        }

        // This loop detects the bombs on the board and sets the value of all of his cells.
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (!this.cell[r][c].isBomb())
                    for (int i = 0; i < 8; i++)
                        if (this.isMineAt(r + neighboursLoop[i][0], c + neighboursLoop[i][1]))
                            this.cell[r][c].setValue(this.cell[r][c].getValue() + 1);
    }

    /**
     * This method counts the amount of flags that surround the given cell.
     *
     * @param r This is the cell's row
     * @param c This is the cell's column
     * @return int; This returns the amount of flags surrounding the at cell--[r,c].
     */
    public int countSurroundingFlags (final int r, final int c) {
        int count = 0;

        for (int i = 0; i < 8; i++)
            if (this.isFlagAt(r + neighboursLoop[i][0], c + neighboursLoop[i][1]))
                count++;

        return count;
    }

    /**
     * This method checks if a cell is inbound.
     *
     * @param r This is the cell's row
     * @param c This is the cell's column
     * @return boolean; This returns whether the given cell is inbound.
     */
    public boolean inbounds (final int r, final int c) {
        return !(r < 0 || c < 0 || r >= this.rows || c >= this.cols);
    }

    /**
     * This method checks if a mine is at a given location.
     *
     * @param r This is the cell's row
     * @param c This is the cell's column
     * @return boolean; This returns if a mine is at cell--[r,c].
     */
    public boolean isMineAt (final int r, final int c) {
        if (this.inbounds(r, c))
            return this.cell[r][c].isBomb();
        return false;
    }

    /**
     * This method checks if a flag is at a given location.
     *
     * @param r This is the cell's row
     * @param c This is the cell's column
     * @return boolean; This returns if a flag is at cell--[r,c].
     */
    public boolean isFlagAt (final int r, final int c) {
        if (this.inbounds(r, c))
            return this.cell[r][c].isFlagged();
        return false;
    }

}
