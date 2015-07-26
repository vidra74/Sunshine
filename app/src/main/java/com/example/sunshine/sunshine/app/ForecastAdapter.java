package com.example.sunshine.sunshine.app;

        import android.content.Context;
        import android.database.Cursor;
        import android.support.v4.widget.CursorAdapter;
        import android.text.format.DateUtils;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import com.example.sunshine.sunshine.app.data.WeatherContract;

        import java.math.RoundingMode;
        import java.text.DateFormat;
        import java.text.SimpleDateFormat;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    private boolean mUseFutureDayViewTypeOnly;

    static class ViewHolder {
        TextView tvDatum;
        TextView tvPrognoza;
        TextView tvhighTemp;
        TextView tvLowTemp;
        ImageView imgPrognoza;
    }

    @Override
    public int getItemViewType(int position) {
        return ((position == 0) && (!mUseFutureDayViewTypeOnly)) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }


    public ForecastAdapter(Context context, Cursor c, int flags) {

        super(context, c, flags);
        mUseFutureDayViewTypeOnly = false;
    }

    public void setViewType(boolean useFutureDayOnly) {
        mUseFutureDayViewTypeOnly = useFutureDayOnly;
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(Context context, double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(context, high, isMetric) + "/" + Utility.formatTemperature(context, low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor, Context context) {


        /* String highAndLow = formatHighLows(context,
                cursor.getDouble(GetWeatherFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(GetWeatherFragment.COL_WEATHER_MIN_TEMP));
        // + " - " + Utility.getPreferredLocation(context);
        */
        return cursor.getString(GetWeatherFragment.COL_WEATHER_DESC);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);

        // return view;

        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId;
        if (viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forecast_today;
        } else {
            layoutId = R.layout.list_item_forecast;
        }

        View view =  LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHold = new ViewHolder();
        viewHold.tvDatum =      (TextView) view.findViewById(R.id.list_item_day_textview);
        viewHold.tvPrognoza =   (TextView) view.findViewById(R.id.list_item_forecast_textview);
        viewHold.tvhighTemp =   (TextView) view.findViewById(R.id.list_item_high_textview);
        viewHold.tvLowTemp =    (TextView) view.findViewById(R.id.list_item_low_textview);
        viewHold.imgPrognoza =  (ImageView)view.findViewById(R.id.list_item_icon);
        view.setTag(viewHold);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        ViewHolder vH = (ViewHolder) view.getTag();

        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(GetWeatherFragment.COL_WEATHER_CONDITION_ID);

        if (getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY) {
            vH.imgPrognoza.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        } else {
            vH.imgPrognoza.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
        }

        vH.tvPrognoza.setText(convertCursorRowToUXFormat(cursor, context));
        vH.tvDatum.setText(Utility.getFriendlyDayString(context, cursor.getLong(GetWeatherFragment.COL_WEATHER_DATE)));

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double temp = cursor.getDouble(GetWeatherFragment.COL_WEATHER_MAX_TEMP);
        vH.tvhighTemp.setText(Utility.formatTemperature(context, temp, isMetric));
        temp = cursor.getDouble(GetWeatherFragment.COL_WEATHER_MIN_TEMP);
        vH.tvLowTemp.setText(Utility.formatTemperature(context, temp, isMetric));
        vH.imgPrognoza.setContentDescription(convertCursorRowToUXFormat(cursor, context));
    }
}