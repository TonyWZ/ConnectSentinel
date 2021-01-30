package group2.connectsentinel.background;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;

public class AddAbuseReportTask extends PostQueryTask{
    private final String url = "addAbuseReport/";
    private Context context;

    public AddAbuseReportTask(Context context) {
        this.context = context;
    }

    String readResult(HttpURLConnection conn) {
        int responseCode = 0;
        try {
            responseCode = conn.getResponseCode();
            Log.v("POST - addAbuseReport", "Got Respond Code");
        } catch (IOException e) {
            Log.v("POST - addUserProfile", "Error when adding new abuse report");
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
            Toast.makeText(context, "Abuse Report successfully submitted!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Error when submitting, please try another time!" + result,
                    Toast.LENGTH_LONG).show();
        }
    }
}
