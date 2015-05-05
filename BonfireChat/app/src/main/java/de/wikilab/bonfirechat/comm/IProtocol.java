package de.wikilab.bonfirechat.comm;

import de.wikilab.bonfirechat.Message;

/**
 * Created by mw on 05.05.2015.
 */
public interface IProtocol {
    void sendMessage(String target, Message message);

    void setOnMessageReceivedListener(OnMessageReceivedListener listener);
}
