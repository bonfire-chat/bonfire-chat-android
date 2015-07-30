package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import de.tudarmstadt.informatik.bp.bonfirechat.routing.Envelope;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Packet;

/**
 * Created by johannes on 22.05.15.
 */
public abstract class SocketProtocol implements IProtocol {

    protected Identity identity;
    protected OnMessageReceivedListener messageListener;
    protected OnPeerDiscoveredListener peerListener;

    protected void sendEnvelope(OutputStream output, Envelope envelope) {
        try {
            Log.d("SocketProtocol", "Sending envelope  uuid=" + envelope.uuid.toString() + "   from=" + envelope.senderNickname);
            final ObjectOutputStream stream = new ObjectOutputStream(output);
            stream.writeObject(envelope);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Envelope receiveEnvelope(InputStream input) throws IOException {
        try {
            final ObjectInputStream stream = new ObjectInputStream(input);
            final Envelope envelope = (Envelope) stream.readObject();
            return envelope;
        } catch(ClassNotFoundException ex) {
            throw new IOException("Unable to deserialize envelope, class not found ("+ex.getMessage() + ")");
        }
    }

    protected void sendPacket(OutputStream output, Packet packet) {
        try {
            final ObjectOutputStream stream = new ObjectOutputStream(output);
            stream.writeObject(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Packet receivePacket (InputStream input) throws IOException {
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
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.messageListener = listener;
    }

    @Override
    public void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener) {
        this.peerListener = listener;
    }

}
