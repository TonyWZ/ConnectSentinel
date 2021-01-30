package group2.connectsentinel.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import group2.connectsentinel.R;
import group2.connectsentinel.background.PostDataEncoder;
import group2.connectsentinel.background.SendAlarmTask;
import group2.connectsentinel.background.SendImageTask;
import group2.connectsentinel.background.SendMissingPersonRequestTask;
import group2.connectsentinel.data.MissingPerson;
import group2.connectsentinel.datamanagement.FakeMissingPersonData;
import group2.connectsentinel.datamanagement.MissingPersonDataSource;

public class ReportMissingPersonActivity extends AppCompatActivity {

    private Uri image;
    private Bitmap bitmap;
    private ImageView imageDisplay;
    private Button chooseImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_missing_person);
        requestStoragePermission();
        imageDisplay = (ImageView) findViewById(R.id.imageDisplay);
        chooseImage = (Button) findViewById(R.id.chooseImage);
        chooseImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i,
                        "Upload a picture of the missing person"), 4);
            }
        });

        Button report = (Button) findViewById(R.id.report);
        report.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                EditText et1 = (EditText) findViewById(R.id.missingPersonNameInput);
                String name = et1.getText().toString();
                EditText et2 = (EditText) findViewById(R.id.lastSeenStreetInput);
                String lastSeenStreet = et2.getText().toString();
                EditText et3 = (EditText) findViewById(R.id.missingPersonDescriptionInput);
                String description = et3.getText().toString();
                EditText et4 = (EditText) findViewById(R.id.lastSeenCityInput);
                String lastSeenCity = et4.getText().toString();
                EditText et5 = (EditText) findViewById(R.id.lastSeenStateInput);
                String lastSeenState = et5.getText().toString();
                String contactNumber =
                        String.valueOf(getIntent().getLongExtra("contact", -10));
                Log.v("MissingReport", contactNumber);
                String[] keys = new String[7];
                keys[0] = "name";
                keys[1] = "lastSeenStreet";
                keys[2] = "lastSeenCity";
                keys[3] = "lastSeenState";
                keys[4] = "description";
                keys[5] = "contactNumber";
                keys[6] = "picture";
                String[] values = new String[7];
                values[0] = name;
                values[1] = lastSeenStreet;
                values[2] = lastSeenCity;
                values[3] = lastSeenState;
                values[4] = description;
                values[5] = contactNumber;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                String imgData = Base64.encodeToString(byteArray, Base64.URL_SAFE);
                values[6] = imgData;
                PostDataEncoder pde = new PostDataEncoder();
                String data = pde.encodePostData(keys, values);
                new SendMissingPersonRequestTask(getApplicationContext()).execute(data);
                setResult(RESULT_OK);
                finish();
            }
                // TODO: Image need to somehow be handled
        });

        Button back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
            // TODO: Image need to somehow be handled
        });

    }

    private void requestStoragePermission() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
    }

    protected void onRequestPermissionResult(int requestCode, String[] permissions,
                                             int[] grantResults) {
        if (requestCode == 3) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Access granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Access denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 4 && resultCode == RESULT_OK) {
            image = intent.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image);
                imageDisplay.setImageBitmap(bitmap);
            } catch (IOException e) {
            }
        }
    }

}
