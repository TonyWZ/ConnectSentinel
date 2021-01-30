package group2.connectsentinel.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import group2.connectsentinel.R;

import static java.lang.Boolean.FALSE;

public class ProfileDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_display);

        String id = String.valueOf(getIntent().getLongExtra("ID", -1l));
        String name =  getIntent().getStringExtra("NAME");
        Boolean mTF = getIntent().getBooleanExtra("MedicalTF", FALSE);
        Boolean cTF = getIntent().getBooleanExtra("CrimeTF", FALSE);

        String medicalAbility;
        String crimeAbility;
        if (mTF)
            medicalAbility = "YES";
        else
            medicalAbility = "NO";

        if (cTF)
            crimeAbility = "YES";
        else
            crimeAbility = "NO";

        TextView idtv = (TextView) findViewById(R.id.phoneNumber); idtv.setText(id);
        TextView nametv = (TextView) findViewById(R.id.name); nametv.setText(name);
        TextView medicaltv = (TextView) findViewById(R.id.medicalAbility); medicaltv.setText(medicalAbility);
        TextView crimetv = (TextView) findViewById(R.id.crimeAbility); crimetv.setText(crimeAbility);
        
    }

    public void back2setting(View view) {
        Intent i = new Intent();
        setResult(RESULT_OK, i);
        finish();
    }

}
