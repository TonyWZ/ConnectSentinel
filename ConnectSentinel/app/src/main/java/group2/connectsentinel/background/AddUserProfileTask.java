package group2.connectsentinel.background;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;

public class AddUserProfileTask extends PostQueryTask{
    private final String url = "addUserProfile/";
    private Context context;

    public AddUserProfileTask(Context context) {
        this.context = context;
    }

    String readResult(HttpURLConnection conn) {
        int responseCode = 0;
        try {
            responseCode = conn.getResponseCode();
            Log.v("addUserProfile", "Got Respond Code");
        } catch (IOException e) {
            Log.v("addUserProfile", "Error when adding new user");
            return null;
        }
        if(responseCode == 200) {
            return "OK";
        } else {
            return null;
        }
    }

    @Override
    String getURL() {
        return url;
    }

    protected void onPostExecute(String result) {
        if(result != null && result.equals("OK")) {
            Toast.makeText(context, "added new user!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed to add new user!",
                    Toast.LENGTH_LONG).show();
        }
    }
}
