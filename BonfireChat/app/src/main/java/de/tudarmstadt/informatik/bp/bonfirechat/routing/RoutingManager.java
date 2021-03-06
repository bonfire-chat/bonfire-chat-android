package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.network.Peer;

/**
 * Created by johannes on 30.07.15.
 */
public class RoutingManager {

    private static final String TAG = "RoutingManager";
    private Hashtable<String, List<byte[]>> shortestPaths = new Hashtable<>();

    public void registerPath(Packet packet) {
        String key = CryptoHelper.toBase64(packet.senderPublicKey);
        StringBuilder pathDebugBuilder = new StringBuilder();
        for (byte[] a : packet.getPath()) {
            pathDebugBuilder.append(" -> ").append(Peer.formatMacAddress(a));
        }
        String pathDebug = pathDebugBuilder.toString();
        Log.i("routingManager", "registerPath to " + key + " " + pathDebug);
        shortestPaths.put(key, packet.getPath());
    }

    public void clearPath(byte[] toPublicKey) {
        String key = CryptoHelper.toBase64(toPublicKey);
        shortestPaths.remove(key);
    }

    /**
     * For debugging: override path to publicKey with direct path to mac
     */
    public void overridePath(byte[] toPublicKey, byte[] targetMac) {
        String key = CryptoHelper.toBase64(toPublicKey);
        List<byte[]> path = new ArrayList<>();
        path.add(targetMac);
        shortestPaths.put(key, path);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("routingManager:\n");
        for (Map.Entry<String, List<byte[]>> e : shortestPaths.entrySet()) {
            s.append("- Path to " + e.getKey() + " ");
            for (byte[] a : e.getValue()) {
                s.append(" -> " + Peer.formatMacAddress(a));
            }
            s.append("\n");
        }
        return s.toString();
    }

    public List<byte[]> getPath(Packet packet) {
        if (shortestPaths.containsKey(CryptoHelper.toBase64(packet.recipientPublicKey))) {
            return shortestPaths.get(CryptoHelper.toBase64(packet.recipientPublicKey));
        } else {
            return null;
        }
    }

    public List<Peer> chooseRecipients(Packet packet, List<Peer> peers) {
        if (packet.isFlooding()) {
            // send to all available peers
            return peers;
        } else if (packet.getNextHop() != null) {
            List<Peer> r = new ArrayList<>(1);
            for (Peer peer : peers) {
                if (peer.hasAddress(packet.getNextHop())) {
                    r.add(peer);
                }
            }
            // if no matching peer was discovered, just be lazy and try to send it via bluetooth
            if (r.size() == 0) {
                ///r.add(new Peer(BluetoothProtocol.class, packet.getNextHop(), "(dynamic)"));
                //throw new IllegalStateException("Next hop not found in neighbour list: "+ StreamHelper.byteArrayToHexString(packet.getNextHop()));
                Log.w(TAG, "Next hop not found in neighbour list: " + StreamHelper.byteArrayToHexString(packet.getNextHop()));
                return null;
            }
            packet.removeNextHop();
            return r;
        } else {
            return null;
        }
    }
}
