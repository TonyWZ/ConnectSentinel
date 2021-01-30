package group2.connectsentinel.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;

import group2.connectsentinel.R;
import group2.connectsentinel.background.AlarmReceiver;

public class DashboardActivity extends AppCompatActivity {

    private long userId;
    private String destination;
    private long emergencyContact;
    private long defaultEContact;
    private boolean isOnTrip;

    // Report Abuse field.
    // How many reports that the very user sent, but didn't receive any response
    // Number of pending reports
    private int reportNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent inputIntent = getIntent();
        userId = inputIntent.getLongExtra("userId", -1l);
        isOnTrip = inputIntent.getBooleanExtra("isOnTrip", false);
        emergencyContact = inputIntent.getLongExtra("currEContact", -1l);
        destination = inputIntent.getStringExtra("currDest");
        defaultEContact = inputIntent.getLongExtra("defaultEContact", -1l);

        reportNumber = inputIntent.getIntExtra("reportNumber", 0);
        setContentView(R.layout.activity_dashboard);

        Button logOut = (Button) findViewById(R.id.logOut_button);
        logOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();
                setResult(RESULT_OK);
                finish();
            }
        });

        Button newTrip = (Button) findViewById(R.id.newTrip_button);
        newTrip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(isOnTrip) {
                    Toast.makeText(getApplicationContext(), "You are already on a trip",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Intent i = new Intent(getApplicationContext(), NewTripActivity.class);
                i.putExtra("id", userId);
                startActivityForResult(i, 1);
            }
        });

        Button currentTrip = (Button) findViewById(R.id.currentTrip_button);
        currentTrip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!isOnTrip) {
                    Toast.makeText(getApplicationContext(), "You are currently not on a trip",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Intent startIntent = new Intent(getApplicationContext(), CurrentTripActivity.class);
                startIntent.putExtra("destination", destination);
                startIntent.putExtra("emergencyContact", emergencyContact);
                startActivityForResult(startIntent, 3);
            }
        });

        Button setting = (Button) findViewById(R.id.setting_button);
        setting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(startIntent);
            }
        });

        // Missing Person field.
        Button reportMissing = (Button) findViewById(R.id.report_missing_button);
        reportMissing.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(),
                        ReportMissingPersonActivity.class);
                startActivity(startIntent);
            }
        });

        Button displayMissing = (Button) findViewById(R.id.displayMissingPerson);
        displayMissing.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(),
                        DisplayMissingPersonActivity.class);
                startActivity(startIntent);
            }
        });

        boolean smsPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        boolean locationPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if(locationPermission) {
            setLocationAlarm();
        }

        String[] permissionReq = new String[2];
        permissionReq[0] = Manifest.permission.ACCESS_FINE_LOCATION;
        permissionReq[1] = Manifest.permission.SEND_SMS;

        Log.v("PermissionReq", permissionReq[0]);
        Log.v("PermissionReq", permissionReq[1]);
        if(!smsPermission && !locationPermission) {
            ActivityCompat.requestPermissions(this, permissionReq, 1);
        } else if(!smsPermission && locationPermission) {
            ActivityCompat.requestPermissions(this, Arrays.copyOfRange(permissionReq, 1, 2), 2);
        } else if(!locationPermission && smsPermission) {
            ActivityCompat.requestPermissions(this, Arrays.copyOfRange(permissionReq, 0, 1), 3);
        }
    }

    public void reportAbuse(View view) {
        Intent i = new Intent(this, ReportAbuseActivity.class);
        i.putExtra("userId", userId);
        Log.v("Id Sent by Dashboard", Long.toString(userId));
        startActivityForResult(i, 2);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            destination = intent.getStringExtra("destination");
            emergencyContact = intent.getLongExtra("emergencyContact", -1l);
            isOnTrip = true;
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            //Sylvie: add corresponding functionality for reportAbuse
            //This shouldn't be the finalized version since the administrator app is not developed.
            //TODO: Probably need to change after admin app.
            reportNumber++;
        } else if (requestCode == 3 && resultCode == RESULT_OK) {
            isOnTrip = false;
            emergencyContact = defaultEContact;
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults){
        if((requestCode == 1 || requestCode == 3) && grantResults.length >= 1) {
            for(int i = 0; i < permissions.length; i++) {
                if(permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    setLocationAlarm();
                }
            }
        }
    }

    private void setLocationAlarm(){
        Context appContext = getApplicationContext();
        AlarmManager alarmManager=(AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(appContext, AlarmReceiver.class);
        intent.putExtra("id", userId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 1, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),30000,
                pendingIntent);
    }

}
