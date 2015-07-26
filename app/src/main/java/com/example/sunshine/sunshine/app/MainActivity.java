package com.example.sunshine.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.sunshine.sunshine.app.service.ServiceSunshine;
import com.example.sunshine.sunshine.app.sync.SunshineSyncAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;


public class MainActivity extends ActionBarActivity
    implements GetWeatherFragment.Callback {

    public String mLocation;
    public Boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "MainActivity.onDestroy");
        super.onDestroy();

    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "MainActivity.onStop");
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "MainActivity.onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "MainActivity.onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "MainActivity.onResume");
        String location = Utility.getPreferredLocation(this);

        if (location != null && !location.equals(mLocation)) {
            GetWeatherFragment ff = (GetWeatherFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (null != ff) {
                ff.onLocationChanged();
            }
            WeatherDayDetailFragment df = (WeatherDayDetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "MainActivity.onCreate");
        mLocation = Utility.getPreferredLocation(getApplicationContext());
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new WeatherDayDetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        GetWeatherFragment forecastFragment = (GetWeatherFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        forecastFragment.setUseFutureDayViewTypeOnly(mTwoPane);
        SunshineSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intentSettings = new Intent(this, SettingsActivity.class);
            startActivity(intentSettings);
            return true;
        }
        if (id == R.id.action_map) {

            mLocation = Utility.getPreferredLocation(this);
            Uri builder = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", mLocation).build();
            showMap(builder);
            return true;
        }
        if (id == R.id.action_download_data) {

            Intent intent = new Intent(this, ServiceSunshine.class);
            intent.putExtra(ServiceSunshine.PREFERRED_LOCATION, Utility.getPreferredLocation(this));
            startService(intent);
            return true;
        }

        if (id == R.id.action_backup_database) {
            try {
                File sd = Environment.getExternalStorageDirectory();
                File currentDB = getApplicationContext().getDatabasePath("weather.db"); //databaseName=your current application database name, for example "my_data.db"
                if (sd.canWrite()) {
                    File backupDB = new File(sd, "sunshine_backup.db"); // for example "my_data_backup.db"
                    if (currentDB.exists()) {
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    }
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri dateUri) {

        Log.d("GetWeatherFragment", "onItemSelected uri: " + dateUri.toString());

        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(WeatherDayDetailFragment.DETAIL_URI, dateUri);
            WeatherDayDetailFragment df = new WeatherDayDetailFragment();
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.weather_detail_container, df, DETAILFRAGMENT_TAG)
                .commit();

            /* if ( null != df ) {
                df.mUri = dateUri;
                df.onLocationChanged(mLocation);
            } */
        } else {
            Intent intent = new Intent(this, WeatherDayDetail.class).setData(dateUri);
            startActivity(intent);
        }
    }
}
