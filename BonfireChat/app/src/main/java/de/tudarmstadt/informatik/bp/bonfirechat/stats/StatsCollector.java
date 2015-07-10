package de.tudarmstadt.informatik.bp.bonfirechat.stats;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;
import de.tudarmstadt.informatik.bp.bonfirechat.network.NonStopIntentService;

/**
 * Created by johannes on 10.07.15.
 */
public class StatsCollector extends BroadcastReceiver {

    private static final String TAG = "StatsCollector";

    public static final long PUBLISH_INTERVAL = AlarmManager.INTERVAL_HALF_HOUR;

    // action in Intents which are sent to the service
    public static final String PUBLISH_STATS_ACTION = "de.tudarmstadt.informatik.bp.bonfirechat.PUBLISH_STATS";

    /*
     * handle stats intents
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        BonfireData db = BonfireData.getInstance(context);
        StatsEntry stats = CurrentStats.getInstance();

        Log.i(TAG, "publishing latest stats data...");

        // save latest stats data to local database
        stats.updateTimestamp();
        db.addStatsEntry(stats);

        publishStats(stats, db);
    }

    private void publishStats(final StatsEntry stats, final BonfireData db) {
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
                    urlConnection = (HttpURLConnection) new URL(BonfireData.API_ENDPOINT + "/stats").openConnection();
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
}
