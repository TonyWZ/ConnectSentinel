package group2.connectsentinel.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import group2.connectsentinel.R;
import group2.connectsentinel.background.AddAbuseReportTask;
import group2.connectsentinel.background.GetBanUserTask;
import group2.connectsentinel.background.GetUserProfileTask;
import group2.connectsentinel.background.PostDataEncoder;
import group2.connectsentinel.background.PostQueryTask;
import group2.connectsentinel.background.RequestOneUserTask;
import group2.connectsentinel.background.SendAlarmTask;
import group2.connectsentinel.background.StringSHA;
import group2.connectsentinel.background.checkBanUserTask;
import group2.connectsentinel.data.BanUser;
import group2.connectsentinel.data.UserProfile;

public class MainActivity extends AppCompatActivity {

    public static final String SHARED_PREFERENCE_FILE_NAME = "LogInInfo";
    public static final String PHONE = "phoneKey";
    public static final String PASSWORD = "passwordKey";
    public static final String USER_NAME = "userName";
    public static final String MEDICAL_ABILITY = "medicalAbility";
    public static final String CRIME_ABILITY = "crimeAbility";
    public static final String CURRENT_E_CONTACT = "curentEContact";
    public static final String DEFAULT_E_Contact = "defaultEContact";
    public static final String IS_ON_TRIP = "isOnTrip";
    public static final String CURRENT_DEST = "currDest";
    public static final String SEND_MEDICAL_ALARM_ACTION = "group2.connectsentinel.actions.SendMedicalAlarm";
    public static final String SEND_CRIMINAL_ALARM_ACTION = "group2.connectsentinel.actions.SendCriminalAlarm";
    public static final String SERVER_ROOT = "http://10.0.2.2:3000/";
    public static double currLatitude;
    public static double currLongitude;

    private SharedPreferences sharedPreferences;
    private long currEContact;
    private String userName;

    //public static final List<UserProfile> listUP = new LinkedList<>();

