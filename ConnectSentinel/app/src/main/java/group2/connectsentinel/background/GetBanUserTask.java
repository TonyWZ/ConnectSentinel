package group2.connectsentinel.background;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import group2.connectsentinel.data.BanUser;

public class GetBanUserTask extends AsyncTask<URL, String, List<BanUser>> {

    private List<BanUser> listBU;

    public GetBanUserTask(List<BanUser> listBU) {
        this.listBU = listBU;
    }

    @Override
    protected List<BanUser> doInBackground(URL... urls) {

        try {
            URL url = urls[0];
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            StringBuilder strB = new StringBuilder();
            Scanner in = new Scanner(url.openStream());
            while (in.hasNext()) {
                strB.append(in.nextLine());
            }
            String str = strB.toString();
            JSONArray jo = new JSONArray(str);
            for (int i = 0; i < jo.length(); i++) {
                JSONObject banUser = jo.getJSONObject(i);
                long id = banUser.getLong("id");
                String password = banUser.getString("password");
                BanUser bu = new BanUser(id, password);
                listBU.add(bu);
                //Log.v("!!BanUser!!", password);
            }
            return listBU;
        }
        catch (IOException | JSONException e) {
            Log.v("BanUser", "Got exception "  + e.toString());
            return null;
        }
    }
}
