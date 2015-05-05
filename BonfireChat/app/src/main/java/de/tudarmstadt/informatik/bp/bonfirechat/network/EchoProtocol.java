package de.tudarmstadt.informatik.bp.bonfirechat.network;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by mw on 05.05.2015.
 */
public class EchoProtocol implements IProtocol {

    OnMessageReceivedListener listener;

    @Override
    public void sendMessage(String target, Message message) {
        Message newMsg = new Message(message.body, Message.MessageDirection.Received);
        listener.onMessageReceived(this, newMsg);
    }

    @Override
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }
}
