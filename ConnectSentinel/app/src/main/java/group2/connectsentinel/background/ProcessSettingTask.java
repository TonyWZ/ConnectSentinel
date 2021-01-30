package group2.connectsentinel.background;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;

public class ProcessSettingTask extends PostQueryTask{
    private final String url = "processSetting/";
    private Context context;

    public ProcessSettingTask(Context context) {
        this.context = context;
    }

    String readResult(HttpURLConnection conn) {
        int responseCode = 0;
        try {
            responseCode = conn.getResponseCode();
        } catch (IOException e) {
            Log.v("ProcessSettingError", "Error when getting response code");
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
            Toast.makeText(context, "processed setting!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed to process setting!",
                    Toast.LENGTH_LONG).show();
        }
    }
}
