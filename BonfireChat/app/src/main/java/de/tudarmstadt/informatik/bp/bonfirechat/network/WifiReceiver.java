package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


/**
 * Created by Simon on 22.05.2015.
 */
public class WifiReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pDevice connectedDevice;
    private WifiProtocol protocol;

    public static WifiP2pInfo info;


    public WifiReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiProtocol protocol) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.protocol = protocol;

    }

    WifiP2pManager.ConnectionInfoListener mConnectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            WifiReceiver.info = info;
        }
    };

    WifiP2pManager.PeerListListener mWifiPeerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            Collection<WifiP2pDevice> mDevList = peers.getDeviceList();
            Log.d(TAG, "the device List is: " + mDevList);
            for (WifiP2pDevice dev : mDevList) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = dev.deviceAddress;
                Log.d(TAG, "wifi device found " + config.deviceAddress);
                config.groupOwnerIntent = 0;
                config.wps.setup = WpsInfo.PBC;
                connectedDevice = dev;

                mManager.requestConnectionInfo(mChannel, mConnectionInfoListener);
                //String tmp = info==null ? "null" : info.toString();
                Log.d(TAG,"Info ist:");
                if(info == null || !info.groupFormed) {
                    mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "successfully connected with " + connectedDevice);
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d(TAG, "could not connect with " + connectedDevice + "with reason " + reason);
                        }
                    });
                }
            }


        }
    };


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive wird ausgef�hrt");
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
            } else {
                // User has to turn on WifiP2P
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            Log.d(TAG, "PeerschangedAction");
            if (mManager != null) {
                Log.d(TAG, "request peers has been done");
                mManager.requestPeers(mChannel, mWifiPeerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Daten werden GANZ AUSSEN gesendet");
            FutureTask futureTask = new FutureTask(new Callable() {
                @Override
                public Object call() throws Exception {
                    Log.d(TAG, "Daten werden au�en gesendet");
                    if (connectedDevice != null)

                    {
                        Log.d(TAG, "Daten werden gesendet");
                        //WifiP2pGroup group = mManager.createGroup(mChannel,null);

                        mManager.requestConnectionInfo(mChannel, mConnectionInfoListener);

                        String host = connectedDevice.deviceAddress;
                        int port = 4242;
                        int len;
                        Socket socket = new Socket();
                        //String msg = mWifiProtocol
                        byte buf[] = new byte[1024];

                        try {
                            /**
                             * Create a client socket with the host,
                             * port, and timeout information.
                             */
                            socket.bind(null);
                            socket.connect((new InetSocketAddress(info.groupOwnerAddress, port)), 500);

                            /**
                             * Create a byte stream from a JPEG file and pipe it to the output stream
                             * of the socket. This data will be retrieved by the server device.
                             */
                            OutputStream outputStream = socket.getOutputStream();

                            protocol.serializeMessage(outputStream, protocol.contact, protocol.msg);

                            outputStream.close();

                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        /**
                         * Clean up any open sockets when done
                         * transferring or if an exception occurred.
                         */ finally {
                            if (socket != null) {
                                if (socket.isConnected()) {
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    return null;
                }
            });
            //todo wieviele Threats?
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            executorService.execute(futureTask);

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))

        {
            // Respond to this device's wifi state changing
        }


    }

    private final String TAG = "WifiReceiver";

    public static String getIPFromMac(String MAC) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {

                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    // Basic sanity check
                    String device = splitted[5];
                    if (device.matches(".*p2p-p2p0.*")) {
                        String mac = splitted[3];
                        if (mac.matches(MAC)) {
                            return splitted[0];
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
