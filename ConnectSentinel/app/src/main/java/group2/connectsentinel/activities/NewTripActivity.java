package group2.connectsentinel.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import group2.connectsentinel.R;
import group2.connectsentinel.background.AlarmReceiver;
import group2.connectsentinel.data.Trip;
import group2.connectsentinel.datamanagement.FakeTripData;
import group2.connectsentinel.datamanagement.TripDataSource;

import java.util.Date;

public class NewTripActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);
        Button newTrip = (Button) findViewById(R.id.newTrip_button);
        Button back = (Button) findViewById(R.id.newTripBack);

        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        newTrip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText et1 = (EditText) findViewById(R.id.cityBox);
                String city = et1.getText().toString();
                EditText et3 = (EditText) findViewById(R.id.streetBox);
                String street = et3.getText().toString();
                String destination = street + "\n" + city;
                EditText et2 = (EditText) findViewById(R.id.emergencyContactBox);
                long emergencyContact = -1l;
                try{
                    Log.v("NumberFormat","Got number " + et2.getText().toString());
                    emergencyContact = Long.parseLong(et2.getText().toString());
                } catch(NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Incorrect phone number entered",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Intent startIntent = new Intent();
                startIntent.putExtra("destination", destination);
                startIntent.putExtra("emergencyContact", emergencyContact);
                Trip trip = new Trip(destination, emergencyContact, new Date());
                TripDataSource tripDB = FakeTripData.getInstance();
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
                long userID = sharedPreferences.getLong(MainActivity.PHONE, -1l);
                tripDB.addTrip(userID, trip);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(MainActivity.CURRENT_E_CONTACT, emergencyContact);
                editor.putString(MainActivity.CURRENT_DEST, destination);
                editor.putBoolean(MainActivity.IS_ON_TRIP, true);
                editor.commit();

                Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
                intent.setAction("group2.connectsentinel.SetTripStatus");
                intent.putExtra("isOnTrip",true);
                getApplicationContext().sendBroadcast(intent);

                setResult(RESULT_OK, startIntent);
                finish();
            }
        });
    }
}
