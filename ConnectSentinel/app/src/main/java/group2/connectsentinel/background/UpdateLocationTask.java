package group2.connectsentinel.background;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;

public class UpdateLocationTask extends PostQueryTask {

    private final String url = "updateLocation/";

    @Override
    protected void onPostExecute(String result) {
        if(result != null && result.equals("OK")) {
            Log.v("LocationUpdate","Update Successful");
        } else {
            Log.v("LocationUpdate","Update Failed");
        }
    }

    @Override
    String readResult(HttpURLConnection conn) {
        int responseCode = 0;
        try {
            responseCode = conn.getResponseCode();
        } catch (IOException e) {
            Log.v("LocationUpdate", "Error when getting response code");
            Log.v("LocationUpdate", e.toString());
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
}
