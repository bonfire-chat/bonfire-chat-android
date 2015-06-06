package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by Simon on 22.05.2015.
 */
public class WifiProtocol extends SocketProtocol {
WifiSenderActivity sender;
    Context ctx;

    public WifiProtocol(Context ctx){
        sender = new WifiSenderActivity(ctx);
        this.ctx = ctx;
    }

    @Override
    public void sendMessage(Contact target, Message message) {
        MessageQ.msg = message;
        sender.broadcastMsg(message, 10);


    }

    private void registerWifiReceiverSocket(){
        new AsyncTask(){

            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    /**
                     * Create a server socket and wait for client connections. This
                     * call blocks until a connection is accepted from a client
                     */
                    ServerSocket serverSocket = new ServerSocket(4242);
                    Socket client = serverSocket.accept();

                    InputStream inputstream = client.getInputStream();
                    WifiProtocol mySocketProtocol = new WifiProtocol(ctx);

                    Message m = mySocketProtocol.deserializeMessage(inputstream);
                    listener.onMessageReceived(WifiProtocol.this,m);

                } catch (IOException e) {
                }
                return null;
            }
        };


    }

}
