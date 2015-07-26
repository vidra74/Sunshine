package com.example.sunshine.sunshine.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;


import com.example.sunshine.sunshine.app.data.WeatherContract;
import com.example.sunshine.sunshine.app.service.ServiceSunshine;
import com.example.sunshine.sunshine.app.sync.SunshineSyncAdapter;


/**
 * A placeholder fragment containing a simple view.
 */
public class GetWeatherFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FORECAST_LOADER_ID = 1;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    private ForecastAdapter adapterPrognoza;
    private boolean mUseFutureDayViewTypeOnly;
    private String FORECASTFRAGMENT_TAG ;
    private Uri mUri;
    private final String LOG_TAG = GetWeatherFragment.class.getSimpleName();
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    Callback mCallbackListener;
    int mPosition;

    public void setUseFutureDayViewTypeOnly(boolean useViewTypeFutureDayOnly){
        mUseFutureDayViewTypeOnly = useViewTypeFutureDayOnly;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapterPrognoza.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            ListView listView = (ListView) getActivity().findViewById(R.id.listview_forecast);
            listView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapterPrognoza.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt("selected_item_position", mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocation = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting,
                System.currentTimeMillis());

        // proširi sa parametrima
        // http://developer.android.com/reference/android/content/CursorLoader.html#CursorLoader(android.content.Context, android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
        return new CursorLoader(getActivity(),
                weatherForLocation,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbackListener = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Callback interface");
        }
    }



    public GetWeatherFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.forecastfragment, menu);
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged( ) {
        Log.d(LOG_TAG, "onLocationChanged updateWeatherData");
        updateWeatherData();
    }

    private void updateWeatherData(){

        Log.d(LOG_TAG, "updateWeatherData() započeo rad");
        /*Intent intent = new Intent(getActivity(), ServiceSunshine.AlarmReceiver.class);
        intent.putExtra(ServiceSunshine.PREFERRED_LOCATION, Utility.getPreferredLocation(getActivity()));

        PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmMgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + 5 * 1000,
                        alarmIntent);

        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);*/
        // getActivity().startService(intent);

        SunshineSyncAdapter.syncImmediately(getActivity());
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
        Log.d(LOG_TAG, "updateWeatherData() završio rad");
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // Toast.makeText(getActivity(), "Smallest screen width in Dp: " + Integer.toString(getResources().getConfiguration().smallestScreenWidthDp), Toast.LENGTH_LONG).show();
                Log.d(LOG_TAG, "onOptionsItemSelected() R.id.action_refresh updateWeatherData");
                updateWeatherData();
                return true;
            default:

                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        adapterPrognoza = new ForecastAdapter(getActivity(), null, 0);
        adapterPrognoza.setViewType(mUseFutureDayViewTypeOnly);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapterPrognoza);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    mUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting,
                            cursor.getLong(COL_WEATHER_DATE));
                    mCallbackListener.onItemSelected(mUri);
                }
                mPosition = position;
            }
        });
        if(savedInstanceState != null && savedInstanceState.containsKey("selected_item_position")) {
            mPosition = savedInstanceState.getInt("selected_item_position");
        }

        return rootView;
    }


}
