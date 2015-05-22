package de.tudarmstadt.informatik.bp.bonfirechat.network;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by johannes on 22.05.15.
 */
public abstract class SocketProtocol implements IProtocol {

    protected Identity identity;
    protected OnMessageReceivedListener listener;

    protected String serializeMessage(Contact target, Message message) {
        return identity.nickname + ":" + message.body;
    }

    protected Message deserializeMessage(String buffer) {
        return new Message(buffer, new Contact("foo"), Message.MessageDirection.Received, DateHelper.getNowString());
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
