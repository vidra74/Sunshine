package com.example.sunshine.sunshine.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sunshine.sunshine.app.data.WeatherContract;

import java.net.URI;
import java.text.DateFormat;
import java.util.Calendar;


/**
 * A placeholder fragment containing a simple view.
 */
public class WeatherDayDetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = WeatherDayDetailFragment.class.getSimpleName();
    private String HASHTAG = " #SunshineApp";

    private ShareActionProvider mShareActionProvider;
    private String mPoruka;

    public static String DETAIL_URI = "Detail_URI";
    private static final int DETAIL_LOADER_ID = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_WIND = 6;
    private static final int COL_WEATHER_PRESSURE = 7;
    private static final int COL_WEATHER_DEGREES = 8;
    private static final int COL_WEATHER_CONDITION_ID = 9;


    public TextView mDetailDay;
    public TextView mDetailDate;
    public TextView mDetailWeatherDesc;
    public TextView mDetailHighTemp;
    public TextView mDetailLowTemp;
    public TextView mDetailHumidity;
    public TextView mDetailPressure;
    public TextView mDetailWindDesc;
    public ImageView mDetailImage;

    public Uri mUri;

    /**
     * Create a new instance of WeatherDayDetailFragment,
     * initialized to show the uri at 'WeatherDateLocationUri'.
     */
    public static WeatherDayDetailFragment newInstance(Uri dateUri) {
        WeatherDayDetailFragment f = new WeatherDayDetailFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("WeatherDateLocationUri", dateUri.toString());
        f.setArguments(args);

        return f;
    }

    public String getWeatherDateLocationUri() {
        return getArguments().getString("WeatherDateLocationUri");
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onDetailChanged(Uri dateUri);
    }

    public WeatherDayDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }

        Bundle args = getArguments();
        if (null != args) {

            mUri = args.getParcelable(DETAIL_URI);
        }

        View rootView =  inflater.inflate(R.layout.fragment_weather_day_detail, container, false);
        // View rootView =  inflater.inflate(R.layout.fragment_detail_wide, container, false);
        mDetailDay = (TextView)rootView.findViewById(R.id.detail_item_day);
        mDetailDate = (TextView)rootView.findViewById(R.id.detail_item_date);
        mDetailWeatherDesc = (TextView)rootView.findViewById(R.id.detail_item_forecast_textview);
        mDetailHighTemp = (TextView)rootView.findViewById(R.id.detail_item_high_textview);
        mDetailLowTemp = (TextView)rootView.findViewById(R.id.detail_item_low_textview);
        mDetailHumidity = (TextView)rootView.findViewById(R.id.detail_item_humidity);
        mDetailPressure = (TextView)rootView.findViewById(R.id.detail_item_pressure);
        mDetailWindDesc = (TextView)rootView.findViewById(R.id.detail_item_wind);
        mDetailImage = (ImageView)rootView.findViewById(R.id.detail_item_icon);

        return rootView;
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mPoruka + HASHTAG);
        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (mPoruka != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mPoruka + HASHTAG);
        return intent;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("Location", Utility.getPreferredLocation(getActivity()));
        outState.putLong("DateinMills", Calendar.getInstance().getTimeInMillis());
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "In onCreateLoader");
        /*Intent intent = getActivity().getIntent();
        if ((intent == null) || (null == intent.getData())) {
            Log.d("DetailFragment", "Intent == null");

*/
        if (mUri == null) {
                mUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        Utility.getPreferredLocation(getActivity()),
                        Calendar.getInstance().getTimeInMillis());
            }
        Log.d("DetailFragment", "onCreateLoader mUri: " + mUri.toString());
        return new CursorLoader(
                    getActivity(),
                    mUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null);
        }
/*
        // Now create and return a CursorLoader that will take care of

        // creating a Cursor for the data being displayed.

        Log.d("DetailFragment", "onCreateLoader intent data: " + intent.getData());
        return new CursorLoader(
                getActivity(),
                intent.getData(),
                FORECAST_COLUMNS,
                null,
                null,
                null);

    }*/

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }

        Context context = getView().getContext();

        Long dateMills = data.getLong(COL_WEATHER_DATE);
        String weatherDescription = data.getString(COL_WEATHER_DESC);
        Float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        Float pressure = data.getFloat(COL_WEATHER_PRESSURE);
        Float wind = data.getFloat(COL_WEATHER_WIND);
        Float degrees = data.getFloat(COL_WEATHER_DEGREES);
        Integer weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(context,
                data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

        String low = Utility.formatTemperature(context,
                data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);


        mDetailDay.setText(Utility.getFriendlyDayString(context, dateMills));
        mDetailDate.setText(Utility.getFormattedMonthDay(context, dateMills));
        mDetailHumidity.setText(String.format(context.getString(R.string.format_humidity), humidity));
        mDetailWindDesc.setText(Utility.getFormattedWind(context, wind, degrees));
        mDetailPressure.setText(String.format(context.getString(R.string.format_pressure), pressure));
        mDetailHighTemp.setText(high);
        mDetailLowTemp.setText(low);
        mDetailWeatherDesc.setText(weatherDescription);
        mDetailImage.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        mDetailImage.setContentDescription(weatherDescription);

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ;
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        if (mUri == null){
            return;
        }

        Log.d("DetailFragment", "onLoaderReset mUri: " + mUri.toString());
        long date = WeatherContract.WeatherEntry.getDateFromUri(mUri);
        Log.d("DetailFragment", "onLoaderReset date from mUri: " + DateFormat.getDateInstance().format(date));
        mUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
        Log.d("DetailFragment", "onLoaderReset updated mUri: " + mUri.toString());
        // if (null != getLoaderManager().getLoader(DETAIL_LOADER_ID)) {
            Log.d("DetailFragment", "onLoaderReset restartLoader" + mUri.toString());
            getLoaderManager().restartLoader(DETAIL_LOADER_ID,null,this);
        /*} else {
            Log.d("DetailFragment", "onLoaderReset initLoader");
            getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        }*/

    }
}
