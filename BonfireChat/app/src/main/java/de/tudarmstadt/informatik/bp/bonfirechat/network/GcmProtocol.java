package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireAPI;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Envelope;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Packet;

/**
 * Created by mw on 15.06.15.
 */
public class GcmProtocol extends SocketProtocol {
    private static final String TAG = "GcmProtocol";

    //FIXME TODO HACK
    public static GcmProtocol instance;

    public final byte[] serverFakeMacAddress;

    public GcmProtocol(Context context) {
        instance = this;
        this.serverFakeMacAddress = Peer.addressFromString("CA:FE:CA:FE:CA:FE");
    }


    @Override
    public void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener) {
        super.setOnPeerDiscoveredListener(listener);

        listener.discoveredPeer(this, serverFakeMacAddress, "(SERVER)");
    }

    public void onHandleGcmIntent(Intent intent) {
        try {
            final String dataString = intent.getStringExtra("msg");
            final String senderId = intent.getStringExtra("senderId");
            Log.i("GcmProtocol", "onHandleGcmIntent: "+ dataString);
            //byte[] data = dataString.getBytes("ascii");
            //Log.i("GcmProtocol", "onHandleGcmIntent: "+ StreamHelper.byteArrayToHexString(data));
            final ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(dataString, Base64.DEFAULT));
            final Packet packet = receive(bais);

            //TODO: HACK - this should better be done by the server
            if (packet.getNextHop() != null) packet.removeNextHop();
            packet.addPathNode(senderId.getBytes());
            //end todo

            packet.addPathNode(serverFakeMacAddress);
            packetListener.onPacketReceived(this, packet);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("GcmProtocol", "Unable to deserialize: "+e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendPacket(Packet packet, Peer peers) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            send(out, packet);

            //TODO this should better be done by the server
            String nextHopId = "";
            if (packet.getNextHop() != null) {
                nextHopId = new String(packet.getNextHop());
            }
            //end todo

            BonfireAPI.sendGcmMessage(identity, packet.recipientPublicKey, nextHopId, out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean canSend() {
        return true;
    }

    @Override
    public void shutdown() {
        // do nothing...
    }
}
