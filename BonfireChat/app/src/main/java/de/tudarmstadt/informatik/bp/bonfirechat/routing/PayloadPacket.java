package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import android.util.Log;

import java.util.UUID;

/**
 * Created by johannes on 29.07.15.
 */
public abstract class PayloadPacket extends Packet {
    // counts how many times this packet has been retransmitted. First transmissions use 1.
    private int transmissionCount;

    public PayloadPacket(byte[] recipientPublicKey, UUID uuid) {
        super(recipientPublicKey, uuid);
        this.type = PacketType.Payload;
        this.transmissionCount = 1;
    }

    public int getTransmissionCount(){
        return transmissionCount;
    }

    @Override
    public boolean equals(Object otherObject){
        if (otherObject == null) return false;
        if (otherObject instanceof PayloadPacket) {
            PayloadPacket packet = (PayloadPacket) otherObject;
            if(super.equals(packet) && transmissionCount == packet.getTransmissionCount())
                return true;
            else
                return false;
        } else if (otherObject instanceof Packet) {
            return super.equals(otherObject);
        } else {
            return false;
        }
    }

    public boolean isRetransmission() {
        return transmissionCount > 1;
    }
    public void incrementTransmissionCount() {
        transmissionCount += 1;
    }


    @Override
    public String toString() {
        return super.toString() + ":Payload(retr=" + String.valueOf(transmissionCount) + ")";
    }

}
