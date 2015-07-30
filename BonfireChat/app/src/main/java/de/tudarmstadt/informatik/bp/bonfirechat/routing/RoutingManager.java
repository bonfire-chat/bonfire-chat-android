package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.network.Peer;

/**
 * Created by johannes on 30.07.15.
 */
public class RoutingManager {

    public List<Peer> chooseRecipients(Packet packet, List<Peer> peers) {
        if (packet.isFlooding()) {
            // send to all available peers
            return peers;
        } else {
            // just return next hop, regardless of whether we see it
            // avoids need for discovering peers.
            // in case of an exception the sender will initiate a retransmission
            List<Peer> r = new ArrayList<>(1);
            r.add(new Peer(packet.getNextHop()));
            return r;
        }
    }
}
