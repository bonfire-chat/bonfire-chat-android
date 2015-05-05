package de.wikilab.bonfirechat.comm;

import de.wikilab.bonfirechat.Message;

/**
 * Created by mw on 05.05.2015.
 */
public interface OnMessageReceivedListener {
    void onMessageReceived(IProtocol sender, Message message);
}
