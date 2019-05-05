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

import eparon.minesweeper.Game.Cell;
import eparon.minesweeper.Game.CellArray;

@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity {

    boolean firstTurn;
    int firstCell;

    int rows = 18;
    int cols = 12;
    CellArray cells = new CellArray(rows, cols);
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

    Drawable cuaDrawable, flagDrawable, flag2Drawable, bombDrawable, smileyDrawable, smiley2Drawable, smiley3Drawable;

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
        cuaDrawable = getDrawable(R.drawable.cell_unassigned);
        assert cuaDrawable != null;
        cuaDrawable.setFilterBitmap(false);
        flagDrawable = getDrawable(R.drawable.flag);
        assert flagDrawable != null;
        flagDrawable.setFilterBitmap(false);
        flag2Drawable = getDrawable(R.drawable.flag2);
        assert flag2Drawable != null;
        flag2Drawable.setFilterBitmap(false);
        bombDrawable = getDrawable(R.drawable.bomb2);
        assert bombDrawable != null;
        bombDrawable.setFilterBitmap(false);
        smileyDrawable = getDrawable(R.drawable.smiley);
        assert smileyDrawable != null;
        smileyDrawable.setFilterBitmap(false);
        smiley2Drawable = getDrawable(R.drawable.smiley2);
        assert smiley2Drawable != null;
        smiley2Drawable.setFilterBitmap(false);
        smiley3Drawable = getDrawable(R.drawable.smiley3);
        assert smiley3Drawable != null;
        smiley3Drawable.setFilterBitmap(false);
        flagIV.setBackground(bombDrawable);

        for (int i = 0; i < images.length; i++) {
            images[i] = getDrawable(imagesResID[i]);
            assert images[i] != null;
            images[i].setFilterBitmap(false);
        }

        smiley = findViewById(R.id.smiley);
        smiley.setImageDrawable(smileyDrawable);
        smiley.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        smiley.setImageDrawable(smiley3Drawable);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        Init();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                smiley.setImageDrawable(smileyDrawable);
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

        firstTurn = true;

        timerHandler.removeCallbacks(timerRunnable);
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        bombs = 60;
        flags = bombs;

        BombsCounter.setText(String.format(Locale.getDefault(),"%02d", flags));

        borderRowsChecked = false;

        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                cells.getCell(i, j).resetState();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

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

                ib[i][j].setImageDrawable(cuaDrawable);

                final int finalI = i, finalJ = j;
                ib[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (firstTurn) {
                            firstTurn = false;
                            firstCell = finalI * cols + finalJ;
                            startBoardGeneration();
                        }
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

    private void startBoardGeneration() {

        Random r = new Random();
        int counter = 0;

        while (bombs != 0) {
            if (counter >= cols * rows)
                counter = 0;

            if (r.nextDouble() < 0.10 && !cells.getCell(counter / cols, counter % cols).isBomb()
                    && counter != firstCell - 1 - cols && counter != firstCell - cols && counter != firstCell + 1 - cols
                    && counter != firstCell - 1        && counter != firstCell        && counter != firstCell + 1
                    && counter != firstCell - 1 + cols && counter != firstCell + cols && counter != firstCell + 1 + cols) {
                cells.getCell(counter / cols, counter % cols).setValue(-1);
                bombs--;
            }
            counter++;
        }

        cells.bombDetector();
    }

    private void revealCell(int rowPos, int colPos) {

        // If it hasn't been clicked yet.
        if (!cells.getCell(rowPos, colPos).isClicked()) {
            if (flag) {
                if (!cells.getCell(rowPos, colPos).isFlagged()) {
                    if (flags > 0) {
                        ib[rowPos][colPos].setImageDrawable(flagDrawable);
                        flags--;
                    } else
                        cells.getCell(rowPos, colPos).setFlagged(!cells.getCell(rowPos, colPos).isFlagged());
                } else {
                    ib[rowPos][colPos].setImageDrawable(cuaDrawable);
                    flags++;
                }
                BombsCounter.setText(String.format(Locale.getDefault(), "%02d", flags));
                cells.getCell(rowPos, colPos).setFlagged(!cells.getCell(rowPos, colPos).isFlagged());
            } else if (!cells.getCell(rowPos, colPos).isFlagged()) {

                cells.getCell(rowPos, colPos).setClicked(true);

                switch (cells.getCell(rowPos, colPos).getValue()) {
                    case -1:
                        smiley.setImageDrawable(smiley2Drawable);
                        timerHandler.removeCallbacks(timerRunnable);
                        for (int i = 0; i < rows; i++)
                            for (int j = 0; j < cols; j++)
                                if (!cells.getCell(i, j).isFlagged())
                                    ib[i][j].setImageDrawable(images[cells.getCell(i, j).getValue() + 1]);
                                else if (cells.getCell(i, j).getValue() != -1)
                                    ib[i][j].setImageDrawable(flag2Drawable);
                        break;
                    case 0:
                        ib[rowPos][colPos].setImageDrawable(images[cells.getCell(rowPos, colPos).getValue() + 1]);

                        // TopLeft
                        if (insideBounds(rowPos - 1, colPos - 1))
                            revealCell(rowPos - 1, colPos - 1);

                        // Top
                        if (insideBounds(rowPos - 1, colPos))
                            revealCell(rowPos - 1, colPos);

                        // TopRight
                        if (insideBounds(rowPos - 1, colPos + 1))
                            revealCell(rowPos - 1, colPos + 1);

                        // Left
                        if (insideBounds(rowPos, colPos - 1))
                            revealCell(rowPos, colPos - 1);

                        // Right
                        if (insideBounds(rowPos, colPos + 1))
                            revealCell(rowPos, colPos + 1);

                        // BottomLeft
                        if (insideBounds(rowPos + 1, colPos - 1))
                            revealCell(rowPos + 1, colPos - 1);

                        // Bottom
                        if (insideBounds(rowPos + 1, colPos))
                            revealCell(rowPos + 1, colPos);

                        // BottomRight
                        if (insideBounds(rowPos + 1, colPos + 1))
                            revealCell(rowPos + 1, colPos + 1);

                        break;
                    default:
                        ib[rowPos][colPos].setImageDrawable(images[cells.getCell(rowPos, colPos).getValue() + 1]);
                        break;
                }
            }
        }
    }

    public void flagSwitch (View view) {
        flag = !flag;
        if (flag)
            view.setBackground(flagDrawable);
        else
            view.setBackground(bombDrawable);
    }

    private boolean insideBounds (int r, int c) {
        return !(r < 0 || c < 0 || r >= rows || c >= cols);
    }

}
