package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johannes on 30.07.15.
 */
public class RoutingManager {
    public static List<byte[]> chooseRecipients(Packet packet, List<byte[]> peers) {
        if (packet.isFlooding()) {
            // send to all available peers
            return peers;
        } else {
            // just return next hop, regardless of whether we see it
            // avoids need for discovering peers.
            // in case of an exception the sender will initiate a retransmission
            List<byte[]> r = new ArrayList<>(1);
            r.add(packet.getNextHop());
            return r;
        }
    }
}
