package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import java.io.Serializable;
import java.util.List;

/**
 * Created by johannes on 29.07.15.
 *
 * packets describe basic pieces of information to be transmitted over the network. Subclassing
 * is needed to specify the particular type of packet, e.g. payload containing an actual message,
 * or a control sequence such as ACK.
 */
public abstract class Packet implements Serializable {
    public List<byte[]> getPath() {
        return path;
    }

    public List<byte[]> getNextHops() {
        return nextHops;
    }

    List<byte[]> path;
    List<byte[]> nextHops;

    private boolean flooding() {
        return path.isEmpty();
    }
}
