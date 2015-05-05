package de.tudarmstadt.informatik.bp.bonfirechat.network;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by mw on 05.05.2015.
 */
public interface IProtocol {
    void sendMessage(String target, Message message);

    void setOnMessageReceivedListener(OnMessageReceivedListener listener);
}
