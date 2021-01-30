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

public class RequestUserProfile extends PostQueryTask {

    @Override
    String readResult(HttpURLConnection conn) {
       return null;
    }

    @Override
    String getURL() { return null;}

    @Override
    protected void onPostExecute(String result) {
    }
}
