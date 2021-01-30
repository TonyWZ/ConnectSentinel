package group2.connectsentinel.background;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import group2.connectsentinel.activities.MainActivity;

public class SendImageTask extends AsyncTask<Bitmap, String, String> {

    private final String urlString = MainActivity.SERVER_ROOT + "processImage";
    private Context context;

    public SendImageTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Bitmap... bitmaps) {
        if(bitmaps.length < 1) {
            return null;
        }
        Bitmap bitmap = bitmaps[0];
        //bitmap.recycle();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream out = conn.getOutputStream();
            out.flush();
            out.close();
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            if(responseCode == 200) {
                return "OK";
            } else {
                return null;
            }
        }
        catch (IOException e) {
            Log.v("SendImage", "Error when getting response code");
            Log.v("SendImage", e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if(result != null && result.equals("OK")) {
            Log.v("SendImage","Showing image sent toast");
            Toast.makeText(context, "Successfully reported!",
                    Toast.LENGTH_LONG).show();
        } else {
            Log.v("SendImage","Failed image sending toast");
            Toast.makeText(context, "Failed to report!",
                    Toast.LENGTH_LONG).show();
        }
    }

}
