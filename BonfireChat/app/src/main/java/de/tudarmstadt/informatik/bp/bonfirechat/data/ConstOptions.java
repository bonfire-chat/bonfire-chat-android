package de.tudarmstadt.informatik.bp.bonfirechat.data;

import java.util.Map;

import de.tudarmstadt.informatik.bp.bonfirechat.network.BluetoothProtocol;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;
import de.tudarmstadt.informatik.bp.bonfirechat.network.IProtocol;
import de.tudarmstadt.informatik.bp.bonfirechat.network.Peer;

/**
 * Created by mw on 03.08.15.
 */
public class ConstOptions {

    public static final boolean ALWAYS_SHOW_OOBE = false;
    public static final String APP_HOMEPAGE = "https://bonfire.projects.teamwiki.net";

    public static final int MAX_RETRANSMISSIONS = 5;
    public static final int RETRANSMISSION_TIMEOUT = 20 * 1000; // ms

    // maximum hops for a message until it will be discarded
    public static final int DEFAULT_TTL = 6;
    public static final int MAX_TTL = 14;

    public static final int LOCATION_BROADCAST_INTERVAL = 60 * 1000; // ms

    public static String getDebugInfo() {
        StringBuilder debug = new StringBuilder();
        for(IProtocol c : ConnectionManager.connections) {
            debug.append("\nProtocol: "+c.toString());
            if (c instanceof BluetoothProtocol) {
                BluetoothProtocol proto = (BluetoothProtocol)c;
                for(Map.Entry<String,BluetoothProtocol.ConnectionHandler> h : proto.getConnections()) {
                    debug.append("\n- Conn: "+h.getKey()+" = "+h.getValue().toString());
                }
            }
        }
        debug.append("\n");
        debug.append(ConnectionManager.routingManager.toString());
        debug.append("\n");
        for(Peer p : ConnectionManager.peers) {
            debug.append("\n"+p.toString());
        }
        return debug.toString();
    }

}
