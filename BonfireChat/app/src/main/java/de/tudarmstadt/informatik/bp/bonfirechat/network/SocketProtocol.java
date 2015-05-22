package de.tudarmstadt.informatik.bp.bonfirechat.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by johannes on 22.05.15.
 */
public abstract class SocketProtocol implements IProtocol {

    protected Identity identity;
    protected OnMessageReceivedListener listener;

    protected void serializeMessage(OutputStream output, Contact target, Message message) {
        try {
            ObjectOutputStream stream = new ObjectOutputStream(output);
            stream.writeObject(target);
            stream.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Message deserializeMessage(InputStream input) {
        try {
            ObjectInputStream stream = new ObjectInputStream(input);
            Contact target = (Contact) stream.readObject();
            Message message = (Message) stream.readObject();
            return message;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    @Override
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }

}
