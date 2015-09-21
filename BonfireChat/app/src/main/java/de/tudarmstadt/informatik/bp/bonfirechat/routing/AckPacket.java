package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import java.util.UUID;

/**
 * Created by johannes on 29.07.15.
 */
public class AckPacket extends Packet {
    public UUID acknowledgesUUID;

    public AckPacket(UUID ackUUID, byte[] senderPublicKey, byte[] recipientPublicKey) {
        super(senderPublicKey, recipientPublicKey, UUID.randomUUID());
        this.type = PacketType.Ack;
        this.acknowledgesUUID = ackUUID;
    }

    @Override
    public String toString() {
        return super.toString() + ":ACK(for=" + acknowledgesUUID.toString() + ")";
    }

}
