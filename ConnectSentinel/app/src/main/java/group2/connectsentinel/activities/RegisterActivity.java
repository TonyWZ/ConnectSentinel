package group2.connectsentinel.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import group2.connectsentinel.R;
import group2.connectsentinel.background.AddUserProfileTask;
import group2.connectsentinel.background.GetBanUserTask;
import group2.connectsentinel.background.GetUserProfileTask;
import group2.connectsentinel.background.PostDataEncoder;
import group2.connectsentinel.background.ProcessSettingTask;
import group2.connectsentinel.background.RequestOneUserTask;
import group2.connectsentinel.background.StringSHA;
import group2.connectsentinel.background.checkBanUserTask;
import group2.connectsentinel.data.BanUser;
import group2.connectsentinel.data.UserProfile;
import group2.connectsentinel.datamanagement.FakeUserData;
import group2.connectsentinel.datamanagement.UserDataSource;

public class RegisterActivity extends AppCompatActivity {
    //private ArrayList<UserProfile> listUP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button Submit = (Button) findViewById(R.id.Submit);
        Submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                EditText nameInput = findViewById(R.id.nameInput);
                String name = nameInput.getText().toString();

                EditText passwordText = findViewById(R.id.passwordInput);
                EditText emergencyContactInput = findViewById(R.id.emergencyContactInput);
                String password = StringSHA.hashString(passwordText.getText().toString());
                long emergencyContact = -1l;
                try{
                    emergencyContact = Long.parseLong(emergencyContactInput.getText().toString());
                } catch(NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Incorrect phone number entered",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                CheckBox medical = findViewById(R.id.abilityMedical);
                CheckBox crime = findViewById(R.id.abilityCrime);

                EditText phoneInput = findViewById(R.id.phoneInput);
                long phone;
                try {
                    phone = Long.parseLong(phoneInput.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Incorrect phone number entered",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Sylvie: check if the user is banned:
                boolean banned = false;
                String[] ban_keys = new String[]{"enterId", "enterPassword"};
                String[] ban_values = new String[]{Long.toString(phone), password};
                String ban_data = PostDataEncoder.encodePostData(ban_keys, ban_values);
                checkBanUserTask banUserTask = new checkBanUserTask(getApplicationContext());

                try {
                    banUserTask.execute(ban_data).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!banUserTask.getState()) {
                    Toast.makeText(getApplicationContext(), "You are banned from the system.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                //Effie: check if unique
                boolean accountExists = false;
                boolean emergencyContactExists = false;

                UserProfile userProfile = null;

                String[] findKey = new String[]{"Id"};
                String[] findValue = new String[]{Long.toString(phone)};
                String findData = PostDataEncoder.encodePostData(findKey, findValue);
                RequestOneUserTask requestUser = new RequestOneUserTask(getApplicationContext());
                try {
                    requestUser.execute(findData).get();

                } catch (ExecutionException e) {
                } catch (InterruptedException e) {
                }

                userProfile = requestUser.getReturnedOneUser();
                if(userProfile != null) {
                    accountExists = true;
                }

                UserProfile EmergencyProfile = null;

                String[] findKeyE = new String[]{"Id"};
                String[] findValueE = new String[]{Long.toString(emergencyContact)};
                String findDataE = PostDataEncoder.encodePostData(findKeyE, findValueE);
                RequestOneUserTask requestUserE = new RequestOneUserTask(getApplicationContext());
                try {
                    requestUserE.execute(findDataE).get();
                } catch (ExecutionException e) {
                } catch (InterruptedException e) {
                }

                EmergencyProfile = requestUserE.getReturnedOneUser();
                if(EmergencyProfile != null) {
                    emergencyContactExists = true;
                }

                if (phoneInput.length() < 10) {
                    Toast.makeText(getApplicationContext(), "Invalid phone number!",
                            Toast.LENGTH_LONG).show();
                } else if (accountExists) {
                    Toast.makeText(getApplicationContext(), "This phone number has an" +
                                    " existing account!",
                            Toast.LENGTH_LONG).show();
                } else if (banned) {
                    Toast.makeText(getApplicationContext(), "You are banned from the system!" ,
                            Toast.LENGTH_LONG).show();
                } else if (!emergencyContactExists) {
                    Toast.makeText(getApplicationContext(), "The emergency contact does not" +
                                    " exist", Toast.LENGTH_LONG).show();
                } else if (phone == emergencyContact){
                    Toast.makeText(getApplicationContext(), "The emergency contact is the same as the phone number",
                            Toast.LENGTH_LONG).show();
                } else {

                    //UserProfile newUser = new UserProfile(phone, name, emergencyContact,
                            //medical.isChecked(), crime.isChecked());


                    //uds.addUserProfile(newUser, password.getText().toString());

                    String[] keys = new String[]{"newID", "newName", "newEmergencyContactID",
                            "newMedicalAbility", "newCrimeAbility", "newPassword"};
                    String[] values = new String[]{Long.toString(phone), name,
                            Long.toString(emergencyContact), medical.isChecked() ? "true" : "false",
                            crime.isChecked() ? "true" : "false", password};
                    String data = PostDataEncoder.encodePostData(keys, values);
                    String url = MainActivity.SERVER_ROOT + "addUserProfile/";
                    new AddUserProfileTask(getApplicationContext()).execute(data);

                    UserProfile newUP = new UserProfile(phone, name, emergencyContact, medical.isChecked(), crime.isChecked(), password);
                    //MainActivity.listUP.add(newUP);
                    Log.v("addUserProfile", "new user added");

                    SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(MainActivity.PHONE, phone);
                    editor.putString(MainActivity.PASSWORD, password);
                    editor.putString(MainActivity.USER_NAME, name);
                    editor.putBoolean(MainActivity.MEDICAL_ABILITY, medical.isChecked());
                    editor.putBoolean(MainActivity.CRIME_ABILITY, crime.isChecked());
                    editor.putLong(MainActivity.DEFAULT_E_Contact, emergencyContact);
                    editor.putLong(MainActivity.CURRENT_E_CONTACT, emergencyContact);
                    Log.v("emergency", "put in:" + emergencyContact);
                    editor.commit();

                    Intent startIntent = new Intent(getApplicationContext(), DashboardDrawerActivity.class);
                    startIntent.putExtra("userId", phone);
                    startIntent.putExtra("isOnTrip", false);
                    startIntent.putExtra("currEContact", emergencyContact);
                    startIntent.putExtra("defaultEContact", emergencyContact);
                    startIntent.putExtra("userName", name);
                    startActivity(startIntent);

                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }
}