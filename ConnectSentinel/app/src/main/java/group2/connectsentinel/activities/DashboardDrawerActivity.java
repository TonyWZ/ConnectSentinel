package group2.connectsentinel.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import group2.connectsentinel.R;
import group2.connectsentinel.background.AlarmReceiver;
import group2.connectsentinel.background.GetUserProfileTask;
import group2.connectsentinel.background.PostDataEncoder;
import group2.connectsentinel.background.SendAlarmTask;
import group2.connectsentinel.data.UserProfile;
import group2.connectsentinel.datamanagement.FakeUserData;
import group2.connectsentinel.datamanagement.UserDataSource;


public class DashboardDrawerActivity extends AppCompatActivity

    implements NavigationView.OnNavigationItemSelectedListener {

    private long userId;
    private String destination;
    private long emergencyContact;
    private long defaultEContact;
    private boolean isOnTrip;
    private String userName;
    private TextView headerTrip;
    private ArrayList<UserProfile> listUP;
    private PendingIntent alarmIntent;
    private BroadcastReceiver br;

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
        Log.v("emergencycontact", "the current is " + Long.toString(emergencyContact));
        destination = inputIntent.getStringExtra("currDest");
        defaultEContact = inputIntent.getLongExtra("defaultEContact", -1l);
        userName = inputIntent.getStringExtra("userName");
        if (userName == null) {
            SharedPreferences pref = getSharedPreferences(MainActivity.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
            userName = pref.getString("userName", "UNKOWN");
        }
        // User Profile from the Database
        try {
            URL url = new URL("http://10.0.2.2:3000/returnUsers");
            listUP = new ArrayList<UserProfile>();
            GetUserProfileTask task = new GetUserProfileTask(listUP);
            task.execute(url);
        }
        catch (Exception e) {
        }

        reportNumber = inputIntent.getIntExtra("reportNumber", 0);
        setContentView(R.layout.activity_dashboard);
        setContentView(R.layout.activity_dashboard_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ConnectSentinel");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        boolean smsPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        boolean locationPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        Log.v("LocationLog", "got permission " + locationPermission);
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
        } else if(!smsPermission) {
            ActivityCompat.requestPermissions(this, Arrays.copyOfRange(permissionReq, 1, 2), 2);
        } else if(!locationPermission) {
            ActivityCompat.requestPermissions(this, Arrays.copyOfRange(permissionReq, 0, 1), 3);
        }

        Button medicalAlarm = (Button) findViewById(R.id.medicalAlarm);
        medicalAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    String[] requiredPermissions = {Manifest.permission.SEND_SMS};
                    ActivityCompat.requestPermissions(DashboardDrawerActivity.this, requiredPermissions, 4);
                } else {
                    SmsManager smsManager = SmsManager.getDefault();
                    String content = "[Alarm] " + userName + " is currently under medical distress. Current location: latitude "
                                + MainActivity.currLatitude + " longitude " + MainActivity.currLongitude + ".";
                    smsManager.sendTextMessage(String.valueOf(emergencyContact),null, content, null, null);
                }
                String[] keys = new String[]{"latitude", "longitude", "alarmCode", "originID"};
                String[] values = new String[]{Double.toString(MainActivity.currLatitude),
                        Double.toString(MainActivity.currLongitude), "0", Long.toString(userId)};
                String data = PostDataEncoder.encodePostData(keys, values);
                String url = MainActivity.SERVER_ROOT + "propagateAlarm/";
                new SendAlarmTask(getApplicationContext()).execute(data);
                Log.v("DistressAlarm", "alarm Sent");
            }
        });

        Button criminalAlarm = (Button) findViewById(R.id.crimeAlarm);
        criminalAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    String[] requiredPermissions = {Manifest.permission.SEND_SMS};
                    ActivityCompat.requestPermissions(DashboardDrawerActivity.this, requiredPermissions, 5);
                } else {
                    SmsManager smsManager = SmsManager.getDefault();
                    String content = "[Alarm] " + userName + " is currently under crime distress. Current position: latitude "
                            + MainActivity.currLatitude + " longitude " + MainActivity.currLongitude + ".";
                    smsManager.sendTextMessage(String.valueOf(emergencyContact),null, content, null, null);
                }
                Toast.makeText(getApplicationContext(), "Crime Alarm Sent!",
                        Toast.LENGTH_LONG).show();
                String[] keys = new String[]{"latitude", "longitude", "alarmCode", "originID"};
                String[] values = new String[]{Double.toString(MainActivity.currLatitude),
                        Double.toString(MainActivity.currLongitude), "1", Long.toString(userId)};
                String data = PostDataEncoder.encodePostData(keys, values);
                new SendAlarmTask(getApplicationContext()).execute(data);
                Log.v("DistressAlarm", "alarm Sent");
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard_test, menu);
        //change header
        //UserDataSource uds = FakeUserData.getInstance();
        //UserProfile userProfile = uds.requestUserProfile(userId);
        UserProfile userProfile = null;
        for (UserProfile user :listUP) {
            long userId = user.getId();
            if (userId == (long)userId) {
                userProfile = user;
            }
        }

        TextView headerName = (TextView) findViewById(R.id.nav_header_name);
        Log.v("username", "hello" + userName);
        headerName.setText("Hello, " + userName);
        headerTrip = (TextView) findViewById(R.id.nav_header_trip);
        updateECTextView();

        if (isOnTrip) {
            headerTrip.setText("You are currently on a trip.");
        } else {
            headerTrip.setText("You are currently not on a trip");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //new items may be added here
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_reportmissing) {
            Intent startIntent = new Intent(getApplicationContext(),
                    ReportMissingPersonActivity.class);
            SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFERENCE_FILE_NAME,
                    Context.MODE_PRIVATE);
            startIntent.putExtra("contact", prefs.getLong(MainActivity.PHONE, -1));
            startActivity(startIntent);
        } else if (id == R.id.nav_reportabuse) {
            Intent i = new Intent(this, ReportAbuseActivity.class);
            i.putExtra("userId", userId);
            startActivity(i);

        } else if (id == R.id.nav_createnew) {
            if(isOnTrip) {
                Toast.makeText(getApplicationContext(), "You are already on a trip",
                        Toast.LENGTH_LONG).show();
                return false;
            }
            Intent i = new Intent(getApplicationContext(), NewTripActivity.class);
            i.putExtra("id", userId);
            startActivityForResult(i, 1);
        } else if (id == R.id.nav_mycurrent) {
            if(!isOnTrip) {
                Toast.makeText(getApplicationContext(), "You are currently not on a trip",
                        Toast.LENGTH_LONG).show();
                return false;
            }
            Intent startIntent = new Intent(getApplicationContext(), CurrentTripActivity.class);
            startIntent.putExtra("destination", destination);
            startIntent.putExtra("emergencyContact", emergencyContact);
            startActivityForResult(startIntent, 3);

        } else if (id == R.id.nav_settings) {
            Intent startIntent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivityForResult(startIntent, 4);

        } else if (id == R.id.nav_logout) {
            SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.commit();
            AlarmManager alarmManager=(AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(alarmIntent);
            Log.v("LocationLog", "alarm canceled");
            alarmIntent.cancel();
            unregisterReceiver(br);
            setResult(RESULT_OK);
            finish();
        } else if (id == R.id.nav_display_missing) {
            Intent startIntent = new Intent(getApplicationContext(),
                    DisplayMissingPersonActivity.class);
            startActivity(startIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            destination = intent.getStringExtra("destination");
            emergencyContact = intent.getLongExtra("emergencyContact", -1l);
            isOnTrip = true;
            headerTrip.setText("You are currently on a trip.");
            updateECTextView();
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            //Sylvie: add corresponding functionality for reportAbuse
            //This shouldn't be the finalized version since the administrator app is not developed.
            //TODO: Probably need to change after admin app.
            reportNumber++;
        } else if (requestCode == 3 && resultCode == RESULT_OK) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(String.valueOf(emergencyContact),null, userName + " has completed the trip safely.", null, null);
            isOnTrip = false;
            headerTrip.setText("You are currently not on a trip.");
            emergencyContact = defaultEContact;
            updateECTextView();
        } else if (requestCode == 4 && resultCode == RESULT_OK) {
            Log.v("BackButton","RESULT IS OK");
            userName = intent.getStringExtra("username");
            if (userName == null) {
                SharedPreferences pref = getSharedPreferences(MainActivity.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
                userName = pref.getString("userName", "UNKOWN");
            }
            TextView nameDisp = (TextView)findViewById(R.id.nav_header_name);
            nameDisp.setText(userName);

            long newDefaultEContact = intent.getLongExtra("emergencyContact", -1);

            defaultEContact = newDefaultEContact;

            if(!isOnTrip) {
                emergencyContact = defaultEContact;
                updateECTextView();
            }
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
        } else if(requestCode == 4 && grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("SMS","Permission granted");
            SmsManager smsManager = SmsManager.getDefault();
            String content = "[Alarm] " + userName + " is currently under medical distress. Current location: latitude "
                    + MainActivity.currLatitude + " longitude " + MainActivity.currLongitude + ".";
            smsManager.sendTextMessage(String.valueOf(emergencyContact), null, content, null, null);
        } else if(requestCode == 5 && grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("SMS","Permission granted");
            SmsManager smsManager = SmsManager.getDefault();
            String content = "[Alarm] " + userName + " is currently under crime distress. Current location: latitude "
                    + MainActivity.currLatitude + " longitude " + MainActivity.currLongitude + ".";
            smsManager.sendTextMessage(String.valueOf(emergencyContact), null, content, null, null);
        }
    }

    private void setLocationAlarm(){
        Context appContext = getApplicationContext();
        AlarmManager alarmManager=(AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(appContext, AlarmReceiver.class);
        Log.v("LocationLog", "Set intent id " + userId);
        intent.putExtra("id", userId);
        alarmIntent = PendingIntent.getBroadcast(appContext, 1, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),30000,
                alarmIntent);
        br = new AlarmReceiver();
        registerReceiver(br, new IntentFilter());
    }

    private void updateECTextView(){
        //UserDataSource uds = FakeUserData.getInstance();
        TextView headerEC = (TextView) findViewById(R.id.nav_header_currentEC);
        TextView nameDisp = (TextView)findViewById(R.id.nav_header_name);
        nameDisp.setText(userName);

        boolean emergencyContactisValid = false;
        String emergencyContactName = "";
        for (UserProfile user: listUP) {
            if(user.getId() == emergencyContact) {
                emergencyContactisValid = true;
                emergencyContactName = user.getName();
            }
        }
        if (emergencyContactisValid) {
            headerEC.setText("Your current emergency contact is: " + Long.toString(emergencyContact)
                    + "(" + emergencyContactName + ")");
        } else {
            headerEC.setText("Your current emergency contact is: " + Long.toString(emergencyContact));
        }
    }

}
