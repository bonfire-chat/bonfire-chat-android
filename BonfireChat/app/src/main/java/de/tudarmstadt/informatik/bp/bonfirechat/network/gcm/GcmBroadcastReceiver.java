package de.tudarmstadt.informatik.bp.bonfirechat.network.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;

/**
 * Created by mw on 23.05.15.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "GcmBroadcastReceiver";

    private static final String SENDER_ID = "1083776418239";
    public static String regid;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "received Gcm Broadcast : "+intent.getAction());
        Log.i(TAG, intent.getExtras().toString());
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                ConnectionManager.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }


    public static String registerForGcm(Activity activityContext) {
        Context context = activityContext.getApplicationContext();

        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices(activityContext)) {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(activityContext);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activityContext);

            regid = prefs.getString("gcmId", "");
            if (regid.isEmpty()) {
                registerInBackground(context);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        return regid;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private static void registerInBackground(final Context context) {
        new AsyncTask() {
            @Override
            protected String doInBackground(Object... params) {
                String msg = "";
                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();

                    prefs.putString("gcmId", regid);
                    prefs.apply();

                    BonfireData db = BonfireData.getInstance(context);
                    db.getDefaultIdentity().registerWithServer();

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(Object msg) {
                //mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private static boolean checkPlayServices(Activity activityContext) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activityContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activityContext,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }
}