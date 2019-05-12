package eparon.minesweeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import eparon.minesweeper.Game.Difficulty;

@SuppressLint("SetTextI18n")
public class Settings extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public String PREFS_OVH = "OVHPrefsFile";
    SharedPreferences prefs;

    int rows = 18, cols = 12;
    TextView Rows, Cols;

    double difficulty = Difficulty.HARD;
    double[] difArr = new double[]{Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD, Difficulty.EXTREME};
    Spinner difSpinner;

    @Override
    public void onBackPressed() {
        if (Rows.getText().toString().equals("")) Rows.setText(String.valueOf(rows));
        if (Cols.getText().toString().equals("")) Cols.setText(String.valueOf(cols));

        rows = Integer.valueOf(Rows.getText().toString());
        cols = Integer.valueOf(Cols.getText().toString());

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
                editor.apply();
                startActivity(new Intent(Settings.this, MainActivity.class));
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_OVH, Context.MODE_PRIVATE);

        rows = prefs.getInt("rows", rows);
        cols = prefs.getInt("cols", cols);
        difficulty = Double.parseDouble(prefs.getString("difficulty", String.valueOf(difficulty)));

        Rows = findViewById(R.id.rText);
        Cols = findViewById(R.id.cText);

        difSpinner = findViewById(R.id.dif_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.difficulty_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difSpinner.setAdapter(adapter);
        difSpinner.setOnItemSelectedListener(this);
        difSpinner.setSelection((int)(6 - difficulty));

        TextView version = findViewById(R.id.ver);
        version.setText(String.format("Version %s", BuildConfig.VERSION_NAME));
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        difficulty = difArr[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }

}
