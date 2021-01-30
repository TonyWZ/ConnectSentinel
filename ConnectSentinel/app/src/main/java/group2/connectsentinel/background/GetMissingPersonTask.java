package group2.connectsentinel.background;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import group2.connectsentinel.data.MissingPerson;

public class GetMissingPersonTask extends AsyncTask<URL, String, List<MissingPerson>> {

    private List<String> display;
    private List<MissingPerson> listMP;
    private ArrayAdapter<String> arrayAdapter;

    public GetMissingPersonTask (List<String> display,
                                 List<MissingPerson> listMP, ArrayAdapter<String> arrayAdapter) {
        this.display = display;
        this.listMP = listMP;
        this.arrayAdapter = arrayAdapter;
    }

    protected List<MissingPerson> doInBackground(URL... urls) {
        Log.v("MissingPerson", "Begin background processes");
        try {
            URL url = urls[0];
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            Log.v("MissingPerson", "Connecting");
            conn.connect();
            StringBuilder strB = new StringBuilder();
            Scanner in = new Scanner(url.openStream());
            while (in.hasNext()) {
                strB.append(in.nextLine());
            }
            String str = strB.toString();
            JSONArray jo = new JSONArray(str);
            Log.v("MissingPerson", "Returned json array has length" + jo.length());
            for (int i = 0; i < jo.length(); i++) {
                JSONObject missingPerson = jo.getJSONObject(i);
                String name = missingPerson.getString("name");
                String street = missingPerson.getString("lastSeenStreet");
                String city = missingPerson.getString("lastSeenCity");
                String state = missingPerson.getString("lastSeenState");
                String description = missingPerson.getString("description");
                long contact = missingPerson.getLong("contactNumber");
                boolean hasImage = missingPerson.getBoolean("hasImage");
                if (!hasImage) {
                    MissingPerson mp = new MissingPerson(street, city, state, description, name,
                            contact, false);
                    listMP.add(mp);
                } else {
                    String imgBase64 = missingPerson.getString("imageBase64");
                    Log.v("Image", imgBase64);
                    MissingPerson mp = new MissingPerson(street, city, state, description,
                            name, contact, imgBase64, true);
                    listMP.add(mp);
                }
            }
            for (MissingPerson mp : this.listMP) {
                String str1 = mp.getName() + " is last seen at " + mp.getLastSeenStreet() + ", " +
                            mp.getLastSeenCity() + ", " + mp.getLastSeenState();
                display.add(str1);
            }
            return listMP;
        }
        catch (IOException | JSONException e) {
            Log.v("MissingPerson", "Got exception "  + e.toString());
            return null;
        }
    }

    protected void onPostExecute(List<MissingPerson> listMP) {
        arrayAdapter.notifyDataSetChanged();
    }
}
