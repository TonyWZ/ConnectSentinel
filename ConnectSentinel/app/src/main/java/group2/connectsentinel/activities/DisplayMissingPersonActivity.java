package group2.connectsentinel.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import group2.connectsentinel.R;
import group2.connectsentinel.background.GetMissingPersonTask;
import group2.connectsentinel.data.MissingPerson;
import group2.connectsentinel.datamanagement.FakeMissingPersonData;
import group2.connectsentinel.datamanagement.MissingPersonDataSource;

public class DisplayMissingPersonActivity extends AppCompatActivity {

    private List<MissingPerson> listMP = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_missing_person);

        ListView lv = (ListView) findViewById(R.id.listview);
        List<String> display = new ArrayList<>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, display);

        try {
            URL url = new URL("http://10.0.2.2:3000/returnAllMissingPeople");
            GetMissingPersonTask task = new GetMissingPersonTask(display, listMP, arrayAdapter);
            task.execute(url);
        }
        catch (Exception e) {
        }

        lv.setAdapter(arrayAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), MissingPersonDetailDisplay.class);
                intent.putExtra("name", listMP.get(i).getName());
                String str = listMP.get(i).getLastSeenStreet() + ", " +
                        listMP.get(i).getLastSeenCity() + ", " + listMP.get(i).getLastSeenState();
                intent.putExtra("lastSeen", str);
                intent.putExtra("description", listMP.get(i).getDescription());
                intent.putExtra("contact", listMP.get(i).getContact());
                intent.putExtra("hasImage", listMP.get(i).getHasImage());
                if (listMP.get(i).getHasImage()) {
                    intent.putExtra("image", listMP.get(i).getImage());
                }
                startActivity(intent);
            }
        });
    }

    public void endActivity(View view) {
        Intent i = new Intent();
        setResult(RESULT_OK, i);
        finish();
    }

}
