package jp.co.abs.tetrisver2_1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{

     private ScoreDB scoredb;
     private TextView highScoreView;
     Switch BGMSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
//DB作成
        scoredb = new ScoreDB(getApplicationContext());

        highScoreView = findViewById(R.id.highScore);

        SQLiteDatabase db = scoredb.getReadableDatabase();

        String order_by = "score DESC";

        Cursor cursor = db.query(
                "scoreDB",
                new String[]{"score", "line"},
                null,
                null,
                null,
                null,
                order_by
                );

            boolean mov = cursor.moveToFirst();

            StringBuilder sbuilder = new StringBuilder();

            if(mov == true) {
                for (int i = 0; i < 3; i++) {
                    sbuilder.append("Score: ");
                    sbuilder.append(cursor.getInt(0));
                    sbuilder.append("\t" + "Line: ");
                    sbuilder.append(cursor.getInt(1) + "\n");
                    cursor.moveToNext();
                }
            }

            cursor.close();

            highScoreView.setText(sbuilder.toString());
            BGMSwitch = findViewById(R.id.BGMSwitch);

            BGMSwitch.setOnCheckedChangeListener(this);
    }

    public void onClick(View view){
        Intent intent = new Intent(this,TetrisActivity.class);
        startActivity(intent);
        finish();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){

      //  SharedPreferences Count = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences Count = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
        SharedPreferences.Editor Editor = Count.edit();

        Editor.putBoolean("Switch", isChecked);
       /* if(isChecked == true) {
            Editor.putBoolean("Switch", true);
        }else{
            Editor.putBoolean("Switch", false);
        }
        */
        Editor.apply();
        Editor.commit();

    }
}