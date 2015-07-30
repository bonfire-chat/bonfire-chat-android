package de.tudarmstadt.informatik.bp.bonfirechat.network;

import de.tudarmstadt.informatik.bp.bonfirechat.routing.Packet;

/**
 * Created by mw on 05.05.2015.
 */
public interface OnMessageReceivedListener {
    void onMessageReceived(IProtocol sender, Packet packet);
}
