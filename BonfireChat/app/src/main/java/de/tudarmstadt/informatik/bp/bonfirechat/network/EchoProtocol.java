package de.tudarmstadt.informatik.bp.bonfirechat.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.tudarmstadt.informatik.bp.bonfirechat.routing.Envelope;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;

/**
 * Created by mw on 05.05.2015.
 */
public class EchoProtocol implements IProtocol {

    OnMessageReceivedListener listener;

    @Override
    public void sendMessage(Envelope envelope) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            final ObjectInputStream ois = new ObjectInputStream(bais);
            final Envelope myNewEnvelope = (Envelope) ois.readObject();
            listener.onMessageReceived(this, myNewEnvelope);
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
    }

    @Override
    public void setIdentity(Identity identity) {

    }

    @Override
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener) { }

    @Override
    public boolean canSend() {
        return true;
    }
}
