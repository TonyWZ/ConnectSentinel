package group2.connectsentinel.background;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import group2.connectsentinel.data.MissingPerson;
import group2.connectsentinel.data.UserProfile;

public class GetUserProfileTask extends AsyncTask<URL, String, List<UserProfile>> {
    private List<UserProfile> listUP;

    public GetUserProfileTask (List<UserProfile> listUP) {
        this.listUP = listUP;
    }

    protected List<UserProfile> doInBackground(URL... urls) {
        try {
            URL url = urls[0];
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            Log.v("database", "connected!");
            StringBuilder strB = new StringBuilder();
            Scanner in = new Scanner(url.openStream());
            while (in.hasNext()) {
                strB.append(in.nextLine());
            }
            String str = strB.toString();
            Log.v("database", "started writing!");
            JSONArray jo = new JSONArray(str);
            for (int i = 0; i < jo.length(); i++) {
                JSONObject userProfile = jo.getJSONObject(i);
                long id = Long.parseLong(userProfile.getString("id"));
                String name = userProfile.getString("name");
                long emergencyContactId = Long.parseLong(userProfile.getString
                        ("emergencyContactID"));
                boolean abilityMedical = userProfile.getBoolean("medicalAbility");
                boolean abilityCrime = userProfile.getBoolean("crimeAbility");
                String password = userProfile.getString("password");

                UserProfile up = new UserProfile(id, name, emergencyContactId,
                        abilityMedical, abilityCrime, password);
                listUP.add(up);
            }
            if(listUP.isEmpty()) {
                Log.v("database", "listUP is empty!");
            } else {
                Log.v("database", "listUP is not empty!");
            }
            for (UserProfile up : this.listUP) {
                String str1 = up.getName();
            }
            Log.v("testLogin", "task finished, List length is "+Integer.toString(listUP.size()));
            return listUP;
        }
        catch (IOException | JSONException e) {
            Log.v("UserProfile", "Got exception "  + e.toString());
            return null;
        }
    }
}
