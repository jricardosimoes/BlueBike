package com.ricardosimoes.bluebike;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;


public class AboutActivity extends Activity {

    private WebView mWebViewAbout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.title_devices);
        setContentView(R.layout.activity_about);
        getActionBar().setDisplayHomeAsUpEnabled(true);


/*


        TextView mabout_title_app    =  (TextView)findViewById(R.id.about_title_app);
        TextView mabout_content_app  =  (TextView)findViewById(R.id.about_content_app);
        TextView mabout_title_dev    =  (TextView)findViewById(R.id.about_title_dev);
        TextView mabout_content_dev  =  (TextView)findViewById(R.id.about_content_dev);
        TextView mabout_content_url  =  (TextView)findViewById(R.id.about_content_url);
        TextView mabout_content_email=  (TextView)findViewById(R.id.about_content_email);
        TextView mabout_title_os     =  (TextView)findViewById(R.id.about_title_os);
        TextView mabout_content_os   =  (TextView)findViewById(R.id.about_content_os);

*/

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(item.getItemId()) {
            /*case R.id.menu_config:
                final Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            */
            case android.R.id.home:
                onBackPressed();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }



}
