package de.tudarmstadt.informatik.bp.bonfirechat.stats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.network.NonStopIntentService;

/**
 * Created by johannes on 10.07.15.
 */
public class StatsCollector extends BroadcastReceiver {

    private static final String TAG = "StatsCollector";

    // action in Intents which are sent to the service
    public static final String PUBLISH_STATS_ACTION = "de.tudarmstadt.informatik.bp.bonfirechat.PUBLISH_STATS";

    /*
     * handle stats intents
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        BonfireData db = BonfireData.getInstance(context);
        StatsEntry stats = CurrentStats.getInstance();

        // save latest stats data to local database
        db.addStatsEntry(stats);

        // publish stats object to the server API
        String postData = "timestamp=" + DateHelper.formatDateTime(stats.timestamp)
                + "&created_at=" + DateHelper.formatDateTime(stats.created_at)
                + "&batterylevel=" + stats.batteryLevel
                + "&powerusage=" + stats.powerUsage
                + "&messages_sent=" + stats.messagesSent
                + "&messages_received=" + stats.messageReceived
                + "&lat=" + stats.lat
                + "&lng=" + stats.lng;

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
    }
}
