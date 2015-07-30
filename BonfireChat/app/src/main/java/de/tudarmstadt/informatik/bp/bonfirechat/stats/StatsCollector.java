package de.tudarmstadt.informatik.bp.bonfirechat.stats;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireAPI;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.location.GpsTracker;

/**
 * Created by johannes on 10.07.15.
 */
public class StatsCollector extends BroadcastReceiver {

    private static final String TAG = "StatsCollector";

    public static final long PUBLISH_INTERVAL = AlarmManager.INTERVAL_HALF_HOUR;

    // action in Intents which are sent to the service
    public static final String PUBLISH_STATS_ACTION = "de.tudarmstadt.informatik.bp.bonfirechat.PUBLISH_STATS";

    private StatsEntry stats;
    private int batteryLastLevel;
    private long batteryLastMeasured;

    public StatsCollector(Context ctx) {
        // get global stats object
        stats = CurrentStats.getInstance();
        // initialize measurement
        batteryLastLevel = stats.batteryLevel;
        batteryLastMeasured = System.currentTimeMillis();
        // listen for battery status
        ctx.registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    /*
     * handle stats intents
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        BonfireData db = BonfireData.getInstance(context);
        Log.i(TAG, "publishing latest stats data...");

        // save latest stats data to local database
        updateStats();
        db.addStatsEntry(stats);

        publishStats(db);
    }

    private void publishStats(final BonfireData db) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // publish stats object to the server API
                String postData = "timestamp=" + DateHelper.formatDateTime(stats.timestamp)
                        + "&batterylevel=" + stats.batteryLevel
                        + "&powerusage=" + stats.powerUsage
                        + "&messages_sent=" + stats.messagesSent
                        + "&messages_received=" + stats.messageReceived
                        + "&lat=" + stats.lat
                        + "&lng=" + stats.lng
                        + "&publickey=" + db.getDefaultIdentity().getPublicKey().asBase64();

                HttpURLConnection urlConnection = null;
                try {
                    urlConnection = (HttpURLConnection) new URL(BonfireAPI.API_ENDPOINT + "/stats").openConnection();
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    urlConnection.setDoOutput(true);
                    urlConnection.setChunkedStreamingMode(0);

                    BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    out.write(postData.getBytes("UTF-8"));
                    out.flush();

                    if (urlConnection.getResponseCode() != 200) {
                        Log.e(TAG, "error publishing stats: server sent response code " + urlConnection.getResponseCode());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(urlConnection == null) urlConnection.disconnect();
                }
                return null;
            }
        }.execute();
    }

    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            stats.batteryLevel = level;
            Log.d(TAG, "updating stats: setting battery level to " + level);
        }
    };

    private void updateStats() {
        // bump time
        stats.timestamp = new Date();

        // update calculated battery consumption
        stats.powerUsage = ((float)batteryLastLevel - (float)stats.batteryLevel) / (System.currentTimeMillis() - batteryLastMeasured) * 1000*60*60;
        if (stats.powerUsage < 0) stats.powerUsage = 0;
        batteryLastLevel = stats.batteryLevel;
        batteryLastMeasured = System.currentTimeMillis();

        // update location
        GpsTracker gps = GpsTracker.getInstance();
        if (gps.canGetLocation()) {
            stats.lat = (float) gps.getLatitude();
            stats.lng = (float) gps.getLongitude();
        }
    }
}
