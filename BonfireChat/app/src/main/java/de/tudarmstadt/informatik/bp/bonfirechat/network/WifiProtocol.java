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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

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


        Looper mSrcLooper = ctx.getMainLooper();

        this.mWifiP2pManager= (WifiP2pManager) ctx.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel =  mWifiP2pManager.initialize(ctx, mSrcLooper, MCHLISTENER);


        mReceiver = new WifiReceiver(mWifiP2pManager,mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        ctx.registerReceiver(mReceiver, mIntentFilter);
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
                Log.d(TAG, "the Reason the discovering of peers failed with reason " + reason);
            }

        });
    }




    private void registerWifiReceiverSocket(){

        FutureTask futureTask = new FutureTask(new Callable(){


            @Override
            public Object call() throws Exception{
                Log.d(TAG, "do in Backround wird ausgef�hrt");
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
                    Log.d(TAG,"Die message war: " + m);
                    listener.onMessageReceived(WifiProtocol.this,m);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }); //todo wieviele Threats?
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(futureTask);


    }

}
