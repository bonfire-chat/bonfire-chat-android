package de.tudarmstadt.informatik.bp.bonfirechat.network;

import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Packet;

/**
 * Created by mw on 05.05.2015.
 */
public interface IProtocol {
    void sendPacket(Packet packet, Peer peer);
    void setIdentity(Identity identity);
    void setOnPacketReceivedListener(OnPacketReceivedListener listener);
    void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener);
    boolean canSend();
    void shutdown();
}
