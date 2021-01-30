package group2.connectsentinel.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import group2.connectsentinel.R;
import group2.connectsentinel.background.AddAbuseReportTask;
import group2.connectsentinel.background.GetUserProfileTask;
import group2.connectsentinel.background.PostDataEncoder;
import group2.connectsentinel.background.RequestOneUserTask;
import group2.connectsentinel.data.UserProfile;

public class ReportAbuseActivity extends Activity {

    private long fromUserId;
    private ArrayList<UserProfile> listUP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        fromUserId = getIntent().getLongExtra("userId",-1l);
        Log.v("AbuseReportActivity - UserIdGot", Long.toString(fromUserId));
        setContentView(R.layout.activity_report_abuse);
        // User Profile from the Database
        try {
            URL url = new URL("http://10.0.2.2:3000/returnUsers");
            listUP = new ArrayList<UserProfile>();
            GetUserProfileTask task = new GetUserProfileTask(listUP);
            task.execute(url);
        }
        catch (Exception e) {
        }

    }


    public void profilequery(View view) {
        EditText entryIdtext = (EditText) findViewById(R.id.toUserId);
        if (entryIdtext == null) {
            Toast.makeText(
                    this,
                    "Please Enter the Id of the Abuser!!",
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

    public void submitReport(View view) {
        String temp = ((EditText) findViewById(R.id.toUserId)).getText().toString();
        if (temp == null || temp.length() == 0) {
            Toast.makeText(
                    this,
                    "Please Enter a Valid User ID!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        long toUserId = -1l;
        try{
            toUserId = Long.parseLong(temp);
        } catch(NumberFormatException e) {
            Toast.makeText(getApplicationContext(), "Incorrect phone number entered",
                    Toast.LENGTH_LONG).show();
            return;
        }
        String mes = ((EditText) findViewById(R.id.message)).getText().toString();
        String loc = ((EditText) findViewById(R.id.location)).getText().toString();
        //Report rep = new Report(fromUserId, toUserId, mes, loc);
        //ReportDataSource ds = FakeReportData.getInstance();
        try {
            // Code for submitting the report to web server then mongodb
            String[] keys = new String[]{"originUserID", "targetUserID", "description",
                    "date", "location"};
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String[] values = new String[]{Long.toString(fromUserId), Long.toString(toUserId),
            mes, dateFormat.format(date), loc};
            String data = PostDataEncoder.encodePostData(keys, values);
            String url = MainActivity.SERVER_ROOT + "addAbuseReport/";
            new AddAbuseReportTask(getApplicationContext()).execute(data);
            //Log.v("POST - addUserProfile", "new user added");

            //Toast.makeText(
            //        this,
            //"Abuse Report submitted!",
             //       Toast.LENGTH_LONG)
              //      .show();
        } catch(Exception e) {
            Toast.makeText(
                    this,
                    "Error Message: " + e.toString() + ". Please try again",
                    Toast.LENGTH_LONG)
                    .show();
        }
        ((EditText) findViewById(R.id.toUserId)).getText().clear();
        ((EditText) findViewById(R.id.message)).getText().clear();
        ((EditText) findViewById(R.id.location)).getText().clear();
        setResult(RESULT_OK);
        finish();
    }

    public void back2setting(View view) {
        Intent i = new Intent();
        setResult(RESULT_OK, i);
        finish();
    }
}
