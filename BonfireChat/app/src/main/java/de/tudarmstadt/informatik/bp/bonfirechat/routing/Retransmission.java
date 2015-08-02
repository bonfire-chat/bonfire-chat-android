package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.RingBuffer;
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
        handler.postDelayed(this, timeout);
    }

    public static void add(Context ctx, PayloadPacket packet, long timeout){
        pendingRetransmissions.put(packet.uuid, new Retransmission(ctx, packet, timeout));
    }
    public static void cancel(UUID uuid) {
        if (pendingRetransmissions.containsKey(uuid)) {
            pendingRetransmissions.get(uuid).cancel();
            pendingRetransmissions.remove(uuid);
        }
    }

    private void cancel() {
        handler.removeCallbacks(this);
    }

    public void run(){
        packet.incrementTransmissionCount();
        packet.setFlooding();
        Log.i(TAG, "timed out, retransmitting "+packet.toString());
        ConnectionManager.sendPacket(context, packet);
    }

}
