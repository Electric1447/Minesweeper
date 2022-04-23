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
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Locale;
import java.util.Objects;

import eparon.minesweeper.Game.Board;
import eparon.minesweeper.Game.Cell;
import eparon.minesweeper.Game.Difficulty;

@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class MainActivity extends AppCompatActivity {

    public static String PREFS_MS = "MSPrefsFile";
    SharedPreferences prefs;

    Board board;
    int rows = 18, cols = 12;
    int gameTurn, numberOfBombs, flags;
    boolean win, flag = false, longpress = true, vibration = true, showADRG = true;
    double difficulty = Difficulty.HARD;
    int[] bestTime = new int[4];
    long startTime = 0;

    GridLayout gridLayout;
    ImageButton[][] ib;

    TextView[] TimerText = new TextView[2];
    TextView BombsCounter;
    ImageView smiley;

    PopupWindow pw, ad;
    boolean adRunning = false;

    Drawable[] images = new Drawable[10];
    int[] imagesResID = new int[] {R.drawable.bomb, R.drawable.cell_empty, R.drawable.cell_1, R.drawable.cell_2, R.drawable.cell_3,
            R.drawable.cell_4, R.drawable.cell_5, R.drawable.cell_6, R.drawable.cell_7, R.drawable.cell_8};
    Drawable cuaDrawable, flagDrawable, flag2Drawable, bombDrawable, smileyDrawable, smiley2Drawable, smiley3Drawable;

    //region Timer region
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
        if (adRunning)
            newGameAlert3();
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_MS, Context.MODE_PRIVATE);

        rows = prefs.getInt("rows", rows);
        cols = prefs.getInt("cols", cols);
        difficulty = Double.parseDouble(Objects.requireNonNull(prefs.getString("difficulty", String.valueOf(difficulty))));
        longpress = prefs.getBoolean("longpress", longpress);
        vibration = prefs.getBoolean("vibration", vibration);
        showADRG = prefs.getBoolean("showADRG", showADRG);
        for (int i = 0; i < bestTime.length; i++)
            bestTime[i] = prefs.getInt("bestTime" + i, bestTime[i]);

        board = new Board(rows, cols);
        numberOfBombs = (int)((rows * cols) / difficulty);

        ib = new ImageButton[rows][cols];

        initializeDrawables();

        TimerText[0] = findViewById(R.id.timer1);
        TimerText[1] = findViewById(R.id.timer2);
        BombsCounter = findViewById(R.id.bombsCounter);
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
                        Init();
                    new Handler().postDelayed(() -> smiley.setImageDrawable(smileyDrawable), 50);
                    break;
            }
            return true;
        });

        Init();
        DrawBoard();
    }

    /**
     * This method initializes the game.
     */
    private void Init () {
        if (pw != null) pw.dismiss();

        gameTurn = 0;
        win = false;
        flags = numberOfBombs;

        timerHandler.removeCallbacks(timerRunnable);
        TimerText[0].setText("00");
        TimerText[1].setText("00");
        BombsCounter.setText(String.format(Locale.getDefault(), "%02d", flags));

        board.resetState();

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (ib[r][c] != null) ib[r][c].setImageDrawable(cuaDrawable);
    }

    /**
     * This method draws the board.
     */
    private void DrawBoard () {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;

        gridLayout = findViewById(R.id.gl);
        gridLayout.setColumnCount(cols);
        gridLayout.setRowCount(rows);

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {

                View v = View.inflate(this, R.layout.cell, null);
                ib[r][c] = v.findViewById(R.id.ib);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.width = (size.x - (int)(scale + 0.5f)) / cols;
                gridLayout.addView(ib[r][c], lp);

                ib[r][c].setImageDrawable(cuaDrawable);
                final int finalR = r, finalC = c;

                ib[r][c].setOnClickListener(view -> clickCell(finalR, finalC, flag));

                if (longpress) ib[r][c].setOnLongClickListener(view -> {
                    Vibrator vibrator = (Vibrator)Objects.requireNonNull(getSystemService(VIBRATOR_SERVICE));
                    if (vibration && board.getState() && !board.getCell(finalR, finalC).isRevealed()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
                        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                        else
                            vibrator.vibrate(100);
                    }

                    clickCell(finalR, finalC, !flag);
                    return true;
                });
            }
    }

    /**
     * This method is called when the user clicks on a cell.
     *
     * @param r         cell's row position
     * @param c         cell's column position
     * @param clickType the type of the click
     */
    private void clickCell (final int r, final int c, boolean clickType) {
        // If the board is disabled you cannot click a cell.
        if (!board.getState())
            return;

        // First turn.
        if (gameTurn == 0 && !clickType) {
            if (board.isFlagAt(r, c))
                return;

            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
            board.startBoardGeneration(r * cols + c, numberOfBombs);
        }

        if (!board.getCell(r, c).isRevealed())
            revealCell(r, c, clickType);
        else if (board.countSurroundingFlags(r, c) >= board.getCell(r, c).getValue())
            revealSurroundingCells(r, c);
    }

    /**
     * This method is called when the user wants to reveal/put flag on a cell.
     *
     * @param rowPos  cell's row position
     * @param colPos  cell's column position
     * @param putFlag the current click action (Flag/Bomb)
     */
    private void revealCell (int rowPos, int colPos, boolean putFlag) {
        Cell currentCell = board.getCell(rowPos, colPos);
        if (currentCell.isRevealed())
            return; // If the cell has been clicked already.

        if (putFlag) { // If the user wants to put a flag on the cell.
            if (currentCell.isFlagged()) {
                ib[rowPos][colPos].setImageDrawable(cuaDrawable);
                flags++;
                currentCell.setFlagged(!currentCell.isFlagged());
            } else if (flags > 0) {
                ib[rowPos][colPos].setImageDrawable(flagDrawable);
                flags--;
                currentCell.setFlagged(!currentCell.isFlagged());
            }
            BombsCounter.setText(String.format(Locale.getDefault(), "%02d", flags));
        } else if (!currentCell.isFlagged()) {
            // If the user wants to put a bomb on the cell.
            currentCell.setRevealed(true); // Reveal the cell;

            if (currentCell.isBomb()) {
                // User clicked on a bomb -> Lost the game.
                smiley.setImageDrawable(smiley2Drawable);
                timerHandler.removeCallbacks(timerRunnable);

                for (int r = 0; r < rows; r++)
                    for (int c = 0; c < cols; c++)
                        if (!board.isFlagAt(r, c))
                            ib[r][c].setImageDrawable(images[board.getCell(r, c).getValue() + 1]);
                        else if (!board.isMineAt(r, c))
                            ib[r][c].setImageDrawable(flag2Drawable);

                board.setState(false);
            } else {
                ib[rowPos][colPos].setImageDrawable(images[currentCell.getValue() + 1]);
                if (currentCell.getValue() == 0)
                    revealSurroundingCells(rowPos, colPos);
            }

            gameTurn++;
            if (gameTurn == rows * cols - numberOfBombs && board.getState())
                winGame(); // Win check.
        }
    }

    /**
     * This method calls the function 'revealCell' on all of the surrounding cells of the given cell.
     *
     * @param r cell's row position
     * @param c cell's column position
     */
    private void revealSurroundingCells (final int r, final int c) {
        for (int i = 0; i < 8; i++)
            if (board.inbounds(r + Board.neighboursLoop[i][0], c + Board.neighboursLoop[i][1]))
                revealCell(r + Board.neighboursLoop[i][0], c + Board.neighboursLoop[i][1], false);
    }

    /**
     * This method is called when the user wins the game.
     */
    private void winGame () {
        win = true;
        board.setState(false);

        // Creating the custom Popup message.
        View wgView = View.inflate(this, R.layout.popup_layout, null);
        pw = new PopupWindow(wgView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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

        ((TextView)wgView.findViewById(R.id.timeMsg)).setText(String.format("%s:%s", TimerText[0].getText().toString(), TimerText[1].getText().toString()));
        ((TextView)wgView.findViewById(R.id.bestTimeMsg)).setText(String.format(Locale.getDefault(), "%02d:%02d", bestTime[pos] / 60, bestTime[pos] % 60));

        timerHandler.removeCallbacks(timerRunnable);
    }

    //region newGameAlert functions region

    /**
     * This method is the function that displays the new game alert.
     * <p>
     * It has 2 helper functions - 'newGameAlert2' & 'newGameAlert3'.
     */
    private void newGameAlert () {
        if (ad != null) ad.dismiss();
        adRunning = true;
        board.setState(false);

        // Creating the custom AlertDialog
        View ngaView = View.inflate(this, R.layout.custom_alertdialog, null);
        ad = new PopupWindow(ngaView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ad.setElevation(5.0f);
        ad.setAnimationStyle(R.style.PopupWindowAnimation);
        ad.showAtLocation(findViewById(R.id.cl), Gravity.CENTER, 0, 0);

        ((TextView)ngaView.findViewById(R.id.title)).setText(R.string.ad_rg_title);
        ((TextView)ngaView.findViewById(R.id.message)).setText(R.string.ad_rg_message);
        TextView Positive = ngaView.findViewById(R.id.positive);
        TextView Negative = ngaView.findViewById(R.id.negative);
        final CheckBox CheckBox = ngaView.findViewById(R.id.cb);

        Positive.setText(R.string.ad_rg_positive);
        Negative.setText(R.string.ad_rg_negative);

        Positive.setOnClickListener(view -> newGameAlert2(CheckBox.isChecked(), true));
        Negative.setOnClickListener(view -> newGameAlert2(CheckBox.isChecked(), false));
    }

    /**
     * This method is the helper method of 'newGameAlert'.
     *
     * @param checked "don't show this message again" checkbox value
     */
    private void newGameAlert2 (boolean checked, boolean result) {
        showADRG = !checked;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("showADRG", showADRG);
        editor.apply();
        newGameAlert3();

        if (result)
            Init();
    }

    /**
     * This method is the helper method of 'newGameAlert' & 'newGameAlert2'.
     */
    private void newGameAlert3 () {
        board.setState(true);
        adRunning = false;
        ad.dismiss();
    }
    //endregion

    /**
     * This method switches the current click action. (Flag/Bomb), it is called by the button in the titlebar.
     */
    public void flagSwitch (View view) {
        flag = !flag;
        view.setBackground(flag ? flagDrawable : bombDrawable);
    }

    //region Drawable region

    /**
     * This function returns a drawable without the bilinear filter that Android gives images by default.
     *
     * @param resID the drawable's resource id
     * @return Drawable; This returns the drawable without the bilinear filter.
     */
    private Drawable initializeDrawable (int resID) {
        Drawable d = ContextCompat.getDrawable(this, resID);
        assert d != null;
        d.setFilterBitmap(false);
        return d;
    }

    /**
     * This method initializes all drawables.
     */
    private void initializeDrawables () {
        cuaDrawable = initializeDrawable(R.drawable.cell_unassigned);
        flagDrawable = initializeDrawable(R.drawable.flag);
        flag2Drawable = initializeDrawable(R.drawable.flag2);
        bombDrawable = initializeDrawable(R.drawable.bomb2);
        smileyDrawable = initializeDrawable(R.drawable.smiley);
        smiley2Drawable = initializeDrawable(R.drawable.smiley2);
        smiley3Drawable = initializeDrawable(R.drawable.smiley3);

        for (int i = 0; i < images.length; i++)
            images[i] = initializeDrawable(imagesResID[i]);

        findViewById(R.id.switchPointer).setBackground(bombDrawable);
        findViewById(R.id.timerll).setBackground(initializeDrawable(R.drawable.timer_background));
    }
    //endregion

    /**
     * This function opens the Settings activity.
     */
    public void goToSettings (View view) {
        startActivity(new Intent(MainActivity.this, Settings.class));
    }

}
