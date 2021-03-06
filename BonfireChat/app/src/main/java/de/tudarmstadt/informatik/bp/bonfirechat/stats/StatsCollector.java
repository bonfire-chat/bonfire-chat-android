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
import java.util.Hashtable;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireAPI;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.network.Peer;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.AckPacket;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Packet;

/**
 * Created by johannes on 10.07.15.
 */
public class StatsCollector extends BroadcastReceiver {

    private static final String TAG = "StatsCollector";

    public static final long PUBLISH_INTERVAL = AlarmManager.INTERVAL_HALF_HOUR;
    private static final int RESPONSE_OK = 200;

    // action in Intents which are sent to the service
    public static final String PUBLISH_STATS_ACTION = "de.tudarmstadt.informatik.bp.bonfirechat.PUBLISH_STATS";

    private StatsEntry stats;
    private int batteryLastLevel;
    private long batteryLastMeasured;
    private boolean isCharging;

    private static String reporterIdentity;

    public StatsCollector(Context ctx) {
        // get global stats object
        stats = CurrentStats.getInstance();
        // initialize measurement
        batteryLastLevel = stats.batteryLevel;
        batteryLastMeasured = System.currentTimeMillis();
        // listen for battery status
        ctx.registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        reporterIdentity = BonfireData.getInstance(ctx).getDefaultIdentity().getNickname();

    }

    /*
     * handle stats intents
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final BonfireData db = BonfireData.getInstance(context);
        Log.i(TAG, "publishing latest stats data...");

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;

        // save latest stats data to local database
        updateStats();
        db.addStatsEntry(stats);

        publishStats();
    }

    public static void publishMessageHop(final Class protocol, final String what, final Peer peer,
                                         final Packet pkg, final String lastHop, final String thisHop) {
        final long dateTime = new Date().getTime();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Hashtable<String, byte[]> body = new Hashtable<>();
                String uid = pkg.uuid.toString();
                if (pkg instanceof AckPacket) {
                    uid = ((AckPacket) pkg).acknowledgesUUID.toString();
                }
                body.put("uuid", BonfireAPI.encode(uid));
                body.put("datetime", BonfireAPI.encode(String.valueOf(dateTime)));
                body.put("string", BonfireAPI.encode(pkg.toString()));
                body.put("action", BonfireAPI.encode(what));
                body.put("peer", BonfireAPI.encode(peer == null ? "" : ("to: " + peer.toString())));
                body.put("protocol", BonfireAPI.encode(protocol == null ? "" : protocol.getSimpleName()));
                body.put("hop1", BonfireAPI.encode(lastHop == null ? "" : lastHop));
                body.put("hop2", BonfireAPI.encode(thisHop == null ? "" : thisHop));
                body.put("reporter", BonfireAPI.encode(reporterIdentity));
                try {
                    BonfireAPI.httpPost("traceroute", body);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void publishError(final Class protocol, final String what, final UUID uuid, final String errMes) {
        final long dateTime = new Date().getTime();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Hashtable<String, byte[]> body = new Hashtable<>();
                String uid = uuid == null ? "" : uuid.toString();
                body.put("uuid", BonfireAPI.encode(uid));
                body.put("datetime", BonfireAPI.encode(String.valueOf(dateTime)));
                body.put("string", BonfireAPI.encode(errMes));
                body.put("action", BonfireAPI.encode(what));
                body.put("peer", new byte[0]);
                body.put("protocol", new byte[0]);
                body.put("hop1", new byte[0]);
                body.put("hop2", new byte[0]);
                body.put("reporter", BonfireAPI.encode(reporterIdentity));
                try {
                    BonfireAPI.httpPost("traceroute", body);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void publishStats() {
        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // publish stats object to the server API
                final String postData = "timestamp=" + DateHelper.formatDateTime(stats.timestamp)
                        + "&batterylevel=" + stats.batteryLevel
                        + "&powerusage=" + stats.powerUsage
                        + "&charging=" + isCharging
                        + "&messages_sent=" + stats.messagesSent
                        + "&messages_received=" + stats.messageReceived
                        + "&lat=" + stats.lat
                        + "&lng=" + stats.lng
                        + "&reporter=" + reporterIdentity;

                HttpURLConnection urlConnection = null;
                try {
                    urlConnection = (HttpURLConnection) new URL(BonfireAPI.API_ENDPOINT + "/stats").openConnection();
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    urlConnection.setDoOutput(true);
                    urlConnection.setChunkedStreamingMode(0);

                    try (final BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream())) {
                        out.write(postData.getBytes("UTF-8"));
                        out.flush();

                        if (urlConnection.getResponseCode() != RESPONSE_OK) {
                            Log.e(TAG, "error publishing stats: server sent response code " + urlConnection.getResponseCode());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                return null;
            }
        }).execute();
    }

    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            stats.batteryLevel = level;
            Log.d(TAG, "updating stats: setting battery level to " + level);
        }
    };


    private void updateStats() {
        // bump time
        stats.timestamp = new Date();

        if (!isCharging) {
            // update calculated battery consumption
            stats.powerUsage = ((float) batteryLastLevel - (float) stats.batteryLevel)
                    / (System.currentTimeMillis() - batteryLastMeasured) * 1000 * 60 * 60;
            if (stats.powerUsage < 0) {
                stats.powerUsage = 0;
            }
            batteryLastLevel = stats.batteryLevel;
            batteryLastMeasured = System.currentTimeMillis();
        }
/*
        // update location
        final GpsTracker gps = GpsTracker.getInstance();
        if (gps.canGetLocation()) {
            stats.lat = (float) gps.getLatitude();
            stats.lng = (float) gps.getLongitude();
        }*/
    }
}
