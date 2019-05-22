package eparon.minesweeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.*;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridLayout.LayoutParams;
import android.widget.GridLayout.Spec;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import eparon.minesweeper.Game.Board;
import eparon.minesweeper.Game.Difficulty;

@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class MainActivity extends AppCompatActivity {

    public String PREFS_OVH = "OVHPrefsFile";
    SharedPreferences prefs;

    int gameTurn;
    int firstCell;

    int rows = 18;
    int cols = 12;
    Board board;
    int numberOfBombs;
    int bombs, flags;
    boolean flag = false;
    double difficulty = Difficulty.HARD;
    boolean longpress = true, vibration = true;

    GridLayout gridLayout;
    Spec[] row, col;
    int slotWidth, slotHeight;

    FrameLayout[][] fl;
    ImageButton[][] ib;

    Drawable[] images = new Drawable[10];
    int[] imagesResID = new int[]{R.drawable.bomb, R.drawable.cell_empty, R.drawable.cell_1,
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

    PopupWindow pw;

    @Override
    public void onBackPressed() { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_OVH, Context.MODE_PRIVATE);

        rows = prefs.getInt("rows", rows);
        cols = prefs.getInt("cols", cols);
        difficulty = Double.parseDouble(prefs.getString("difficulty", String.valueOf(difficulty)));
        longpress = prefs.getBoolean("longpress", longpress);
        vibration = prefs.getBoolean("vibration", vibration);

        board = new Board(rows, cols);
        numberOfBombs = (int)((rows * cols) / difficulty);

        row = new Spec[rows];
        col = new Spec[cols];

        fl = new FrameLayout[rows][cols];
        ib = new ImageButton[rows][cols];

        cuaDrawable = initializeDrawable(R.drawable.cell_unassigned);
        flagDrawable = initializeDrawable(R.drawable.flag);
        flag2Drawable = initializeDrawable(R.drawable.flag2);
        bombDrawable = initializeDrawable(R.drawable.bomb2);
        smileyDrawable = initializeDrawable(R.drawable.smiley);
        smiley2Drawable = initializeDrawable(R.drawable.smiley2);
        smiley3Drawable = initializeDrawable(R.drawable.smiley3);

        for (int i = 0; i < images.length; i++)
            images[i] = initializeDrawable(imagesResID[i]);

        TimerText[0] = findViewById(R.id.timer1);
        TimerText[1] = findViewById(R.id.timer2);
        BombsCounter = findViewById(R.id.bombsCounter);

        findViewById(R.id.timerll).setBackground(initializeDrawable(R.drawable.timer_background));
        BombsCounter.setBackground(initializeDrawable(R.drawable.bombs_counter_background));

        findViewById(R.id.switchPointer).setBackground(bombDrawable);

        smiley = findViewById(R.id.smiley);
        smiley.setImageDrawable(smileyDrawable);
        smiley.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
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

        deleteCache(this);

        gameTurn = 0;

        timerHandler.removeCallbacks(timerRunnable);
        TimerText[0].setText("00");
        TimerText[1].setText("00");

        if (pw != null) pw.dismiss();

        bombs = numberOfBombs;
        flags = numberOfBombs;

        BombsCounter.setText(String.format(Locale.getDefault(),"%02d", flags));

        board.resetState();

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
                        clickCell(finalI, finalJ, flag);
                    }
                });
                if (longpress) {
                    ib[i][j].setOnLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (vibration && !board.getCell(finalI, finalJ).isClicked())
                                if (Build.VERSION.SDK_INT >= 26)
                                    ((Vibrator) Objects.requireNonNull(getSystemService(VIBRATOR_SERVICE))).vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                                else
                                    ((Vibrator) Objects.requireNonNull(getSystemService(VIBRATOR_SERVICE))).vibrate(100);
                            clickCell(finalI, finalJ, !flag);
                            return true;
                        }
                    });
                }
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

            if (r.nextDouble() < 0.10 && !board.getCell(counter / cols, counter % cols).isBomb()
                    && counter != firstCell - 1 - cols && counter != firstCell - cols && counter != firstCell + 1 - cols
                    && counter != firstCell - 1        && counter != firstCell        && counter != firstCell + 1
                    && counter != firstCell - 1 + cols && counter != firstCell + cols && counter != firstCell + 1 + cols) {
                board.getCell(counter / cols, counter % cols).setValue(-1);
                bombs--;
            }
            counter++;
        }

        board.detectBombs();
    }


    private void revealCell(int rowPos, int colPos, boolean putFlag) {

        // If it hasn't been clicked yet.
        if (!board.getCell(rowPos, colPos).isClicked()) {
            if (putFlag) {
                if (!board.getCell(rowPos, colPos).isFlagged()) {
                    if (flags > 0) {
                        ib[rowPos][colPos].setImageDrawable(flagDrawable);
                        flags--;
                    } else {
                        board.getCell(rowPos, colPos).setFlagged(!board.getCell(rowPos, colPos).isFlagged());
                    }
                } else {
                    ib[rowPos][colPos].setImageDrawable(cuaDrawable);
                    flags++;
                }
                BombsCounter.setText(String.format(Locale.getDefault(), "%02d", flags));
                board.getCell(rowPos, colPos).setFlagged(!board.getCell(rowPos, colPos).isFlagged());
            } else if (!board.getCell(rowPos, colPos).isFlagged()) {

                board.getCell(rowPos, colPos).setClicked(true);

                switch (board.getCell(rowPos, colPos).getValue()) {
                    case -1:
                        gameTurn = 0;
                        smiley.setImageDrawable(smiley2Drawable);
                        timerHandler.removeCallbacks(timerRunnable);
                        for (int i = 0; i < rows; i++)
                            for (int j = 0; j < cols; j++)
                                if (!board.getCell(i, j).isFlagged())
                                    ib[i][j].setImageDrawable(images[board.getCell(i, j).getValue() + 1]);
                                else if (board.getCell(i, j).getValue() != -1)
                                    ib[i][j].setImageDrawable(flag2Drawable);
                        board.Disable();
                        break;
                    case 0:
                        ib[rowPos][colPos].setImageDrawable(images[board.getCell(rowPos, colPos).getValue() + 1]);
                        revealSurroundingCells(rowPos, colPos);
                        break;
                    default:
                        ib[rowPos][colPos].setImageDrawable(images[board.getCell(rowPos, colPos).getValue() + 1]);
                        break;
                }

                gameTurn++;

                if (gameTurn == rows * cols - numberOfBombs)
                    winGame();
            }
        }
    }

    private void winGame () {

        board.Disable();

        // Creating the custom Popup message
        View customView = View.inflate(this, R.layout.popup_layout, null);

        pw = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pw.setElevation(5.0f);
        pw.showAtLocation(findViewById(R.id.cl), Gravity.CENTER,0,0);

        TextView Message = customView.findViewById(R.id.message);
        Message.setText(String.format("%s\nTime: %s:%s", getResources().getString(R.string.win_msg), TimerText[0].getText().toString(), TimerText[1].getText().toString()));

        timerHandler.removeCallbacks(timerRunnable);
    }

    private void clickCell (final int r, final int c, boolean clickType) {
        if (board.getCell(r, c).isClicked()) {
            if (board.countSurroundingFlags(r, c) >= board.getCell(r, c).getValue())
                revealSurroundingCells(r, c);
        } else {
            if (gameTurn == 0 && !clickType) {
                startTime = System.currentTimeMillis();
                timerHandler.postDelayed(timerRunnable, 0);
                firstCell = r * cols + c;
                startBoardGeneration();
            }
            revealCell(r, c, clickType);
        }
    }

    private void revealSurroundingCells (final int r, final int c) {
        RSC2(r - 1, c - 1); // Top Left
        RSC2(r - 1, c    ); // Top
        RSC2(r - 1, c + 1); // Top Right
        RSC2(r    , c - 1); // Left
        RSC2(r    , c + 1); // Right
        RSC2(r + 1, c - 1); // Bottom Left
        RSC2(r + 1, c    ); // Bottom
        RSC2(r + 1, c + 1); // Bottom Right
    }

    private void RSC2(final int r, final int c) {
        if (board.inbounds(r, c)) revealCell(r, c, false);
    }

    private Drawable initializeDrawable (int resID) {
        Drawable d = getDrawable(resID);
        assert d != null;
        d.setFilterBitmap(false);
        return d;
    }

    public void flagSwitch (View view) {
        flag = !flag;
        if (flag) view.setBackground(flagDrawable);
        else view.setBackground(bombDrawable);
    }

    public void goSettings (View view) {
        startActivity(new Intent(MainActivity.this, Settings.class));
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children)
                if (!deleteDir(new File(dir, child)))
                    return false;
            return dir.delete();
        } else if (dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

}
