package nl.giovanniterlingen.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Alessio on 11/08/2015.
 */
public class StatusActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView StatusCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status);
        Button backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(this);
        Button saveButton = (Button) findViewById(R.id.save_status_button);
        saveButton.setOnClickListener(this);
        EditText statusText = (EditText) findViewById(R.id.status_text);
        StatusCounter = (TextView) findViewById(R.id.status_counter);
        statusText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                StatusCounter.setText(String.valueOf(charSequence.length()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back_button) {
            Intent intent = new Intent(this, Main.class);
            startActivity(intent);
            finish();
        } else if (view.getId() == R.id.save_status_button) {
            //Set changed status
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        finish();
    }
}
