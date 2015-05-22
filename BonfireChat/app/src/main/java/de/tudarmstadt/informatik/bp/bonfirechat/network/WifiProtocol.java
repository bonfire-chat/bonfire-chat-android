package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by Simon on 22.05.2015.
 */
public class WifiProtocol extends SocketProtocol {
WifiSenderActivity sender;

    public WifiProtocol(Context ctx){
        sender = new WifiSenderActivity(ctx);
    }

    @Override
    public void sendMessage(Contact target, Message message) {
        MessageQ.msg = message;
        sender.broadcastMsg(message, 10);


    }
}
