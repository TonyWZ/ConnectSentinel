package group2.connectsentinel.background;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class checkBanUserTask extends PostQueryTask{

    private boolean f = false;
    private String mes;
    private final String url = "checkBanUser/";
    private Context context;

    public checkBanUserTask(Context context) {
        this.context = context;
    }

    String readResult(HttpURLConnection conn) {
        String mes = "";
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                mes = inputLine;
            //Log.v("--ReponseMessage--", mes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(mes == null || mes.equals("Error")) {
            this.mes = "Internal Error, Please try again!";
        } else if (mes.equals("Good")) {
            this.mes = "You are banned from the system.";
        } else if (mes.equals("Wrong Password")) {
            this.mes = "Wrong Password";
        } else {
            f = true;
        }
        return mes;
    }

    public boolean getState() {
        return f;
    }

    public String getMes() {
        return mes;
    }

    @Override
    String getURL() {
        return url;
    }


    // return true if the checking process could go on.
    // True - not banned
    // False - banned
    protected void onPostExecute(String result) {
        return;
    }

}
