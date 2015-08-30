package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.data.ConstOptions;
import de.tudarmstadt.informatik.bp.bonfirechat.location.GpsTracker;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.LocationUdpPacket;

/**
 * Created by johannes on 31.08.15.
 */
public class SendLocationBroadcastsService implements Runnable {

    private static final String TAG = "SendLocationBroadcastsT"; // max. 23 characters

    private Context context;
    private BonfireData db;
    private Handler handler;

    public SendLocationBroadcastsService(Context context) {
        this.context = context;
        db = BonfireData.getInstance(context);
        handler = new Handler();

        // dispatch
        handler.post(this);
    }

    @Override
    public void run() {

        GpsTracker gps = GpsTracker.getInstance();

        // location available?
        if (gps.canGetLocation()) {
            // get all contacts to share location with
            ArrayList<Contact> contacts = db.getContactsToShareLocationWith();
            for (Contact contact: contacts) {
                // send location broadcast
                Log.i(TAG, "periodically sending location broadcast to " + contact.getNickname());
                LocationUdpPacket p = new LocationUdpPacket(db.getDefaultIdentity(), contact.getPublicKey().asByteArray(), gps.getLatitude(), gps.getLongitude());
                ConnectionManager.sendLocationUdpPacket(context, p);
            }
        }
        else {
            Log.i(TAG, "can't send location broadcast despite it's time to do so: no location available");
        }

        // reschedule
        handler.postDelayed(this, ConstOptions.LOCATION_BROADCAST_INTERVAL);
    }
}