            @Override
            protected void onCreate (Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                /*
                // User Profile from the Database
                try {
                    URL url = new URL("http://10.0.2.2:3000/returnUsers");
                    listUP = new ArrayList<UserProfile>();
                    GetUserProfileTask task = new GetUserProfileTask(listUP);
                    task.execute(url).get();
                    Log.v("testSetting","List length when setting tarts: " + Integer.toString(listUP.size()));
                } catch (Exception e) {
                }
                */

                sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
                long id = sharedPreferences.getLong(PHONE, -1l);
                if (id < 0 && (SEND_MEDICAL_ALARM_ACTION.equals(getIntent().getAction()) || SEND_CRIMINAL_ALARM_ACTION.equals(getIntent().getAction()))) {
                    Toast.makeText(getApplicationContext(), "Cannot send alarm when not logged in",
                            Toast.LENGTH_LONG).show();
                }
                setContentView(R.layout.activity_main);
                Button logIn = (Button) findViewById(R.id.logIn_button);
                logIn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        // BanUsers related code:
                        /*
                        try {
                            URL url = new URL("http://10.0.2.2:3000/returnBanUsers");
                            listBU = new ArrayList<BanUser>();
                            GetBanUserTask task = new GetBanUserTask(listBU);
                            task.execute(url).get();
                        } catch (Exception e) {
                        }
                        */

                        EditText et1 = (EditText) findViewById(R.id.phoneNumberInput);
                        long phoneNumber = -1;
                        try {
                            phoneNumber = Long.parseLong(et1.getText().toString());
                        } catch (NumberFormatException e) {
                            Toast.makeText(getApplicationContext(), "Incorrect phone number entered",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        EditText et2 = (EditText) findViewById(R.id.passwordInput);
                        String password = StringSHA.hashString(et2.getText().toString());
                        Log.v("Hash", "Unhashed pass word is " + et2.getText().toString());
                        Log.v("Hash", "Hashed pass word is " + password);

                        boolean flag = true;

                        // BanUser check first (By Sylvie

                        String[] keys = new String[]{"enterId", "enterPassword"};
                        String[] values = new String[]{Long.toString(phoneNumber), password};
                        String data = PostDataEncoder.encodePostData(keys, values);
                        checkBanUserTask banUserTask = new checkBanUserTask(getApplicationContext());

                        try {
                            banUserTask.execute(data).get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        flag = banUserTask.getState();
                        if (!flag) {
                            Toast.makeText(getApplicationContext(), banUserTask.getMes(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        } else {


                            //UserDataSource uds = FakeUserData.getInstance();
                            //UserProfile userProfile = uds.requestUserProfile(phoneNumber);
                            //boolean passwordCheck = uds.checkPassword(phoneNumber, password);

                            UserProfile userProfile = null;

                            String[] findKey = new String[]{"Id"};
                            String[] findValue = new String[]{Long.toString(phoneNumber)};
                            String findData = PostDataEncoder.encodePostData(findKey, findValue);
                            RequestOneUserTask requestUser = new RequestOneUserTask(getApplicationContext());
                            try {
                                requestUser.execute(findData).get();

                            } catch (ExecutionException e) {
                            } catch (InterruptedException e) {
                            }

                            Log.v("returnOneUser", "Calling get");
                            userProfile = requestUser.getReturnedOneUser();

                            if (userProfile == null) {
                                Toast.makeText(getApplicationContext(), "Can't find this user!",
                                        Toast.LENGTH_LONG).show();
                            } else if (!password.equals(userProfile.getPassword())) {
                                Toast.makeText(getApplicationContext(), "Wrong password!",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putLong(PHONE, phoneNumber);
                                editor.putString(PASSWORD, password);
                                editor.putLong(CURRENT_E_CONTACT, userProfile.getEmergencyContactId());
                                editor.putString(USER_NAME, userProfile.getName());
                                editor.putBoolean(MEDICAL_ABILITY, userProfile.getAbilityMedical());
                                editor.putBoolean(CRIME_ABILITY, userProfile.getAbilityCrime());
                                editor.putLong(DEFAULT_E_Contact, userProfile.getEmergencyContactId());
                                editor.commit();
                                Intent startIntent = new Intent(getApplicationContext(), DashboardDrawerActivity.class);
                                startIntent.putExtra("userId", phoneNumber);
                                startIntent.putExtra("isOnTrip", false);
                                startIntent.putExtra("currEContact", userProfile.getEmergencyContactId());
                                startIntent.putExtra("defaultEContact", userProfile.getEmergencyContactId());
                                startIntent.putExtra("userName", userProfile.getName());
                                startActivity(startIntent);
                                et1.getText().clear();
                                et2.getText().clear();
                            }

                        }
                    }
                });

                Button register = (Button) findViewById(R.id.register_button);
                register.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent startIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                        startActivity(startIntent);
                    }
                });

                if (id != -1l) {
                    UserProfile userProfile = null;

                    String[] findKey = new String[]{"Id"};
                    String[] findValue = new String[]{Long.toString(id)};
                    String findData = PostDataEncoder.encodePostData(findKey, findValue);
                    RequestOneUserTask requestUser = new RequestOneUserTask(getApplicationContext());
                    try {
                        requestUser.execute(findData).get();
                        userProfile = requestUser.getReturnedOneUser();
                    } catch (ExecutionException e) {
                    } catch (InterruptedException e) {
                    }
                        currEContact = sharedPreferences.getLong(CURRENT_E_CONTACT, userProfile.getEmergencyContactId());
                        boolean sendMedicalAlarm = SEND_MEDICAL_ALARM_ACTION.equals(getIntent().getAction());
                        boolean sendCriminalAlarm = SEND_CRIMINAL_ALARM_ACTION.equals(getIntent().getAction());
                        if (sendMedicalAlarm || sendCriminalAlarm) {
                            Log.v("SMS", "CreatedActivity");
                            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
                            userName = sharedPreferences.getString(USER_NAME, null);
                            Log.v("SMS", "Creating SMS Sender");
                            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS)
                                    != PackageManager.PERMISSION_GRANTED) {
                                String[] requiredPermissions = {Manifest.permission.SEND_SMS};
                                ActivityCompat.requestPermissions(this, requiredPermissions, 1);
                            } else {
                                SmsManager smsManager = SmsManager.getDefault();
                                String content;
                                String alarmCode = "0";
                                if (sendMedicalAlarm) {
                                    content = "[Alarm] " + userName + " is currently under medical distress. Current location: latitude "
                                            + currLatitude + " longitude " + currLongitude + ".";
                                } else {
                                    content = "[Alarm] " + userName + " is currently under criminal distress. Current location: latitude "
                                            + currLatitude + " longitude " + currLongitude + ".";
                                    alarmCode = "1";
                                }
                                smsManager.sendTextMessage(String.valueOf(currEContact), null, content, null, null);
                                String[] keys = new String[]{"latitude", "longitude", "alarmCode", "originID"};
                                String[] values = new String[]{Double.toString(MainActivity.currLatitude),
                                        Double.toString(MainActivity.currLongitude), alarmCode, Long.toString(id)};
                                String data = PostDataEncoder.encodePostData(keys, values);
                                String url = MainActivity.SERVER_ROOT + "propagateAlarm/";
                                new SendAlarmTask(getApplicationContext()).execute(data);
                                Log.v("DistressAlarm", "alarm Sent");
                            }
                        }
                        Intent startIntent = new Intent(getApplicationContext(), DashboardDrawerActivity.class);
                        boolean isOnTrip = sharedPreferences.getBoolean(IS_ON_TRIP, false);
                        long defaultEContact = userProfile.getEmergencyContactId();
                        String dest = sharedPreferences.getString(CURRENT_DEST, null);
                        String name = sharedPreferences.getString(USER_NAME, null);
                        startIntent.putExtra("userId", id);
                        startIntent.putExtra("isOnTrip", isOnTrip);
                        startIntent.putExtra("currEContact", currEContact);
                        startIntent.putExtra("currDest", dest);
                        startIntent.putExtra("defaultEContact", defaultEContact);
                        startIntent.putExtra("userName", name);
                        startActivity(startIntent);
                }
            }

                public void onRequestPermissionResult (int requestCode, String[] permissions, int[] grantResults){
                    Log.v("SMS", "on request permission result called");
                    if (requestCode == 1 && grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.v("SMS", "Permission granted");
                        boolean sendMedicalAlarm = SEND_MEDICAL_ALARM_ACTION.equals(getIntent().getAction());
                        boolean sendCriminalAlarm = SEND_CRIMINAL_ALARM_ACTION.equals(getIntent().getAction());
                        if (sendMedicalAlarm || sendCriminalAlarm) {
                            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
                            userName = sharedPreferences.getString(USER_NAME, null);
                            SmsManager smsManager = SmsManager.getDefault();
                            String content;
                            String alarmCode = "0";
                            if (sendMedicalAlarm) {
                                content = "[Alarm] " + userName + " is currently under medical distress. Current location: latitude "
                                        + currLatitude + " longitude " + currLongitude + ".";
                            } else {
                                content = "[Alarm] " + userName + " is currently under criminal distress. Current location: latitude "
                                        + currLatitude + " longitude " + currLongitude + ".";
                                alarmCode = "1";
                            }
                            smsManager.sendTextMessage(String.valueOf(currEContact), null, content, null, null);
                            String[] keys = new String[]{"latitude", "longitude", "alarmCode", "originID"};
                            String[] values = new String[]{Double.toString(MainActivity.currLatitude),
                                    Double.toString(MainActivity.currLongitude), alarmCode, Long.toString(sharedPreferences.getLong(PHONE, -1))};
                            String data = PostDataEncoder.encodePostData(keys, values);
                            String url = MainActivity.SERVER_ROOT + "propagateAlarm/";
                            new SendAlarmTask(getApplicationContext()).execute(data);
                            Log.v("DistressAlarm", "alarm Sent");
                        }
                    }
                }
            }
