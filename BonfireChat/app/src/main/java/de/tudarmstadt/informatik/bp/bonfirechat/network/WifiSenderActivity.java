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
import android.widget.TextView;

import java.util.Collection;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by Simon on 20.05.2015.
 * Class to setting up a Wifi -Ad Hoc Connection
 */
public class WifiSenderActivity extends Activity {

    //todo in etwas sinnvolles ändern:
    public static final String MBSSID ="1A:2B:3C:4D:5E:6F";
    //if a callback is needed in case of loss of framework communication this should be unnulled;
    public static final WifiP2pManager.ChannelListener MCHLISTENER =null;
    WifiP2pManager mWifiP2pManager;
    WifiP2pManager.Channel mChannel;
    IntentFilter mIntentFilter;
    BroadcastReceiver mReceiver;





    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Looper mSrcLooper = this.getMainLooper();

        this.mWifiP2pManager= (WifiP2pManager) this.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel =  mWifiP2pManager.initialize(this, mSrcLooper, MCHLISTENER);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);



    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver( mReceiver, mIntentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }



    /**
     * broadcasts a message to spread amount of recipients if available
     * @param msg
     * @param spread
     */
    public void broadcastMsg(Message msg, int spread){
       mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mWifiP2pManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        Collection<WifiP2pDevice> mDevList = peers.getDeviceList();
                        for(WifiP2pDevice dev : mDevList){
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = dev.deviceAddress;
                            config.groupOwnerIntent = 0;
                            config.wps.setup = WpsInfo.PBC;
                            mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onFailure(int reason) {

                                }
                            });
                        }
                    }
                });
            };

            @Override
            public void onFailure(int reason) {

            }

        });
    }


}
