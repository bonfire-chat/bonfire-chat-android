package de.tudarmstadt.informatik.bp.bonfirechat.network;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by mw on 05.05.2015.
 */
public class EchoProtocol implements IProtocol {

    OnMessageReceivedListener listener;

    @Override
    public void sendMessage(Contact target, Message message) {
        Message newMsg = new Message(message.body, Message.MessageDirection.Received, message.dateTime);
        listener.onMessageReceived(this, newMsg);
    }

    @Override
    public void setIdentity(Identity identity) {

    }

    @Override
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean canSend() {
        return true;
    }
}
