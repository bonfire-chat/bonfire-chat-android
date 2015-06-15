package de.tudarmstadt.informatik.bp.bonfirechat.network;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by mw on 05.05.2015.
 */
public interface IProtocol {
    void sendMessage(Contact target, Message message);
    void setIdentity(Identity identity);
    void setOnMessageReceivedListener(OnMessageReceivedListener listener);
    boolean canSend();
}
