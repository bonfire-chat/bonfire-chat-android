package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.network.BluetoothProtocol;
import de.tudarmstadt.informatik.bp.bonfirechat.network.Peer;

/**
 * Created by johannes on 30.07.15.
 */
public class RoutingManager {

    private Hashtable<String, List<byte[]>> shortestPaths = new Hashtable<>();

    public void registerPath(Packet packet) {
        String key = CryptoHelper.toBase64(packet.senderPublicKey);
        String pathDebug = "";
        for(byte[] a : packet.getPath()) pathDebug += " -> " + Peer.formatMacAddress(a);
        Log.i("RoutingManager", "registerPath to " + key + " " + pathDebug);
        shortestPaths.put(key, packet.getPath());
    }

    public List<byte[]> getPath(Packet packet) {
        if (shortestPaths.containsKey(CryptoHelper.toBase64(packet.recipientPublicKey))) {
            return shortestPaths.get(CryptoHelper.toBase64(packet.recipientPublicKey));
        } else {
            return null;
        }
    }

    public List<Peer> chooseRecipients(Packet packet, List<Peer> peers) {
        synchronized (peers) {
            if (packet.isFlooding()) {
                // send to all available peers
                return (ArrayList) ((ArrayList) peers).clone();
            } else if (packet.getNextHop() != null) {
                List<Peer> r = new ArrayList<>(1);
                for (Peer peer : peers) {
                    if (peer.equals(packet.getNextHop())) {
                        r.add(peer);
                    }
                }
                // if no matching peer was discovered, just be lazy and try to send it via bluetooth
                if (r.size() == 0)
                    r.add(new Peer(BluetoothProtocol.class, packet.getNextHop()));
                packet.removeNextHop();
                return r;
            } else {
                return null;
            }
        }
    }
}
