package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Hashtable;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.data.ConstOptions;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;

/**
 * Created by jonas on 01.08.15.
 * The purpose of this class is to be given the sentButNotAckedPackages Queue of ConnectionManager
 * and initiating necessary retransmits
 */
public class Retransmission implements Runnable{

    private static final String TAG="Retransmission";
    private static final Hashtable<UUID, Retransmission> pendingRetransmissions = new Hashtable<>();

    private PayloadPacket packet;
    //timeout in milliseconds
    private int timeout;
    private Handler handler;
    private Context context;

    /**
     * Retransmit a given packet after a timeout, if not cancelled before
     * @param packet packet to retransmit
     * @param timeout timeout in milliseconds before packet is retransmit
     */
    private Retransmission(Context ctx, PayloadPacket packet, long timeout){
        this.packet = packet;
        this.context = ctx;
        handler = new Handler();
        Log.d(TAG, "Scheduling retransmission in "+timeout+" ms : "+packet.toString());
        handler.postDelayed(this, timeout);
    }

    public static void add(Context ctx, PayloadPacket packet, long timeout){
        if (pendingRetransmissions.containsKey(packet.uuid)) {
            Log.e(TAG, "RETR-TEST Packet "+packet.uuid+" already has one pending Retransmission, refused to schedule a second one!");
            return;
        }
        pendingRetransmissions.put(packet.uuid, new Retransmission(ctx, packet, timeout));
    }
    public static void cancel(UUID uuid) {
        Log.i(TAG, "Packet "+ uuid+" was acked, cancelling retransmission...");
        if (pendingRetransmissions.containsKey(uuid)) {
            pendingRetransmissions.get(uuid).cancel();
            pendingRetransmissions.remove(uuid);
        } else {
            Log.w(TAG, "RETR-TEST Cancelling retransmission failed  --- timing!");
        }
    }

    private void cancel() {
        handler.removeCallbacks(this);
    }

    public void run(){
        pendingRetransmissions.remove(this.packet.uuid);
        packet.incrementTransmissionCount();
        if (packet.getTransmissionCount() > ConstOptions.MAX_RETRANSMISSIONS) {
            Log.e(TAG, "Maximum retransmission count exceeded, ignoring message");

            final Intent localIntent = new Intent(ConnectionManager.MSG_SENT_BROADCAST_EVENT)
                    .putExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID, packet.uuid)
                    .putExtra(ConnectionManager.EXTENDED_DATA_ERROR, "max_retr_exceeded");

            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
            return;
        }
        if (!packet.isFlooding()) {
            packet.ttl = packet.getPath().size() + 1;
            packet.setFlooding();
        } else {
            // set ttl to DEFAULT_TTL, if it was smaller than DEFAULT_TTL
            // increment ttl by two but do not exceed MAX_TTL
            packet.ttl = Math.min(ConstOptions.MAX_TTL, Math.max(packet.ttl, ConstOptions.DEFAULT_TTL) + 2);
        }
        Log.i(TAG, "timed out, retransmitting "+packet.toString());
        ConnectionManager.sendPacket(context, packet);
    }

}
