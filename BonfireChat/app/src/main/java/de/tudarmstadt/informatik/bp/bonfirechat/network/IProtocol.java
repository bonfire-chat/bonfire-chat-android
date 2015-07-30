package de.tudarmstadt.informatik.bp.bonfirechat.network;

import de.tudarmstadt.informatik.bp.bonfirechat.routing.Envelope;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;

/**
 * Created by mw on 05.05.2015.
 */
public interface IProtocol {
    void sendMessage(Envelope envelope);
    void setIdentity(Identity identity);
    void setOnMessageReceivedListener(OnMessageReceivedListener listener);
    void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener);
    boolean canSend();
}
