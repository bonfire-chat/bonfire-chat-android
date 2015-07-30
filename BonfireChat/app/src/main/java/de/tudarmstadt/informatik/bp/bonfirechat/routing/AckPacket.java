package de.tudarmstadt.informatik.bp.bonfirechat.routing;

/**
 * Created by johannes on 29.07.15.
 */
public class AckPacket extends Packet {
    public AckPacket() {
        super();
        this.type = PacketType.Ack;
    }
}
