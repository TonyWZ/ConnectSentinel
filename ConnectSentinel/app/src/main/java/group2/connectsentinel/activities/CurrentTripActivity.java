package group2.connectsentinel.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import group2.connectsentinel.R;
import group2.connectsentinel.background.AlarmReceiver;

public class CurrentTripActivity extends AppCompatActivity {

    private String destination;
    private long emergencyContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_trip);
        Intent i = getIntent();
        destination = i.getStringExtra("destination");
        emergencyContact = i.getLongExtra("emergencyContact", -1l);
        TextView ec = (TextView) findViewById(R.id.EmergencyContact);
        ec.setText(String.valueOf(emergencyContact));
        TextView d = (TextView) findViewById(R.id.Destination);
        d.setText(destination);

        CheckBox checkIn = findViewById(R.id.arrivalCheckIn);
        checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CompoundButton) view).isChecked()){
                    Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
                    intent.setAction("group2.connectsentinel.SetTripStatus");
                    intent.putExtra("isOnTrip",false);
                    getApplicationContext().sendBroadcast(intent);

                    SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(MainActivity.CURRENT_DEST, null);
                    editor.putBoolean(MainActivity.IS_ON_TRIP, false);
                    editor.commit();

                    Intent startIntent = new Intent();
                    startIntent.putExtra("tripFinished", true);
                    setResult(RESULT_OK, startIntent);
                    finish();
                }
            }
        });
    }


}
