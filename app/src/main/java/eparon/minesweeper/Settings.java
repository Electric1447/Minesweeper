package eparon.minesweeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Display;
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

import java.util.Locale;

import eparon.minesweeper.Game.Difficulty;

@SuppressLint("SetTextI18n")
public class Settings extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public String PREFS_OVH = "OVHPrefsFile";
    SharedPreferences prefs;

    int rows = 18, cols = 12;
    TextView Rows, Cols;

    double difficulty = Difficulty.HARD;
    Spinner difSpinner;

    boolean longpress = true, vibration = true, showADRG = true, showADOOB = true;
    CheckBox longpressCB, vibrationCB, adrgCB;

    PopupWindow ad;

    @Override
    public void onBackPressed () { Save(); }


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_OVH, Context.MODE_PRIVATE);

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

        difSpinner = findViewById(R.id.dif_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.difficulty_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difSpinner.setAdapter(adapter);
        difSpinner.setOnItemSelectedListener(this);
        difSpinner.setSelection(Difficulty.valueToPosition(difficulty));

        longpressCB = findViewById(R.id.cbLongpress);
        longpressCB.setChecked(longpress);
        vibrationCB = findViewById(R.id.cbVibration);
        vibrationCB.setChecked(vibration);
        adrgCB = findViewById(R.id.cbADRG);
        adrgCB.setChecked(showADRG);

        TextView version = findViewById(R.id.ver);
        version.setText(String.format("Version %s\nCreated by Itai Levin.", BuildConfig.VERSION_NAME));
    }

    @Override
    public void onItemSelected (AdapterView<?> adapterView, View view, int position, long l) {
        difficulty = Difficulty.positionToValue(position);
    }

    @Override
    public void onNothingSelected (AdapterView<?> adapterView) { }

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

    public void goBack (View view) { Save(); }

    private void Save () {
        if (Rows.getText().toString().equals("")) Rows.setText(String.valueOf(rows));
        if (Cols.getText().toString().equals("")) Cols.setText(String.valueOf(cols));

        rows = Integer.valueOf(Rows.getText().toString());
        cols = Integer.valueOf(Cols.getText().toString());

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int abh = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            abh = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());

        if (showADOOB && (size.x / cols) > ((size.y - abh) / rows)) {
            if (ad != null) ad.dismiss();

            // Creating the custom AlertDialog
            View customView = View.inflate(this, R.layout.custom_alertdialog, null);

            ad = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ad.setElevation(5.0f);
            ad.showAtLocation(findViewById(R.id.cl), Gravity.CENTER,0,0);

            TextView Title = customView.findViewById(R.id.title);
            TextView Message = customView.findViewById(R.id.message);
            TextView Positive = customView.findViewById(R.id.positive);
            TextView Negative = customView.findViewById(R.id.negative);
            final CheckBox CheckBox = customView.findViewById(R.id.cb);

            Title.setText(R.string.ad_oob_title);
            Message.setText(R.string.ad_oob_message);
            Positive.setText(R.string.ad_oob_positive);
            Negative.setText(R.string.ad_oob_negative);

            Positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showADOOB = !CheckBox.isChecked();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("showADOOB", showADOOB);
                    editor.apply();
                    ad.dismiss();
                    Save2();
                }
            });

            Negative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showADOOB = !CheckBox.isChecked();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("showADOOB", showADOOB);
                    editor.apply();
                    ad.dismiss();
                }
            });
        } else {
            Save2();
        }
    }

    private void Save2 () {
        if (rows > 24 || rows < 9)
            Toast.makeText(this, "Rows number should be between 9 to 24", Toast.LENGTH_LONG).show();
        else {
            if (cols > 16 || cols < 6)
                Toast.makeText(this, "Cols number should be between 6 to 16", Toast.LENGTH_LONG).show();
            else {
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
    }

}
