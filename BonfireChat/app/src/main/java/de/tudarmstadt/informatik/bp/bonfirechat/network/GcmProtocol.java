package de.tudarmstadt.informatik.bp.bonfirechat.network;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Envelope;

/**
 * Created by mw on 15.06.15.
 */
public class GcmProtocol extends SocketProtocol {

    @Override
    public void sendMessage(Envelope envelope) {

    }

    @Override
    public boolean canSend() {
        return false;
    }
}
