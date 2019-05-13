package eparon.minesweeper.Game;

public class Board {

    private Cell[][] cell;
    private final int rows;
    private final int cols;

    public Board(int r, int c) {

        this.rows = r;
        this.cols = c;

        this.cell = new Cell[rows][cols];

        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < this.cols; j++)
                this.cell[i][j] = new Cell();
    }

    public Cell[][] getBoard() {
        return this.cell;
    }

    public Cell getCell(int r, int c) {
        return this.cell[r][c];
    }

    public void Disable () {
        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < this.cols; j++)
                this.cell[i][j].setClicked(true);
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

    private boolean isMineAt (final int r, final int c) {
        if (!(r < 0 || c < 0 || r >= this.rows || c >= this.cols))
            return this.cell[r][c].isBomb();
        return false;
    }

}
