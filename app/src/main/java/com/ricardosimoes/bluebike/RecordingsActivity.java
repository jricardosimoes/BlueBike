package com.ricardosimoes.bluebike;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import java.io.FileInputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class RecordingsActivity extends Activity {

    private List<String> fileList = new ArrayList<String>();
    public static final String RECORDINGS_FILE = "recordings.csv";
    private TextView mDataRecordingsField;
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private ArrayList<String> listValuesStart = new ArrayList<String> ();
    private ArrayList<String> listValuesAll= new ArrayList<String> ();

    public static final String DATE_FORMAT_DEFAULT = "dd/MM/yyyy HH:mm:ss";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_list_logs);
        getActionBar().setTitle(R.string.title_devices);
        setContentView(R.layout.activity_recordings);
        getActionBar().setDisplayHomeAsUpEnabled(true);


        final ListView listview = (ListView) findViewById(R.id.list_files);
        //String[] values = new String[]{};


        String temp="";

        try {

            long unixTime = System.currentTimeMillis() / 1000L;

            FileInputStream inputStream = openFileInput(RECORDINGS_FILE);
            int c;

            while( (c = inputStream.read()) != -1){
                if(Character.toString((char)c).equals("\n")) {
                    String s[] = temp.split("\t");

                   // new SimpleDateFormat(DATE_FORMAT_DEFAULT).format(currentTimestamp)

                    listValuesStart.add(new SimpleDateFormat(DATE_FORMAT_DEFAULT).format(Timestamp.valueOf(s[0])));
                    listValuesAll.add(temp);
                    Log.d(TAG, RECORDINGS_FILE + " line: " + temp);
                    temp =  "";
                }else{
                    temp = temp + Character.toString((char) c);
                }
            }


            final ArrayAdapter adapter = new ArrayAdapter(this,
                    android.R.layout.simple_list_item_1, listValuesStart);

            listview.setAdapter(adapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Intent intent = new Intent(getApplicationContext(), RecordingActivity.class);
                    intent.putExtra("recording", new String[]{listValuesStart.get(position), listValuesAll.get(position)});
                    startActivity(intent);
                    Log.d(TAG, "Selecionado: " + listValuesStart.get(position) + "/" + listValuesAll.get(position));
                }
            });


        } catch (Exception e) {

            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.text_no_recording), Toast.LENGTH_LONG);
            toast.show();
            e.printStackTrace();
        }



        ListDir();


    }

    void ListDir(){




        /*

        ArrayAdapter<String> directoryList
                = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, fileList);
        setListAdapter(directoryList);
        */
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_logs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {


            case R.id.menu_config:
                final Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.menu_about:
                final Intent intent2 = new Intent(this, AboutActivity.class);
                startActivity(intent2);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
