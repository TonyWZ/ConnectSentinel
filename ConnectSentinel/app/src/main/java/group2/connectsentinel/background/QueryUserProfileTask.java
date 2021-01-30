package group2.connectsentinel.background;

import android.os.AsyncTask;
import android.util.Log;

import com.twilio.rest.chat.v1.service.User;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import group2.connectsentinel.activities.MainActivity;
import group2.connectsentinel.data.UserProfile;

public abstract class QueryUserProfileTask extends AsyncTask<String, String, UserProfile> {

    protected UserProfile doInBackground(String... args) {
        if (args.length < 1) {
            return null;
        }
        try{
            Log.v("QueryServer", "Got request");
            URL url = new URL(MainActivity.SERVER_ROOT + getURL());
            String params = args[0];
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty( "charset", "utf-8");
            OutputStream os = conn.getOutputStream();
            os.write(params.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
            Log.v("QueryServer", "Data sent");
            int responseCode = conn.getResponseCode();
            Log.v("QueryServer", "Got response " + responseCode);
            if(responseCode == 200) {
                UserProfile result = readResult(conn);
                return result;
            } else {
                return null;
            }
        } catch (IOException e) {
            Log.v("DistressAlarm","IOException encountered: " + e);
            return null;
        }
    }

    abstract UserProfile readResult(HttpURLConnection conn);

    abstract String getURL();

    protected abstract void onPostExecute(String result);
}
