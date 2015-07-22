package com.ricardosimoes.bluebike;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;


public class RecordingActivity extends Activity {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private TextView mRecordingDate;
    private TextView mRecordingTime;
    private TextView mRecordingData;
    private String recording[];


    private TextView mDataCadenceMaxField;
    private TextView mDataCadenceAvgField;

    private TextView mDataSpeedMaxField;
    private TextView mDataSpeedAvgField;

    private TextView mDataOdometerField;


    //public static final String DATE_FORMAT_DEFAULT = "dd/MM/yyyy";
    //public static final String HOUR_FORMAT_DEFAULT = "HH:mm:ss";

    private ArrayList<String> cadenceHistory = new ArrayList<String> ();
    private ArrayList<String> speedHistory = new ArrayList<String> ();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        getActionBar().setTitle(R.string.title_devices);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        recording = getIntent().getStringArrayExtra("recording");

        mRecordingDate= (TextView) findViewById(R.id.recording_date);
        mRecordingTime= (TextView) findViewById(R.id.recording_time);
        mRecordingData= (TextView) findViewById(R.id.recording_data);


        mDataCadenceMaxField= (TextView) findViewById(R.id.rec_cadence_max);
        mDataCadenceAvgField= (TextView) findViewById(R.id.rec_cadence_avg);

        mDataSpeedMaxField= (TextView) findViewById(R.id.rec_speed_max);
        mDataSpeedAvgField= (TextView) findViewById(R.id.rec_speed_avg);

        mDataOdometerField = (TextView) findViewById(R.id.rec_odometer);


        String temp="";

        String s[] = recording[1].split("\t");

        Log.d(TAG,  "Recebido: " + recording[1]);

        try {
            FileInputStream inputStream = openFileInput(s[2]);
            int c;
            while( (c = inputStream.read()) != -1){
                if(Character.toString((char)c).equals("\n")) {
                    String t[] = temp.split("\t");
                    speedHistory.add(t[1]);
                    cadenceHistory.add(t[2]);
                    temp =  "";
                }else{
                    temp = temp + Character.toString((char) c);
                }
            }
            //Log.d(TAG,  recording[1]+ ": " + temp);

            mRecordingDate.setText(new SimpleDateFormat(getString(R.string.date_only_format_default)).format(Timestamp.valueOf(s[0])));
            mRecordingTime.setText(getString(R.string.label_from) +" " + new SimpleDateFormat(getString(R.string.hour_format_default)).format(Timestamp.valueOf(s[0])) +" " + getString(R.string.label_to) +" "+ new SimpleDateFormat(getString(R.string.hour_format_default)).format(Timestamp.valueOf(s[1])));
            //mRecordingData.setText(temp);


/*

startTimestamp.toString() + "\t"
                    + currentTimestamp.toString() + "\t"
                    + csvfile + "\t"
                    + wheel_len + "\t"
                    + speed_max + "\t"
                    + speed_avg + "\t"
                    + cadence_max + "\t"
                    + cadence_avg + "\t"
                    + mDataOdometerField.getText() + "\t"
                    + lenght_unit + "\t"

*/

            mDataSpeedMaxField.setText(String.format("%.1f", Float.valueOf(s[4])) +s[9] +"/h");
            mDataSpeedAvgField.setText(String.format("%.1f", Float.valueOf(s[5])) +s[9]+ "/h");
            mDataCadenceMaxField.setText(String.format("%.1f", Float.valueOf(s[6]))+ "rpm");
            mDataCadenceAvgField.setText(String.format("%.1f", Float.valueOf(s[7]))+ "rpm");

            /*
            mDataSpeedMaxField.setText(s[4] + s[9] +"/h");
            mDataSpeedAvgField.setText(s[5] + s[9]+ "/h");
            mDataCadenceMaxField.setText(s[6]+ "rpm");
            mDataCadenceAvgField.setText(s[7]+ "rpm");
            */

            mDataOdometerField.setText(s[8]);

            graphData(cadenceHistory, speedHistory);

        } catch (Exception e) {

            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.text_recording_open_fails), Toast.LENGTH_LONG);
            toast.show();
            e.printStackTrace();
        }



    }

    private void graphData(ArrayList<String> cadenceHistory, ArrayList<String> speedHistory)    {

        LinearLayout layoutS = (LinearLayout) findViewById(R.id.speedXYPlot);
        LinearLayout layoutC = (LinearLayout) findViewById(R.id.cadenceXYPlot);

       int num = 0;
        int i =0;

        //Speed
        num = speedHistory.size();
        GraphView.GraphViewData[] datas = new GraphView.GraphViewData[num];
        Iterator<String> its = speedHistory.iterator();
        i = 0;

        double[] datasa = new double[num];
        while(its.hasNext()) {
            Object obj = its.next();
            datasa[i] = Double.valueOf(obj.toString());
            //Log.d(TAG, String.valueOf(datasa[i]));
            i++;
        }
        for (i=0; i<num; i++) {
            datas[i] = new GraphView.GraphViewData(i, datasa[i]);
        }


        GraphView graphViewSpeed  = new LineGraphView(this, "Velocidade");

        graphViewSpeed.addSeries(new GraphViewSeries(datas));
        graphViewSpeed.setShowHorizontalLabels(false);
        graphViewSpeed.setViewPort(2, 40);
        graphViewSpeed.setScrollable(true);
        graphViewSpeed.setScalable(true);

        layoutS.removeAllViews();
        layoutS.addView(graphViewSpeed);

        //Cadence
        num = cadenceHistory.size();
        GraphView.GraphViewData[] datac = new GraphView.GraphViewData[num];
        Iterator<String> itc = cadenceHistory.iterator();
        i = 0;
        double[] dataca = new double[num];
        while(itc.hasNext()) {
            Object obj = itc.next();
            dataca[i] = Double.valueOf(obj.toString());
            i++;
        }
        for (i=0; i<num; i++) {
            datac[i] = new GraphView.GraphViewData(i, dataca[i]);
        }

        GraphView graphViewCadence  = new LineGraphView(this, "Cadência");
        graphViewCadence.addSeries(new GraphViewSeries(datac));

        graphViewCadence.setViewPort(2, 40);
        graphViewCadence.setScrollable(true);
        graphViewCadence.setScalable(true);
        graphViewCadence.setShowHorizontalLabels(false);

        layoutC.removeAllViews();
        layoutC.addView(graphViewCadence);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recording, menu);
        return true;




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(item.getItemId()) {
/*
            case R.id.menu_delete:
                try {
                    File file = new File(recording[2]);
                    file.delete();
                    Toast toast = Toast.makeText(getApplicationContext(), "Atividade excluída.", Toast.LENGTH_LONG);
                    toast.show();
                    setResult(Activity.RESULT_OK);
                } catch (Exception e) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Não foi possível excluir a atividade gravada.", Toast.LENGTH_LONG);
                    toast.show();
                    e.printStackTrace();
                }

                break;
                */
            case R.id.menu_config:
                final Intent intent1 = new Intent(this, SettingsActivity.class);
                startActivity(intent1);
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
