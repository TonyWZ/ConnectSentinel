package group2.connectsentinel.background;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import group2.connectsentinel.activities.MainActivity;
import group2.connectsentinel.data.UserProfile;

public class RequestOneUserTask extends PostQueryTask {
    private UserProfile up;
    private final String url = "returnOneUser/";
    private Context context;

    public RequestOneUserTask(Context context) {
        this.context = context;
    }

    @Override
    String readResult(HttpURLConnection conn) {
        String mes = "";
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                mes = mes + inputLine;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v("returnedMessage", mes);

        try {
            JSONObject userProfile = new JSONObject(mes);
            long id = Long.parseLong(userProfile.getString("id"));
            String name = userProfile.getString("name");
            Log.v("returnOneUser", "returned user name is null? in execute " + (name ==null));
            long emergencyContactId = Long.parseLong(userProfile.getString
                    ("emergencyContactID"));
            boolean abilityMedical = userProfile.getBoolean("medicalAbility");
            boolean abilityCrime = userProfile.getBoolean("crimeAbility");
            String password = userProfile.getString("password");
            Log.v("returnOneUser", "password is " + password);
            up = new UserProfile(id, name, emergencyContactId,
                    abilityMedical, abilityCrime, password);
            Log.v("returnOneUser", "user profile is built");
        } catch (JSONException e) {
            Log.v("returnOneUser", "Got exception "  + e.toString());
        }
        return mes;
    }

    @Override
    String getURL() {
        return url;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.v("returnOneUser", "onPostExecute");
    }

    public UserProfile getReturnedOneUser() {
        return up;
    }
}
