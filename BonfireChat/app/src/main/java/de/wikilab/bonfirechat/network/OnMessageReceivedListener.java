package de.wikilab.bonfirechat.network;

import de.wikilab.bonfirechat.models.Message;

/**
 * Created by mw on 05.05.2015.
 */
public interface OnMessageReceivedListener {
    void onMessageReceived(IProtocol sender, Message message);
}
