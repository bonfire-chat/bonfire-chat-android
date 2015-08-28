package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireAPI;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;
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

    public static boolean isSupported(Context context) {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

    @Override
    public void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener) {
        super.setOnPeerDiscoveredListener(listener);

        listener.discoveredPeer(this, serverFakeMacAddress, "(SERVER)");
    }

    public void onHandleGcmIntent(Intent intent) {
        try {
            final String senderId = intent.getStringExtra("senderId");
            // Messages too long for GCM must be fetched from server
            if (intent.hasExtra("uuid")) {
                HttpURLConnection urlConnection = null;
                try {
                    urlConnection = (HttpURLConnection)
                            new URL(BonfireAPI.API_ENDPOINT + "/message?retrieve=" + intent.hasExtra("uuid"))
                            .openConnection();
                    onHandleMessage(urlConnection.getInputStream(), senderId);
                } finally {
                    if(urlConnection == null) urlConnection.disconnect();
                }
            } else {
                final String dataString = intent.getStringExtra("msg");
                Log.i("GcmProtocol", "onHandleGcmIntent: "+ dataString);
                onHandleMessage(new ByteArrayInputStream(Base64.decode(dataString, Base64.DEFAULT)), senderId);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("GcmProtocol", "Unable to deserialize: "+e.getMessage());
            e.printStackTrace();
        }
    }
    private void onHandleMessage(InputStream inputStream, String senderId) {
        try {
            final Packet packet = receive(inputStream);

            //TODO: HACK - this should better be done by the server
            if (packet.getNextHop() != null) packet.removeNextHop();
            packet.addPathNode(senderId.getBytes());
            //end todo

            packet.addPathNode(serverFakeMacAddress);
            packetListener.onPacketReceived(this, packet);
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

            BonfireAPI.sendGcmMessage(identity, packet.recipientPublicKey, nextHopId, packet.uuid.toString(), out.toByteArray());
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
