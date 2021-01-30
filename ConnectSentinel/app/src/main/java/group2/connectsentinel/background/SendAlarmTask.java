package group2.connectsentinel.background;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;

public class SendAlarmTask extends PostQueryTask {

    private final String url = "propagateAlarm/";
    private Context context;

    public SendAlarmTask(Context context) {
        this.context = context;
    }

    String readResult(HttpURLConnection conn) {
        int responseCode = 0;
        try {
            responseCode = conn.getResponseCode();
        } catch (IOException e) {
            Log.v("DistressAlarm", "Error when getting response code");
            Log.v("DistressAlarm", e.toString());
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
            Log.v("DistressAlarm","Showing toast");
            Toast.makeText(context, "Alarm Sent!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed to send alarm!",
                    Toast.LENGTH_LONG).show();
        }
    }
}
