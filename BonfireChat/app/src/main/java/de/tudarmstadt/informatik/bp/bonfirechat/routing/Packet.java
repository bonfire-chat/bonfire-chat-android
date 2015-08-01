package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;

/**
 * Created by johannes on 29.07.15.
 *
 * packets describe basic pieces of information to be transmitted over the network. Subclassing
 * is needed to specify the particular type of packet, e.g. payload containing an actual message,
 * or a control sequence such as ACK.
 */
public abstract class Packet implements Serializable {
    // "sequence number"
    public final UUID uuid;

    // indicates type of this packet, for further casting
    protected PacketType type;

    // recipient of this packet
    public final byte[] recipientPublicKey;

    // previous and next hops
    private List<byte[]> path;
    private List<byte[]> nextHops;

    public int hopCount;

    public Packet() {
        this(new byte[0], UUID.randomUUID());
    }
    public Packet(byte[] recipientPublicKey, UUID uuid) {
        this(recipientPublicKey, uuid, new ArrayList<byte[]>());
    }
    public Packet(byte[] recipientPublicKey, List<byte[]> nextHops) {
        this(recipientPublicKey, UUID.randomUUID(), nextHops);
    }
    public Packet(byte[] recipientPublicKey, UUID uuid, List<byte[]> nextHops) {
        this.recipientPublicKey = recipientPublicKey;
        this.uuid = uuid;
        this.nextHops = nextHops;
        this.path = new ArrayList<>();
        this.hopCount = 0;
    }

    public PacketType getType() {
        return type;
    }

    public List<byte[]> getPath() {
        return path;
    }

    public List<byte[]> getNextHops() {
        return nextHops;
    }

    public byte[] getNextHop() {
        return nextHops.get(0);
    }

    public void removeNextHop() {
        nextHops.remove(0);
    }

    public void addPathNode(byte[] id) {
        path.add(id);
    }

    public boolean isFlooding() {
        return path.isEmpty();
    }

    public boolean hasRecipient(Identity id) {
        return (Arrays.equals(recipientPublicKey, id.publicKey.asByteArray()));
    }

}
