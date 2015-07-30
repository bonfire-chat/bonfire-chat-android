package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import java.util.UUID;

/**
 * Created by johannes on 29.07.15.
 */
public abstract class PayloadPacket extends Packet {
    // counts how many times this packet has been retransmitted. First transmissions use 1.
    private int transmissionCount;

    public PayloadPacket() {
        this(UUID.randomUUID());
    }
    public PayloadPacket(UUID uuid) {
        super(uuid);
        this.type = PacketType.Payload;
        this.transmissionCount = 1;
    }

    public boolean isRetransmission() {
        return transmissionCount > 1;
    }
    public void incrementTransmissionCount() {
        transmissionCount += 1;
    }
}
