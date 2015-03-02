package com.techiedb.apps.handquill;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class HandQuillActivity extends Activity {

    public static final int SIGNATURE_ACTIVITY = 1;
    private Button mGetSignatureButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.mGetSignatureButton = (Button) findViewById(R.id.button_get_signature);
        this.mGetSignatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                final Intent intent = new Intent(HandQuillActivity.this, SignatureActivity.class);
                startActivityForResult(intent, SIGNATURE_ACTIVITY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SIGNATURE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    final Bundle bundle = data.getExtras();
                    String status = bundle.getString("status");
                    if (status.equalsIgnoreCase("done")) {
                        final Toast toast = Toast.makeText(this, "Signature capture successful!", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 105, 50);
                        toast.show();
                    }
                }
                break;
        }
    }
}
