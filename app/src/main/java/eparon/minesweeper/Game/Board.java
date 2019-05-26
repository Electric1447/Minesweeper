package eparon.minesweeper.Game;

public class Board {

    private Cell[][] cell;
    private final int rows;
    private final int cols;
    private boolean enabled = true;

    public Board(int r, int c) {

        this.rows = r;
        this.cols = c;

        this.cell = new Cell[rows][cols];

        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < this.cols; j++)
                this.cell[i][j] = new Cell();
    }

    public Cell getCell(int r, int c) {
        return this.cell[r][c];
    }

    public void resetState () {
        enabled = true;
        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < this.cols; j++)
                this.cell[i][j].resetState();
    }

    public void Disable () {
        enabled = false;
        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < this.cols; j++)
                this.cell[i][j].setClicked(true);
    }

    public boolean isEnabled () {
        return this.enabled;
    }

    public void detectBombs () {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (!this.cell[r][c].isBomb()) {
                    if (this.isMineAt(r - 1, c - 1)) this.cell[r][c].plusplus(); // Top Left
                    if (this.isMineAt(r - 1, c    )) this.cell[r][c].plusplus(); // Top
                    if (this.isMineAt(r - 1, c + 1)) this.cell[r][c].plusplus(); // Top Right
                    if (this.isMineAt(r    , c - 1)) this.cell[r][c].plusplus(); // Left
                    if (this.isMineAt(r    , c + 1)) this.cell[r][c].plusplus(); // Right
                    if (this.isMineAt(r + 1, c - 1)) this.cell[r][c].plusplus(); // Bottom Left
                    if (this.isMineAt(r + 1, c    )) this.cell[r][c].plusplus(); // Bottom
                    if (this.isMineAt(r + 1, c + 1)) this.cell[r][c].plusplus(); // Bottom Right
                }
    }

    public int countSurroundingFlags (final int r, final int c) {
        int count = 0;
        if (this.isFlagAt(r - 1, c - 1)) count++; // Top Left
        if (this.isFlagAt(r - 1, c    )) count++; // Top
        if (this.isFlagAt(r - 1, c + 1)) count++; // Top Right
        if (this.isFlagAt(r    , c - 1)) count++; // Left
        if (this.isFlagAt(r    , c + 1)) count++; // Right
        if (this.isFlagAt(r + 1, c - 1)) count++; // Bottom Left
        if (this.isFlagAt(r + 1, c    )) count++; // Bottom
        if (this.isFlagAt(r + 1, c + 1)) count++; // Bottom Right
        return count;
    }

    public boolean inbounds (final int r, final int c) {
        return !(r < 0 || c < 0 || r >= this.rows || c >= this.cols);
    }

    private boolean isMineAt (final int r, final int c) {
        if (this.inbounds(r, c))
            return this.cell[r][c].isBomb();
        return false;
    }

    private boolean isFlagAt (final int r, final int c) {
        if (this.inbounds(r, c))
            return this.cell[r][c].isFlagged();
        return false;
    }

}
