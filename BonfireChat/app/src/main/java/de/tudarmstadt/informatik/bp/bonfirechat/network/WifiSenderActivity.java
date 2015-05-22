package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import java.util.Collection;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by Simon on 20.05.2015.
 * Class to setting up a Wifi -Ad Hoc Connection
 */
public class WifiSenderActivity  {

    //todo in etwas sinnvolles  aendern:
    public static final String MBSSID ="1A:2B:3C:4D:5E:6F";
    //if a callback is needed in case of loss of framework communication this should be unnulled;
    public static final WifiP2pManager.ChannelListener MCHLISTENER =null;
    WifiP2pManager mWifiP2pManager;
    WifiP2pManager.Channel mChannel;
    IntentFilter mIntentFilter;
    WifiReceiver mReceiver;


 public WifiSenderActivity(Context ctx){

        Log.d(TAG, "Dieser Code wird ausgefuehrt");
        Looper mSrcLooper = ctx.getMainLooper();

        this.mWifiP2pManager= (WifiP2pManager) ctx.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel =  mWifiP2pManager.initialize(ctx, mSrcLooper, MCHLISTENER);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }


private static final String TAG = "WifiSenderActivity";

    /**
     * broadcasts a message to spread amount of recipients if available
     * @param msg
     * @param spread
     */
    public void broadcastMsg(Message msg, final int spread){
        Log.d(TAG, "Der mWifiManager ist " + mWifiP2pManager);
       mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            Log.d(TAG, "Discovering of peers was successful!");

            };

            @Override
            public void onFailure(int reason) {
                System.out.println(reason);
            }

        });
    }


}
