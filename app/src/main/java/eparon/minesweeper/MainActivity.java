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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import eparon.minesweeper.Game.Board;
import eparon.minesweeper.Game.Cell;
import eparon.minesweeper.Game.Difficulty;

@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class MainActivity extends AppCompatActivity {

    public String PREFS_MS = "MSPrefsFile";
    SharedPreferences prefs;

    int gameTurn;
    boolean win, flag = false;

    Board board;
    int rows = 18, cols = 12;
    int numberOfBombs, bombs, flags;
    double difficulty = Difficulty.HARD;

    boolean longpress = true, vibration = true, showADRG = true;
    int[] bestTime = new int[4];

    GridLayout gridLayout;
    int slotDimensions;

    FrameLayout[][] fl;
    ImageButton[][] ib;

    Drawable[] images = new Drawable[10];
    int[] imagesResID = new int[]{R.drawable.bomb, R.drawable.cell_empty, R.drawable.cell_1, R.drawable.cell_2, R.drawable.cell_3,
            R.drawable.cell_4, R.drawable.cell_5, R.drawable.cell_6, R.drawable.cell_7, R.drawable.cell_8};
    Drawable cuaDrawable, flagDrawable, flag2Drawable, bombDrawable, smileyDrawable, smiley2Drawable, smiley3Drawable;

    TextView BombsCounter;
    ImageView smiley;

    PopupWindow pw, ad;
    boolean adRunning = false;

    TextView[] TimerText = new TextView[2];
    long startTime = 0;

    //region Timer
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run () {
            long millis = System.currentTimeMillis() - startTime;

            TimerText[0].setText(String.format(Locale.getDefault(), "%02d", (int)(millis / 60000))); // Minutes
            TimerText[1].setText(String.format(Locale.getDefault(), "%02d", (int)(millis / 1000) % 60)); // Seconds

            timerHandler.postDelayed(this, 500);
        }
    };
    //endregion

    @Override
    public void onBackPressed () {
        if (adRunning) newGameAlert3();
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_MS, Context.MODE_PRIVATE);

        rows = prefs.getInt("rows", rows);
        cols = prefs.getInt("cols", cols);
        difficulty = Double.parseDouble(prefs.getString("difficulty", String.valueOf(difficulty)));
        longpress = prefs.getBoolean("longpress", longpress);
        vibration = prefs.getBoolean("vibration", vibration);
        showADRG = prefs.getBoolean("showADRG", showADRG);
        for (int i = 0; i < bestTime.length; i++)
            bestTime[i] = prefs.getInt("bestTime" + i, bestTime[i]);

        board = new Board(rows, cols);
        numberOfBombs = (int)((rows * cols) / difficulty);

        fl = new FrameLayout[rows][cols];
        ib = new ImageButton[rows][cols];

        initializeDrawables();

        TimerText[0] = findViewById(R.id.timer1);
        TimerText[1] = findViewById(R.id.timer2);
        BombsCounter = findViewById(R.id.bombsCounter);

        findViewById(R.id.switchPointer).setBackground(bombDrawable);
        findViewById(R.id.timerll).setBackground(initializeDrawable(R.drawable.timer_background));
        BombsCounter.setBackground(initializeDrawable(R.drawable.bombs_counter_background));

        smiley = findViewById(R.id.smiley);
        smiley.setImageDrawable(smileyDrawable);
        smiley.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    smiley.setImageDrawable(smiley3Drawable);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (showADRG && board.getState() && !adRunning && !win && gameTurn != 0)
                        newGameAlert();
                    else if (!adRunning)
                        Init(false);
                    new Handler().postDelayed(() -> smiley.setImageDrawable(smileyDrawable), 50);
                    break;
            }
            return true;
        });

        // Getting the Screen Size and converting it into the Slots Dimensions;
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        slotDimensions = (size.x - (int)(scale + 0.5f)) / cols;

        gridLayout = findViewById(R.id.gl);
        gridLayout.setColumnCount(cols);
        gridLayout.setRowCount(rows);

        Init(true);
    }

    private void Init (boolean onAppStart) {
        if (pw != null) pw.dismiss();

        gameTurn = 0;
        win = false;
        bombs = numberOfBombs;
        flags = numberOfBombs;

        timerHandler.removeCallbacks(timerRunnable);
        TimerText[0].setText("00");
        TimerText[1].setText("00");
        BombsCounter.setText(String.format(Locale.getDefault(), "%02d", flags));

        board.resetState();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                if (onAppStart) {
                    View v = View.inflate(this, R.layout.cell, null);
                    fl[i][j] = v.findViewById(R.id.fl);
                    ib[i][j] = v.findViewById(R.id.image);

                    if (fl[i][j].getParent() != null) ((ViewGroup)fl[i][j].getParent()).removeView(fl[i][j]);

                    GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                    lp.width = slotDimensions;
                    lp.height = slotDimensions;
                    gridLayout.addView(fl[i][j], lp);
                }

                ib[i][j].setImageDrawable(cuaDrawable);

                final int finalI = i, finalJ = j;
                ib[i][j].setOnClickListener(view -> clickCell(finalI, finalJ, flag));

                if (longpress) ib[i][j].setOnLongClickListener(v -> {
                    Vibrator vibrator = (Vibrator)Objects.requireNonNull(getSystemService(VIBRATOR_SERVICE));
                    if (vibration && !board.getCell(finalI, finalJ).isRevealed() && board.getState())
                        if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                        else                             vibrator.vibrate(100);
                    clickCell(finalI, finalJ, !flag);
                    return true;
                });
            }
        }

        // Set the GridLayout's height the same size as it's width
        ViewGroup.LayoutParams lp = gridLayout.getLayoutParams();
        lp.height = lp.width;
        gridLayout.setLayoutParams(lp);
    }

    private void startBoardGeneration (int firstCell) {

        Random r = new Random();
        int counter = 0;

        while (bombs != 0) {
            if (counter >= cols * rows) counter = 0;

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

    private void revealCell (int rowPos, int colPos, boolean putFlag) {
        Cell currentCell = board.getCell(rowPos, colPos);

        // If it hasn't been clicked yet.
        if (!currentCell.isRevealed()) {

            if (putFlag) {
                if (!currentCell.isFlagged()) {
                    if (flags > 0) {
                        ib[rowPos][colPos].setImageDrawable(flagDrawable);
                        flags--;
                        currentCell.setFlagged(!currentCell.isFlagged());
                    }
                } else {
                    ib[rowPos][colPos].setImageDrawable(cuaDrawable);
                    flags++;
                    currentCell.setFlagged(!currentCell.isFlagged());
                }
                BombsCounter.setText(String.format(Locale.getDefault(), "%02d", flags));

            } else if (!currentCell.isFlagged()) {
                currentCell.setRevealed(true);

                switch (currentCell.getValue()) {
                    case -1: // Bomb
                        gameTurn = 0;
                        smiley.setImageDrawable(smiley2Drawable);
                        timerHandler.removeCallbacks(timerRunnable);
                        for (int i = 0; i < rows; i++)
                            for (int j = 0; j < cols; j++)
                                if (!board.getCell(i, j).isFlagged())   ib[i][j].setImageDrawable(images[board.getCell(i, j).getValue() + 1]);
                                else if (!board.getCell(i, j).isBomb()) ib[i][j].setImageDrawable(flag2Drawable);
                        board.setState(false);
                        break;
                    case 0: // 0
                        ib[rowPos][colPos].setImageDrawable(images[currentCell.getValue() + 1]);
                        revealSurroundingCells(rowPos, colPos);
                        break;
                    default:
                        ib[rowPos][colPos].setImageDrawable(images[currentCell.getValue() + 1]);
                        break;
                }

                gameTurn++;
                if (gameTurn == rows * cols - numberOfBombs) winGame();
            }
        }
    }

    private void winGame () {
        win = true;
        board.setState(false);

        // Creating the custom Popup message.
        View customView = View.inflate(this, R.layout.popup_layout, null);

        pw = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pw.setElevation(5.0f);
        pw.setAnimationStyle(R.style.PopupWindowAnimation);
        pw.showAtLocation(findViewById(R.id.cl), Gravity.CENTER, 0, 0);

        int time = Integer.parseInt(TimerText[0].getText().toString()) * 60 + Integer.parseInt(TimerText[1].getText().toString());
        int pos = Difficulty.valueToPosition(difficulty);

        if (time < bestTime[pos] || bestTime[pos] == 0) {
            bestTime[pos] = time;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("bestTime" + pos, bestTime[pos]);
            editor.apply();
        }

        ((TextView)customView.findViewById(R.id.timeMsg)).setText(String.format("%s:%s", TimerText[0].getText().toString(), TimerText[1].getText().toString()));
        ((TextView)customView.findViewById(R.id.bestTimeMsg)).setText(String.format(Locale.getDefault(), "%02d:%02d", bestTime[pos] / 60, bestTime[pos] % 60));

        timerHandler.removeCallbacks(timerRunnable);
    }

    private void clickCell (final int r, final int c, boolean clickType) {
        if (board.getState()) {
            if (board.getCell(r, c).isRevealed()) {
                if (board.countSurroundingFlags(r, c) >= board.getCell(r, c).getValue())
                    revealSurroundingCells(r, c);
            } else {
                if (gameTurn == 0 && !clickType) {
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable, 0);
                    startBoardGeneration(r * cols + c);
                }
                revealCell(r, c, clickType);
            }
        }
    }

    private void revealSurroundingCells (final int r, final int c) {
        for (int i = 0; i < 8; i++)
            if (board.inbounds(r + Board.neighboursLoop[i][0], c + Board.neighboursLoop[i][1]))
                revealCell(r + Board.neighboursLoop[i][0], c + Board.neighboursLoop[i][1], false);
    }

    private Drawable initializeDrawable (int resID) {
        Drawable d = getDrawable(resID);
        assert d != null;
        d.setFilterBitmap(false);
        return d;
    }

    private void initializeDrawables () {
        cuaDrawable = initializeDrawable(R.drawable.cell_unassigned);
        flagDrawable = initializeDrawable(R.drawable.flag);
        flag2Drawable = initializeDrawable(R.drawable.flag2);
        bombDrawable = initializeDrawable(R.drawable.bomb2);
        smileyDrawable = initializeDrawable(R.drawable.smiley);
        smiley2Drawable = initializeDrawable(R.drawable.smiley2);
        smiley3Drawable = initializeDrawable(R.drawable.smiley3);

        for (int i = 0; i < images.length; i++) images[i] = initializeDrawable(imagesResID[i]);
    }

    public void flagSwitch (View view) {
        flag = !flag;
        if (flag) view.setBackground(flagDrawable);
        else      view.setBackground(bombDrawable);
    }

    /**
     * This function is the function that displays the new game alert.
     * <p>
     * It has 2 helper functions - 'newGameAlert2' & 'newGameAlert3'.
     */
    private void newGameAlert () {
        if (ad != null) ad.dismiss();
        adRunning = true;
        board.setState(false);

        // Creating the custom AlertDialog
        View customView = View.inflate(this, R.layout.custom_alertdialog, null);

        ad = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ad.setElevation(5.0f);
        ad.setAnimationStyle(R.style.PopupWindowAnimation);
        ad.showAtLocation(findViewById(R.id.cl), Gravity.CENTER, 0, 0);

        ((TextView)customView.findViewById(R.id.title)).setText(R.string.ad_rg_title);
        ((TextView)customView.findViewById(R.id.message)).setText(R.string.ad_rg_message);
        TextView Positive = customView.findViewById(R.id.positive);
        TextView Negative = customView.findViewById(R.id.negative);
        final CheckBox CheckBox = customView.findViewById(R.id.cb);

        Positive.setText(R.string.ad_rg_positive);
        Negative.setText(R.string.ad_rg_negative);

        Positive.setOnClickListener(view -> {
            newGameAlert2(CheckBox.isChecked());
            Init(false);
        });

        Negative.setOnClickListener(view -> newGameAlert2(CheckBox.isChecked()));
    }

    /**
     * This function is the helper function of 'newGameAlert'.
     *
     * @param checked "don't show this message again" checkbox value
     */
    private void newGameAlert2 (boolean checked) {
        showADRG = !checked;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("showADRG", showADRG);
        editor.apply();
        newGameAlert3();
    }

    /**
     * This function is the helper function of 'newGameAlert' & 'newGameAlert2'.
     */
    private void newGameAlert3 () {
        board.setState(true);
        adRunning = false;
        ad.dismiss();
    }

    public void goSettings (View view) {
        startActivity(new Intent(MainActivity.this, Settings.class));
    }

}
