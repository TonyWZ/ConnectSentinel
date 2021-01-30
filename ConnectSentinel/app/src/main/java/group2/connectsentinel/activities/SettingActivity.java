package group2.connectsentinel.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.preference.PreferenceManager;

import com.twilio.rest.chat.v1.service.User;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import group2.connectsentinel.R;
import group2.connectsentinel.background.GetUserProfileTask;
import group2.connectsentinel.background.PostDataEncoder;
import group2.connectsentinel.background.ProcessSettingTask;
import group2.connectsentinel.background.RequestOneUserTask;
import group2.connectsentinel.background.SendAlarmTask;
import group2.connectsentinel.background.StringSHA;
import group2.connectsentinel.data.UserProfile;
import group2.connectsentinel.datamanagement.FakeUserData;
import group2.connectsentinel.datamanagement.UserDataSource;

import static java.lang.Boolean.FALSE;

public class SettingActivity extends AppCompatActivity {
    //UserDataSource fake = FakeUserData.getInstance();
    private ArrayList<UserProfile> listUP;
    private long userId;
    private long defaultEContact;
    private String userName;
    private String password;
    private boolean medicalAbility;
    private boolean crimeAbility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE);
        userId = prefs.getLong(MainActivity.PHONE, -1);
        defaultEContact = prefs.getLong("defaultEContact", -1);
        Log.v("defaultEContact", "setting default E Contact: " + Long.toString(defaultEContact));
        if (defaultEContact == -1) {
            Log.v("econtact", "econtact is empty!");
        }
        userName = prefs.getString(MainActivity.USER_NAME, "");
        password = prefs.getString(MainActivity.PASSWORD, "");
        medicalAbility = prefs.getBoolean(MainActivity.MEDICAL_ABILITY, false);
        crimeAbility = prefs.getBoolean(MainActivity.CRIME_ABILITY, false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //find current profile
        final long entryId = prefs.getLong(MainActivity.PHONE, -1);

        //set texts
        String name =  userName;
        Boolean mTF = medicalAbility;
        Boolean cTF = crimeAbility;
        EditText namedisp = (EditText) findViewById(R.id.NameEdit);
        namedisp.setText(name);
        final EditText emergencydisp = (EditText) findViewById(R.id.EmergencyEdit);
        emergencydisp.setText(String.valueOf(defaultEContact));

        EditText passworddisp = (EditText) findViewById(R.id.PasswordEdit);
        passworddisp.setHint("Enter new password here.");

        if (mTF) {
            CheckBox medicaldisp = (CheckBox) findViewById(R.id.MedicalCheckBox);
            medicaldisp.setChecked(true);
        }

        if (cTF) {
            CheckBox crimedisp = (CheckBox) findViewById(R.id.CrimeCheckBox);
            crimedisp.setChecked(true);
        }

        //button activity
        Button submitSetting = (Button)findViewById(R.id.settingSubmit);
        submitSetting.setOnClickListener( new View.OnClickListener() {
            public void onClick(View view) {
                // get inputs
                EditText nameInput = findViewById(R.id.NameEdit);
                String name = nameInput.getText().toString();
                EditText emergencyContactInput = findViewById(R.id.EmergencyEdit);
                long emergencyContact;
                try {
                    emergencyContact = Long.parseLong(emergencyContactInput.getText().toString());
                    Log.v("emegencyContact", Long.toString(emergencyContact));
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Incorrect emergency contact phone" +
                                    " number entered",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                EditText passwordInput = findViewById(R.id.PasswordEdit);
                String password = StringSHA.hashString(passwordInput.getText().toString());
                CheckBox medical = findViewById(R.id.MedicalCheckBox);
                CheckBox crime = findViewById(R.id.CrimeCheckBox);

                // check inputs
                boolean emergencyContactisValid = false;
                boolean emergencyContactisUser = false;


                UserProfile userProfile = null;

                String[] findKey = new String[]{"Id"};
                String[] findValue = new String[]{Long.toString(emergencyContact)};
                String findData = PostDataEncoder.encodePostData(findKey, findValue);
                RequestOneUserTask requestUser = new RequestOneUserTask(getApplicationContext());
                try {
                    requestUser.execute(findData).get();

                } catch (ExecutionException e) {
                } catch (InterruptedException e) {
                }
                userProfile = requestUser.getReturnedOneUser();

                if (userProfile != null) {
                    emergencyContactisValid = true;
                    if (userProfile.getId() == userId) {
                        emergencyContactisValid = false;
                        emergencyContactisUser = true;
                    }
                }


                if (!emergencyContactisValid) {
                    if (emergencyContactisUser) {
                        Toast.makeText(getApplicationContext(), "You have entered your own phone number! Please re-enter and submit again!",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "The emergency contact does not " +
                                        "exist! Please re-enter and submit again!",
                                Toast.LENGTH_LONG).show();
                    }

                } else {
                    String[] keys = new String[]{"settingId", "settingName", "settingPassword",
                            "settingEmergencyContactID", "settingMedicalAbility",
                            "settingCrimeAbility"};
                    String[] values = new String[]{Long.toString(entryId), name, password,
                            Long.toString(emergencyContact),
                            medical.isChecked() ? "true" : "false", crime.isChecked() ? "true" :
                            "false"};
                    String data = PostDataEncoder.encodePostData(keys, values);
                    String url = MainActivity.SERVER_ROOT + "processSetting/";
                    new ProcessSettingTask(getApplicationContext()).execute(data);
                    Log.v("processSetting", "setting processed");
                    SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(MainActivity.PASSWORD, password);
                    editor.putString(MainActivity.USER_NAME, name);
                    editor.putBoolean(MainActivity.MEDICAL_ABILITY, medical.isChecked());
                    editor.putBoolean(MainActivity.CRIME_ABILITY, crime.isChecked());
                    editor.putLong(MainActivity.DEFAULT_E_Contact, emergencyContact);
                    editor.commit();

                    userProfile.setName(name);
                    userProfile.setPassword(password);
                    userProfile.setAbilityCrime(crime.isChecked());
                    userProfile.setAbilityMedical(medical.isChecked());
                    userProfile.setEmergencyContactId(emergencyContact);

                    Toast.makeText(getApplicationContext(), "Changes saved!",
                            Toast.LENGTH_LONG).show();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("emergencyContact", emergencyContact);
                    resultIntent.putExtra("username", name);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        Button back = (Button)findViewById(R.id.backToDash);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
                }
        });
    }

    /*
    1. Collect the ID which the user enters.
    2. Search the database (though the "UserDataSource.java" Interface) to see if present
    3.1 present ->  show (need another view)
    3.2 not present -> display warning message
     */
    public void profileQuery(android.view.View view) {
        EditText entryIdtext = (EditText) findViewById(R.id.idBox);
        if (entryIdtext == null) {
            Toast.makeText(
                    this,
                    "Please Enter Something!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        String temp = entryIdtext.getText().toString();
        if (temp == null || temp.length() == 0) {
            Toast.makeText(
                    this,
                    "Please Enter Something!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        long entryId = -1l;
        try{
            entryId = Long.parseLong(temp);
        } catch(NumberFormatException e) {
            Toast.makeText(getApplicationContext(), "Incorrect phone number entered",
                    Toast.LENGTH_LONG).show();
            return;
        }


        //UserDataSource fake = FakeUserData.getInstance();
        //UserProfile pf = fake.requestUserProfile(entryId);
        UserProfile pf = null;
        String[] findKey = new String[]{"Id"};
        String[] findValue = new String[]{Long.toString(entryId)};
        String findData = PostDataEncoder.encodePostData(findKey, findValue);
        RequestOneUserTask requestUser = new RequestOneUserTask(getApplicationContext());
        try {
            requestUser.execute(findData).get();

        } catch (ExecutionException e) {
        } catch (InterruptedException e) {
        }
        pf = requestUser.getReturnedOneUser();

        if (pf == null) {
            Toast.makeText(
                    this,
                    "User doesn\'t exist",
                    Toast.LENGTH_LONG)
                    .show();
        } else {
            Intent i = new Intent(this, ProfileDisplayActivity.class);
            i.putExtra("ID",pf.getId());
            i.putExtra("NAME",pf.getName());
            i.putExtra("MedicalTF", pf.getAbilityMedical());
            i.putExtra("CrimeTF",pf.getAbilityCrime());
            startActivityForResult(i, 1);
        }
    }
}
