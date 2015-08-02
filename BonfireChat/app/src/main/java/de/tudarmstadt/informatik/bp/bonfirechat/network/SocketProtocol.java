package de.tudarmstadt.informatik.bp.bonfirechat.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Packet;

/**
 * Created by johannes on 22.05.15.
 */
public abstract class SocketProtocol implements IProtocol {

    protected Identity identity;
    protected OnPacketReceivedListener packetListener;
    protected OnPeerDiscoveredListener peerListener;

    protected void send(OutputStream output, Packet packet) throws IOException {
        final ObjectOutputStream stream = new ObjectOutputStream(output);
        stream.writeObject(packet);
    }

    protected Packet receive (InputStream input) throws IOException {
        try {
            final ObjectInputStream stream = new ObjectInputStream(input);
            final Packet packet = (Packet) stream.readObject();
            return packet;
        } catch(ClassNotFoundException ex) {
            throw new IOException("Unable to deserialize packet, class not found ("+ex.getMessage() + ")");
        }
    }

    @Override
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    @Override
    public void setOnPacketReceivedListener(OnPacketReceivedListener listener) {
        this.packetListener = listener;
    }

    @Override
    public void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener) {
        this.peerListener = listener;
    }

}
