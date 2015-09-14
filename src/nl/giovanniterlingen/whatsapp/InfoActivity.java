package nl.giovanniterlingen.whatsapp;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

        ImageView mChangelogFAB = (ImageView) findViewById(R.id.changelogFAB);
        mChangelogFAB.setOnClickListener(this);
        mChangelogFAB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                return false;
            }
        });
        }

    @Override
     public void onClick(View view){
        if (view.getId() == R.id.changelogFAB) {
            final Dialog changelogDialog = new Dialog(this);
            changelogDialog.setTitle(R.string.changelog);
            changelogDialog.setContentView(R.layout.changelog_dialog);
            changelogDialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
