package eparon.minesweeper;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridLayout.LayoutParams;
import android.widget.GridLayout.Spec;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity {

    int rows = 18;
    int cols = 12;
    int[][] cells = new int[rows][cols];
    boolean[][] clicked = new boolean[rows][cols];
    boolean[][] flagged = new boolean[rows][cols];
    int bombs = 60;
    int flags = bombs;
    boolean flag = false;
    boolean borderRowsChecked = false;

    GridLayout gridLayout;
    Spec row[] = new Spec[rows];
    Spec col[] = new Spec[cols];
    int slotWidth, slotHeight;

    FrameLayout fl[][] = new FrameLayout[rows][cols];
    ImageButton ib[][] = new ImageButton[rows][cols];

    Drawable images[] = new Drawable[10];
    int imagesResID[] = new int[] {R.drawable.bomb, R.drawable.cell_empty, R.drawable.cell_1,
            R.drawable.cell_2, R.drawable.cell_3, R.drawable.cell_4, R.drawable.cell_5,
            R.drawable.cell_6, R.drawable.cell_7, R.drawable.cell_8};

    TextView[] TimerText = new TextView[2];
    long startTime = 0;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int)(millis / 1000);
            int minutes = seconds / 60;
            seconds %= 60;

            TimerText[0].setText(String.format(Locale.getDefault(), "%02d", minutes));
            TimerText[1].setText(String.format(Locale.getDefault(), "%02d", seconds));

            timerHandler.postDelayed(this, 500);
        }
    };

    TextView BombsCounter;
    ImageView smiley;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimerText[0] = findViewById(R.id.timer1);
        TimerText[1] = findViewById(R.id.timer2);
        BombsCounter = findViewById(R.id.bombsCounter);
        Typeface digital7_mono = Typeface.createFromAsset(getAssets(), "fonts/digital-7_mono.ttf");
        TimerText[0].setTypeface(digital7_mono);
        TimerText[1].setTypeface(digital7_mono);
        BombsCounter.setTypeface(digital7_mono);
        LinearLayout TimerLL = findViewById(R.id.timerll);
        Drawable timerDrawable = getDrawable(R.drawable.timer_background);
        Drawable bcDrawable = getDrawable(R.drawable.bombs_counter_background);
        assert timerDrawable != null;
        assert bcDrawable != null;
        timerDrawable.setFilterBitmap(false);
        bcDrawable.setFilterBitmap(false);
        TimerLL.setBackground(timerDrawable);
        BombsCounter.setBackground(bcDrawable);

        ImageView flagIV = findViewById(R.id.switchPointer);
        Drawable bDraw = getDrawable(R.drawable.bomb2);
        assert bDraw != null;
        bDraw.setFilterBitmap(false);
        flagIV.setBackground(bDraw);

        for (int i = 0; i < images.length; i++) {
            images[i] = getDrawable(imagesResID[i]);
            assert images[i] != null;
            images[i].setFilterBitmap(false);
        }

        smiley = findViewById(R.id.smiley);
        Drawable s1 = getDrawable(R.drawable.smiley);
        assert s1 != null;
        s1.setFilterBitmap(false);
        smiley.setImageDrawable(s1);
        smiley.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Drawable s3 = getDrawable(R.drawable.smiley3);
                        assert s3 != null;
                        s3.setFilterBitmap(false);
                        smiley.setImageDrawable(s3);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        Init();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                Drawable s1 = getDrawable(R.drawable.smiley);
                                assert s1 != null;
                                s1.setFilterBitmap(false);
                                smiley.setImageDrawable(s1);
                            }
                        }, 50);
                        break;
                }
                return true;
            }
        });

        // Getting the Screen Size and converting it into the Slots Prams;
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        int pixels = (int)(scale + 0.5f);
        double screenWidth = size.x - pixels;
        double screenHeight = screenWidth / cols * rows;
        slotWidth = (int)(screenWidth / cols);
        slotHeight = (int)(screenHeight / rows);

        // Initializing the GridLayout;
        for (int i = 0; i < rows; i++)
            row[i] = GridLayout.spec(i);
        for (int i = 0; i < cols; i++)
            col[i] = GridLayout.spec(i);


        gridLayout = findViewById(R.id.gl);
        gridLayout.setColumnCount(col.length);
        gridLayout.setRowCount(row.length);

        Init();
    }

    private void Init() {

        timerHandler.removeCallbacks(timerRunnable);
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        bombs = 60;
        flags = bombs;

        BombsCounter.setText(String.format(Locale.getDefault(),"%02d", flags));

        borderRowsChecked = false;

        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                cells[i][j] = 0;

        Random r = new Random();
        int counter = 0;

        while (bombs != 0) {
            if (counter >= cols * rows)
                counter = 0;

            if (r.nextDouble() < 0.10 && cells[counter / cols][counter % cols] != -1) {
                cells[counter / cols][counter % cols] = -1;
                bombs--;
            }
            counter++;
        }

        // TopLeft
        if (cells[0][0] != -1) {
            // Right
            if (cells[0][1] == -1)
                cells[0][0]++;
            // Bottom
            if (cells[1][0] == -1)
                cells[0][0]++;
            // BottomRight
            if (cells[1][1] == -1)
                cells[0][0]++;
        }

        // TopRight
        if (cells[0][cols - 1] != -1) {
            // Left
            if (cells[0][cols - 2] == -1)
                cells[0][cols - 1]++;
            // BottomLeft
            if (cells[1][cols - 2] == -1)
                cells[0][cols - 1]++;
            // Bottom
            if (cells[1][cols - 1] == -1)
                cells[0][cols - 1]++;
        }

        // BottomLeft
        if (cells[rows - 1][0] != -1) {
            // Top
            if (cells[rows - 2][0] == -1)
                cells[rows - 1][0]++;
            // TopRight
            if (cells[rows - 2][1] == -1)
                cells[rows - 1][0]++;
            // Right
            if (cells[rows - 1][1] == -1)
                cells[rows - 1][0]++;
        }

        // BottomRight
        if (cells[rows - 1][cols - 1] != -1) {
            // TopLeft
            if (cells[rows - 2][cols - 2] == -1)
                cells[rows - 1][cols - 1]++;
            // Top
            if (cells[rows - 2][cols - 1] == -1)
                cells[rows - 1][cols - 1]++;
            // Left
            if (cells[rows - 1][cols - 2] == -1)
                cells[rows - 1][cols - 1]++;
        }

        for (int i = 1; i < rows - 1; i++) {

            if (cells[i][0] != -1) {
                // Top
                if (cells[i - 1][0] == -1)
                    cells[i][0]++;
                // TopRight
                if (cells[i - 1][1] == -1)
                    cells[i][0]++;
                // Right
                if (cells[i][1] == -1)
                    cells[i][0]++;
                // Bottom
                if (cells[i + 1][0] == -1)
                    cells[i][0]++;
                // BottomRight
                if (cells[i + 1][1] == -1)
                    cells[i][0]++;
            }

            if (cells[i][cols - 1] != -1) {
                // TopLeft
                if (cells[i - 1][cols - 2] == -1)
                    cells[i][cols - 1]++;
                // Top
                if (cells[i - 1][cols - 1] == -1)
                    cells[i][cols - 1]++;
                // Left
                if (cells[i][cols - 2] == -1)
                    cells[i][cols - 1]++;
                // BottomLeft
                if (cells[i + 1][cols - 2] == -1)
                    cells[i][cols - 1]++;
                // Bottom
                if (cells[i + 1][cols - 1] == -1)
                    cells[i][cols - 1]++;
            }

            for (int j = 1; j < cols - 1; j++) {

                if (!borderRowsChecked) {
                    if (cells[0][j] != -1) {
                        // Left
                        if (cells[0][j - 1] == -1)
                            cells[0][j]++;
                        // Right
                        if (cells[0][j + 1] == -1)
                            cells[0][j]++;
                        // BottomLeft
                        if (cells[1][j - 1] == -1)
                            cells[0][j]++;
                        // Bottom
                        if (cells[1][j] == -1)
                            cells[0][j]++;
                        // BottomRight
                        if (cells[1][j + 1] == -1)
                            cells[0][j]++;
                    }

                    if (cells[rows - 1][j] != -1) {
                        // TopLeft
                        if (cells[rows - 2][j - 1] == -1)
                            cells[rows - 1][j]++;
                        // Top
                        if (cells[rows - 2][j] == -1)
                            cells[rows - 1][j]++;
                        // TopRight
                        if (cells[rows - 2][j + 1] == -1)
                            cells[rows - 1][j]++;
                        // Left
                        if (cells[rows - 1][j - 1] == -1)
                            cells[rows - 1][j]++;
                        // Right
                        if (cells[rows - 1][j + 1] == -1)
                            cells[rows - 1][j]++;
                    }
                }

                if (cells[i][j] != -1) {
                    // TopLeft
                    if (cells[i - 1][j - 1] == -1)
                        cells[i][j]++;
                    // Top
                    if (cells[i - 1][j] == -1)
                        cells[i][j]++;
                    // TopRight
                    if (cells[i - 1][j + 1] == -1)
                        cells[i][j]++;
                    // Left
                    if (cells[i][j - 1] == -1)
                        cells[i][j]++;
                    // Right
                    if (cells[i][j + 1] == -1)
                        cells[i][j]++;
                    // BottomLeft
                    if (cells[i + 1][j - 1] == -1)
                        cells[i][j]++;
                    // Bottom
                    if (cells[i + 1][j] == -1)
                        cells[i][j]++;
                    // BottomRight
                    if (cells[i + 1][j + 1] == -1)
                        cells[i][j]++;
                }
            }

            borderRowsChecked = true;
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                clicked[i][j] = false;
                flagged[i][j] = false;

                View v = View.inflate(this, R.layout.cell, null);
                fl[i][j] = v.findViewById(R.id.fl);
                ib[i][j] = v.findViewById(R.id.image);

                if (fl[i][j].getParent() != null)
                    ((ViewGroup) fl[i][j].getParent()).removeView(fl[i][j]);

                LayoutParams lp = new LayoutParams(row[i], col[j]);
                lp.width = slotWidth;
                lp.height = slotHeight;
                fl[i][j].setLayoutParams(lp);
                gridLayout.addView(fl[i][j], lp);

                Drawable cuaDraw = getDrawable(R.drawable.cell_unassigned);
                assert cuaDraw != null;
                cuaDraw.setFilterBitmap(false);
                ib[i][j].setImageDrawable(cuaDraw);

                final int finalI = i, finalJ = j;
                ib[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        revealCell(finalI, finalJ);
                    }
                });

            }
        }

        // Set the GridLayout's height the same size as it's width
        ViewGroup.LayoutParams lp = gridLayout.getLayoutParams();
        lp.height = lp.width;
        gridLayout.setLayoutParams(lp);
    }

    private void revealCell(int rowPos, int colPos) {
        if (!clicked[rowPos][colPos]) {
            if (flag) {
                if (!flagged[rowPos][colPos]) {
                    if (flags > 0) {
                        Drawable fDraw = getDrawable(R.drawable.flag);
                        assert fDraw != null;
                        fDraw.setFilterBitmap(false);
                        ib[rowPos][colPos].setImageDrawable(fDraw);
                        flags--;
                    } else
                        flagged[rowPos][colPos] = !flagged[rowPos][colPos];
                } else {
                    Drawable cuaDraw = getDrawable(R.drawable.cell_unassigned);
                    assert cuaDraw != null;
                    cuaDraw.setFilterBitmap(false);
                    ib[rowPos][colPos].setImageDrawable(cuaDraw);
                    flags++;
                }
                BombsCounter.setText(String.format(Locale.getDefault(), "%02d", flags));
                flagged[rowPos][colPos] = !flagged[rowPos][colPos];
            } else if (!flagged[rowPos][colPos]) {
                clicked[rowPos][colPos] = true;
                switch (cells[rowPos][colPos]) {
                    case -1:
                        Drawable s2 = getDrawable(R.drawable.smiley2);
                        assert s2 != null;
                        s2.setFilterBitmap(false);
                        smiley.setImageDrawable(s2);
                        timerHandler.removeCallbacks(timerRunnable);
                        for (int i = 0; i < rows; i++)
                            for (int j = 0; j < cols; j++)
                                if (!flagged[i][j])
                                    ib[i][j].setImageDrawable(images[cells[i][j] + 1]);
                                else if (cells[i][j] != -1) {
                                    Drawable fDraw = getDrawable(R.drawable.flag2);
                                    assert fDraw != null;
                                    fDraw.setFilterBitmap(false);
                                    ib[i][j].setImageDrawable(fDraw);
                                }
                        break;
                    case 0:
                        ib[rowPos][colPos].setImageDrawable(images[cells[rowPos][colPos] + 1]);
                        if (rowPos == 0 && colPos == 0) {
                            revealCell(rowPos, colPos + 1);                 // Right
                            revealCell(rowPos + 1, colPos);                // Bottom
                            revealCell(rowPos + 1, colPos + 1);     // BottomRight
                        } else if (rowPos == 0 && colPos == cols - 1) {
                            revealCell(rowPos, colPos - 1);                 // Left
                            revealCell(rowPos + 1, colPos - 1);     // BottomLeft
                            revealCell(rowPos + 1, colPos);                // Bottom
                        } else if (rowPos == rows - 1 && colPos == 0) {
                            revealCell(rowPos - 1, colPos);                // Top
                            revealCell(rowPos - 1, colPos + 1);     // TopRight
                            revealCell(rowPos, colPos + 1);                 // Right
                        } else if (rowPos == rows - 1 && colPos == cols - 1) {
                            revealCell(rowPos - 1, colPos - 1);     // TopLeft
                            revealCell(rowPos - 1, colPos);                // Top
                            revealCell(rowPos, colPos - 1);                 // Left
                        } else if (rowPos == 0 && colPos != cols - 1) {
                            revealCell(rowPos, colPos - 1);                 // Left
                            revealCell(rowPos, colPos + 1);                 // Right
                            revealCell(rowPos + 1, colPos - 1);     // BottomLeft
                            revealCell(rowPos + 1, colPos);                // Bottom
                            revealCell(rowPos + 1, colPos + 1);     // BottomRight
                        } else if (rowPos == rows - 1 && colPos != 0 && colPos != cols - 1) {
                            revealCell(rowPos - 1, colPos - 1);     // TopLeft
                            revealCell(rowPos - 1, colPos);                // Top
                            revealCell(rowPos - 1, colPos + 1);     // TopRight
                            revealCell(rowPos, colPos - 1);                 // Left
                            revealCell(rowPos, colPos + 1);                 // Right
                        } else if (colPos == 0 && rowPos != rows - 1) {
                            revealCell(rowPos - 1, colPos);                // Top
                            revealCell(rowPos - 1, colPos + 1);     // TopRight
                            revealCell(rowPos, colPos + 1);                 // Right
                            revealCell(rowPos + 1, colPos);                // Bottom
                            revealCell(rowPos + 1, colPos + 1);     // BottomRight
                        } else if (colPos == cols - 1 && rowPos != 0 && rowPos != rows - 1) {
                            revealCell(rowPos - 1, colPos - 1);     // TopLeft
                            revealCell(rowPos - 1, colPos);                // Top
                            revealCell(rowPos, colPos - 1);                 // Left
                            revealCell(rowPos + 1, colPos - 1);     // BottomLeft
                            revealCell(rowPos + 1, colPos);                // Bottom
                        } else {
                            revealCell(rowPos - 1, colPos - 1);     // TopLeft
                            revealCell(rowPos - 1, colPos);                // Top
                            revealCell(rowPos - 1, colPos + 1);     // TopRight
                            revealCell(rowPos, colPos - 1);                 // Left
                            revealCell(rowPos, colPos + 1);                 // Right
                            revealCell(rowPos + 1, colPos - 1);     // BottomLeft
                            revealCell(rowPos + 1, colPos);                // Bottom
                            revealCell(rowPos + 1, colPos + 1);     // BottomRight
                        }
                        break;
                    default:
                        ib[rowPos][colPos].setImageDrawable(images[cells[rowPos][colPos] + 1]);
                        break;
                }
            }
        }
    }

    public void flagSwitch (View view) {
        flag = !flag;
        Drawable fDraw;
        if (flag)
            fDraw = getDrawable(R.drawable.flag);
        else
            fDraw = getDrawable(R.drawable.bomb2);
        assert fDraw != null;
        fDraw.setFilterBitmap(false);
        view.setBackground(fDraw);
    }

}
