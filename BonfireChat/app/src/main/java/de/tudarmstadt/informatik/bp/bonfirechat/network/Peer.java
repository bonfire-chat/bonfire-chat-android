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
    private Class protocol;

    // timestamp of this peer beeing available for the last time
    private long lastSeen;

    public Peer(Class protocol, byte[] address) {
        this.address = address;
        this.lastSeen = System.currentTimeMillis();
        this.protocol = protocol;
    }

    public byte[] getAddress() {
        return address;
    }

    public Class getProtocolClass() {
        return protocol;
    }

    public void updateLastSeen(Class protocol) {
        lastSeen = System.currentTimeMillis();
        this.protocol = protocol;
    }

    public boolean isOutdated() {
        //TODO implement some flag for peers that will never be outdated....
        if (protocol.getSimpleName().equals("GcmProtocol")) return false;

        return lastSeen + OUTDATED_TTL < System.currentTimeMillis();
    }

    @Override
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

    @Override
    public String toString() {
        return "Peer(" + protocol.getSimpleName() + ", " + formatMacAddress(address) + ")";
    }

    public static String formatMacAddress(byte[] macAddress) {
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X",
                macAddress[0], macAddress[1], macAddress[2], macAddress[3], macAddress[4], macAddress[5]);
    }

    public static byte[] addressFromString(String formattedMacAddress) {
        String[] a = formattedMacAddress.split(":");
        return new byte[] {
                (byte) Integer.valueOf(a[0], 16).intValue(),
                (byte) Integer.valueOf(a[1], 16).intValue(),
                (byte) Integer.valueOf(a[2], 16).intValue(),
                (byte) Integer.valueOf(a[3], 16).intValue(),
                (byte) Integer.valueOf(a[4], 16).intValue(),
                (byte) Integer.valueOf(a[5], 16).intValue()
        };
    }
}
