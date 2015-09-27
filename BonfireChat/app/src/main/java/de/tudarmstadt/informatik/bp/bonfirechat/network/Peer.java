package de.tudarmstadt.informatik.bp.bonfirechat.network;

import java.util.Arrays;

/**
 * Created by johannes on 30.07.15.
 *
 * a Peer represents a nearby device, identified by its Bluetooth MAC address
 */
public class Peer {

    // time delta to pass before flagging this peer as outdated
    private static final long OUTDATED_TTL = 1000 * 60 * 10; // 10 minutes

    private static final int MAC_ADDRESS_BYTES = 6;

    // Bluetooth MAC address
    private byte[] address;
    private Class protocol;

    // timestamp of this peer beeing available for the last time
    private long lastSeen;

    public String debugInfo;

    public Peer(Class protocol, byte[] address, String debugInfo) {
        this.address = address;
        this.lastSeen = System.currentTimeMillis();
        this.protocol = protocol;
        this.debugInfo = debugInfo;
    }

    public byte[] getAddress() {
        return address.clone();
    }

    public Class getProtocolClass() {
        return protocol;
    }

    public void updateLastSeen(Class newProtocol) {
        lastSeen = System.currentTimeMillis();
        this.protocol = newProtocol;
    }

    public boolean isOutdated() {
        //TODO implement some flag for peers that will never be outdated....
        if (protocol.getSimpleName().equals("GcmProtocol")) {
            return false;
        }

        return lastSeen + OUTDATED_TTL < System.currentTimeMillis();
    }

    public boolean hasAddress(byte[] checkForAddress) {
        return Arrays.equals(this.address, checkForAddress);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Peer)
            return hasAddress(((Peer)other).getAddress());
        else
            return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Peer(" + protocol.getSimpleName() + ", " + formatMacAddress(address) + ", " + debugInfo + ")";
    }

    public static String formatMacAddress(byte[] macAddress) {
        if (macAddress == null) {
            return null;
        }
        if (macAddress.length < MAC_ADDRESS_BYTES) {
            return "<invalid>";
        }
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
