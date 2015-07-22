package com.ricardosimoes.bluebike;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(item.getItemId()) {


            case R.id.menu_new:
                final Intent intent = new Intent(this, DeviceScanActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_config:
                final Intent intent1 = new Intent(this, SettingsActivity.class);
                startActivity(intent1);
                break;
            case R.id.menu_about:
                final Intent intent2 = new Intent(this, AboutActivity.class);
                startActivity(intent2);
                break;

            case R.id.menu_recordings:
                final Intent intent3 = new Intent(this, RecordingsActivity.class);
                startActivity(intent3);
                break;



        }
        return super.onOptionsItemSelected(item);

    }
}
