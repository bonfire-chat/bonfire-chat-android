package de.tudarmstadt.informatik.bp.bonfirechat.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.routing.Envelope;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Packet;

/**
 * Created by mw on 05.05.2015.
 */
public class EchoProtocol implements IProtocol {

    OnPacketReceivedListener listener;

    @Override
    public void sendPacket(Packet packet, List<Peer> peers) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            final ObjectInputStream ois = new ObjectInputStream(bais);
            final Envelope myNewEnvelope = (Envelope) ois.readObject();
            listener.onPacketReceived(this, myNewEnvelope);
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
    }

    @Override
    public void setIdentity(Identity identity) {

    }

    @Override
    public void setOnPacketReceivedListener(OnPacketReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener) { }

    @Override
    public boolean canSend() {
        return true;
    }
}
