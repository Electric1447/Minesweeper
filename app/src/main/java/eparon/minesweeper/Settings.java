package eparon.minesweeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import eparon.minesweeper.Game.Difficulty;

public class Settings extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    SharedPreferences prefs;

    int rows = 18, cols = 12;
    TextView Rows, Cols;

    double difficulty = Difficulty.HARD;
    Spinner diffSpinner;

    boolean longpress = true, vibration = true, showADRG = true, showADOOB = true;
    CheckBox longpressCB, vibrationCB, adrgCB;

    PopupWindow ad;

    @Override
    public void onBackPressed () {
        Save();
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(MainActivity.PREFS_MS, Context.MODE_PRIVATE);

        rows = prefs.getInt("rows", rows);
        cols = prefs.getInt("cols", cols);
        difficulty = Double.parseDouble(prefs.getString("difficulty", String.valueOf(difficulty)));
        longpress = prefs.getBoolean("longpress", longpress);
        vibration = prefs.getBoolean("vibration", vibration);
        showADRG = prefs.getBoolean("showADRG", showADRG);
        showADOOB = prefs.getBoolean("showADOOB", showADOOB);

        Rows = findViewById(R.id.rText);
        Cols = findViewById(R.id.cText);
        Rows.setHint(String.format(Locale.getDefault(), "%02d", rows));
        Cols.setHint(String.format(Locale.getDefault(), "%02d", cols));

        diffSpinner = findViewById(R.id.diff_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.difficulty_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        diffSpinner.setAdapter(adapter);
        diffSpinner.setOnItemSelectedListener(this);
        diffSpinner.setSelection(Difficulty.valueToPosition(difficulty));

        longpressCB = findViewById(R.id.cbLongpress);
        longpressCB.setChecked(longpress);
        vibrationCB = findViewById(R.id.cbVibration);
        vibrationCB.setChecked(vibration);
        adrgCB = findViewById(R.id.cbADRG);
        adrgCB.setChecked(showADRG);

        ((TextView)findViewById(R.id.ver)).setText(String.format("Version %s\nCreated by Itai Levin.", BuildConfig.VERSION_NAME)); // Set version TextView.
    }

    //region Spinner select Override functions region
    @Override
    public void onItemSelected (AdapterView<?> adapterView, View view, int position, long l) {
        difficulty = Difficulty.positionToValue(position);
    }

    @Override
    public void onNothingSelected (AdapterView<?> adapterView) {
    }
    //endregion

    //region Click actions region
    public void setLongpress (View view) {
        longpressCB.setChecked(!longpressCB.isChecked());
        longpress = longpressCB.isChecked();
    }

    public void setVibration (View view) {
        vibrationCB.setChecked(!vibrationCB.isChecked());
        vibration = vibrationCB.isChecked();
    }

    public void setADRG (View view) {
        adrgCB.setChecked(!adrgCB.isChecked());
        showADRG = adrgCB.isChecked();
    }

    public void goBack (View view) {
        Save();
    }
    //endregion

    //region Save region

    /**
     * This method is the main save method.
     * <p>
     * It also calls the a helper methods - 'Save2' & 'AlertDialogOOB'.
     */
    private void Save () {
        if (!Rows.getText().toString().equals("")) rows = Integer.parseInt(Rows.getText().toString());
        if (!Cols.getText().toString().equals("")) cols = Integer.parseInt(Cols.getText().toString());

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int abh = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) abh = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics()); // Get ActionBar Size

        if (showADOOB && ((size.x / cols) > ((size.y - abh) / rows)))
            AlertDialogOOB(); // If "Out of bounds" display warning.
        else
            Save2();
    }

    /**
     * This method is the helper method of 'Save'.
     */
    private void AlertDialogOOB () {
        if (ad != null) ad.dismiss();

        // Creating the custom AlertDialog
        View adoobView = View.inflate(this, R.layout.custom_alertdialog, null);
        ad = new PopupWindow(adoobView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ad.setElevation(5.0f);
        ad.setAnimationStyle(R.style.PopupWindowAnimation);
        ad.showAtLocation(findViewById(R.id.cl), Gravity.CENTER, 0, 0);

        ((TextView)adoobView.findViewById(R.id.title)).setText(R.string.ad_oob_title); // Title
        ((TextView)adoobView.findViewById(R.id.message)).setText(R.string.ad_oob_message); // Message
        TextView Positive = adoobView.findViewById(R.id.positive); // Positive button
        TextView Negative = adoobView.findViewById(R.id.negative); // Negative button
        final CheckBox CheckBox = adoobView.findViewById(R.id.cb); // Checkbox

        Positive.setText(R.string.ad_oob_positive);
        Negative.setText(R.string.ad_oob_negative);

        Positive.setOnClickListener(view -> AlertDialogOOB2(CheckBox.isChecked(), true));
        Negative.setOnClickListener(view -> AlertDialogOOB2(CheckBox.isChecked(), false));
    }

    /**
     * This method is the helper method of 'AlertDialogOOB'.
     *
     * @param checked the AlertDialog's checkbox state
     */
    private void AlertDialogOOB2 (boolean checked, boolean result) {
        showADOOB = !checked;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("showADOOB", showADOOB);
        editor.apply();
        ad.dismiss();
        if (result) Save2();
    }

    /**
     * This method is the helper method of 'Save'.
     */
    private void Save2 () {
        if (rows > 24 || rows < 9) {
            Toast.makeText(this, R.string.err_invalid_rows, Toast.LENGTH_LONG).show();
        } else if (cols > 16 || cols < 6) {
            Toast.makeText(this, R.string.err_invalid_cols, Toast.LENGTH_LONG).show();
        } else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("rows", rows);
            editor.putInt("cols", cols);
            editor.putString("difficulty", String.valueOf(difficulty));
            editor.putBoolean("longpress", longpress);
            editor.putBoolean("vibration", vibration);
            editor.putBoolean("showADRG", showADRG);
            editor.apply();
            startActivity(new Intent(Settings.this, MainActivity.class));
        }
    }
    //endregion

}
