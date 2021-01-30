package group2.connectsentinel.background;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;

public class SendMissingPersonRequestTask extends PostQueryTask {

    private final String url = "processNewMissingRequest/";
    private Context context;

    public SendMissingPersonRequestTask(Context context) {
        this.context = context;
    }

    @Override
    String readResult(HttpURLConnection conn) {
        int responseCode = 0;
        try {
            responseCode = conn.getResponseCode();
        } catch (IOException e) {
            Log.v("MissingPersonRequests", "Error when getting response code");
            Log.v("MissingPersonRequests", e.toString());
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

    @Override
    protected void onPostExecute(String result) {
        if(result != null && result.equals("OK")) {
            Log.v("MissingPersonRequest","Showing toast");
            Toast.makeText(context, "Successfully reported!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed to report!",
                    Toast.LENGTH_LONG).show();
        }
    }
}
