package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

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

    Context ctx;
    //todo in etwas sinnvolles  aendern:
    public static final String MBSSID ="1A:2B:3C:4D:5E:6F";
    //if a callback is needed in case of loss of framework communication this should be unnulled;
    public static final WifiP2pManager.ChannelListener MCHLISTENER =null;
    WifiP2pManager mWifiP2pManager;
    WifiP2pManager.Channel mChannel;
    IntentFilter mIntentFilter;
    WifiReceiver mReceiver;
    public Message msg;
    public Contact contact;


    private static final String TAG = "WifiProtocol";


    public WifiProtocol(Context ctx){

        this.ctx = ctx;
        mReceiver = new WifiReceiver(mWifiP2pManager,mChannel, this);
        Log.d(TAG, "Dieser Code wird ausgefuehrt");
        Looper mSrcLooper = ctx.getMainLooper();

        this.mWifiP2pManager= (WifiP2pManager) ctx.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel =  mWifiP2pManager.initialize(ctx, mSrcLooper, MCHLISTENER);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerWifiReceiverSocket();
    }

    @Override
    public void sendMessage(Contact target, Message msg) {
        this.msg = msg;
        this.contact = target;

        Log.d(TAG, "Der mWifiManager ist " + mWifiP2pManager);
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Discovering of peers was successful!");

            }

            ;

            @Override
            public void onFailure(int reason) {
                System.out.println(reason);
            }

        });
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
