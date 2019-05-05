package eparon.minesweeper.Game;

public class CellArray {

    private Cell[][] cell;
    private int rows;
    private int cols;

    public CellArray(int r, int c) {

        this.rows = r;
        this.cols = c;

        this.cell = new Cell[rows][cols];

        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                this.cell[i][j] = new Cell();
    }

    public Cell[][] getCellArray() {
        return this.cell;
    }

    public Cell getCell(int r, int c) {
        return this.cell[r][c];
    }

    public void bombDetector() {

        boolean borderRowsChecked = false;

        // TopLeft
        if (!this.cell[0][0].isBomb()) {
            checkRight(0, 0);
            checkBottomRight(0, 0);
            checkBottom(0, 0);
        }

        // TopRight
        if (!this.cell[0][this.cols - 1].isBomb()) {
            checkLeft(0, this.cols - 1);
            checkBottomLeft(0, this.cols - 1);
            checkBottom(0, this.cols - 1);
        }

        // BottomLeft
        if (!this.cell[this.rows - 1][0].isBomb()) {
            checkTop(this.rows - 1, 0);
            checkTopRight(this.rows - 1, 0);
            checkRight(this.rows - 1, 0);
        }

        // BottomRight
        if (!this.cell[this.rows - 1][this.cols - 1].isBomb()) {
            checkTopLeft(this.rows - 1, this.cols - 1);
            checkTop(this.rows - 1, this.cols - 1);
            checkLeft(this.rows - 1, this.cols - 1);
        }

        for (int i = 1; i < this.rows - 1; i++) {

            // Left
            if (!this.cell[i][0].isBomb()) {
                checkTop(i, 0);
                checkTopRight(i, 0);
                checkRight(i, 0);
                checkBottom(i, 0);
                checkBottomRight(i, 0);
            }

            // Right
            if (!this.cell[i][this.cols - 1].isBomb()) {
                checkTopLeft(i, this.cols - 1);
                checkTop(i, this.cols - 1);
                checkLeft(i, this.cols - 1);
                checkBottomLeft(i, this.cols - 1);
                checkBottom(i, this.cols - 1);
            }
        }

        for (int j = 1; j < this.cols - 1; j++) {

            // Top
            if (!this.cell[0][j].isBomb()) {
                checkLeft(0, j);
                checkRight(0, j);
                checkBottomLeft(0, j);
                checkBottom(0, j);
                checkBottomRight(0, j);
            }

            // Bottom
            if (!this.cell[this.rows - 1][j].isBomb()) {
                checkTopLeft(this.rows - 1, j);
                checkTop(this.rows - 1, j);
                checkTopRight(this.rows - 1, j);
                checkLeft(this.rows - 1, j);
                checkRight(this.rows - 1, j);
            }
        }

        for (int i = 1; i < this.rows - 1; i++)
            for (int j = 1; j < this.cols - 1; j++)

                // Middle
                if (!this.cell[i][j].isBomb()) {
                    checkTopLeft(i, j);
                    checkTop(i, j);
                    checkTopRight(i, j);
                    checkLeft(i, j);
                    checkRight(i, j);
                    checkBottomLeft(i, j);
                    checkBottom(i, j);
                    checkBottomRight(i, j);
                }
    }

    private void checkTopLeft(int r, int c) {
        if (this.cell[r - 1][c - 1].isBomb())
            this.cell[r][c].plusplus();
    }

    private void checkTop(int r, int c) {
        if (this.cell[r - 1][c].isBomb())
            this.cell[r][c].plusplus();
    }

    private void checkTopRight(int r, int c) {
        if (this.cell[r - 1][c + 1].isBomb())
            this.cell[r][c].plusplus();
    }

    private void checkLeft(int r, int c) {
        if (this.cell[r][c - 1].isBomb())
            this.cell[r][c].plusplus();
    }

    private void checkRight(int r, int c) {
        if (this.cell[r][c + 1].isBomb())
            this.cell[r][c].plusplus();
    }

    private void checkBottomLeft(int r, int c) {
        if (this.cell[r + 1][c - 1].isBomb())
            this.cell[r][c].plusplus();
    }

    private void checkBottom(int r, int c) {
        if (this.cell[r + 1][c].isBomb())
            this.cell[r][c].plusplus();
    }

    private void checkBottomRight(int r, int c) {
        if (this.cell[r + 1][c + 1].isBomb())
            this.cell[r][c].plusplus();
    }

}
