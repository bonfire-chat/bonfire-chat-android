package de.tudarmstadt.informatik.bp.bonfirechat.network;

/**
 * Created by johannes on 30.07.15.
 */
public interface OnPeerDiscoveredListener {
    void discoveredPeer(byte[] address);
}
