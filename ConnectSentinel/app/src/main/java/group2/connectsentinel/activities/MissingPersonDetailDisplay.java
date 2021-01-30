package group2.connectsentinel.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import group2.connectsentinel.R;

import static java.lang.Boolean.FALSE;

public class MissingPersonDetailDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missing_person_detail_display);

        String contact = String.valueOf(getIntent().getLongExtra("contact", -1));
        String name =  getIntent().getStringExtra("name");
        String description =  getIntent().getStringExtra("description");
        String lastSeen =  getIntent().getStringExtra("lastSeen");
        boolean hasImage = getIntent().getBooleanExtra("hasImage", false);

        TextView t1 = (TextView) findViewById(R.id.Name);
        t1.setText(name);
        TextView t2 = (TextView) findViewById(R.id.Description);
        t2.setText(description);
        TextView t3 = (TextView) findViewById(R.id.LastSeen);
        t3.setText(lastSeen);
        TextView t4 = (TextView) findViewById(R.id.Contact);
        t4.setText(contact);

        if (hasImage) {
            String imgBase64 = getIntent().getStringExtra("image");
            ImageView imageDisplay = (ImageView) findViewById(R.id.image);

                Log.v("Image", imgBase64);
                byte[] decodedBytes = Base64.getMimeDecoder().decode(imgBase64);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imageDisplay.setImageBitmap(bitmap);

        }

        Button back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
}
