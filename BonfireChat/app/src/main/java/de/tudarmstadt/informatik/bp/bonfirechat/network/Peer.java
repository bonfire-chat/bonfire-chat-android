package de.tudarmstadt.informatik.bp.bonfirechat.network;

import java.util.Arrays;

/**
 * Created by johannes on 30.07.15.
 *
 * a Peer represents a nearby device, identified by its Bluetooth MAC address
 */
public class Peer {

    // time delta to pass before flagging this peer as outdated
    private static long OUTDATED_TTL = 1000 * 60 * 10; // 10 minutes

    // Bluetooth MAC address
    private byte[] address;

    // timestamp of this peer beeing available for the last time
    private long lastSeen;

    public Peer(byte[] address) {
        this.address = address;
        this.lastSeen = System.currentTimeMillis();
    }

    public byte[] getAddress() {
        return address;
    }

    public void updateLastSeen() {
        lastSeen = System.currentTimeMillis();
    }

    public boolean isOutdated() {
        return lastSeen + OUTDATED_TTL < System.currentTimeMillis();
    }

    public boolean equals(Object other) {
        if (other instanceof byte[]) {
            return Arrays.equals(address, (byte[]) other);
        }
        else if (other instanceof Peer) {
            return Arrays.equals(address, ((Peer) other).address);
        }
        else {
            return false;
        }
    }
}
