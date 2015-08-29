package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;

import de.tudarmstadt.informatik.bp.bonfirechat.routing.Packet;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.TracerouteHopSegment;

/**
 * Created by mw on 16.08.15.
 */
public class WifiProtocol extends SocketProtocol {

    public static final String TAG = "WifiProtocol";
    private Context context;

    private final WifiP2pManager manager;
    private final WifiP2pManager.Channel channel;

    private byte[] myMacAddress;

    private DatagramSocket socket;

    private final ArrayList<Packet> packetsToSend = new ArrayList<Packet>();

    public WifiProtocol(Context context) {
        this.context = context;
        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);

        channel = manager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.w(TAG, "onChannelDisconnected");
            }
        });

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);       //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);       // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);  // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION); // Indicates this device's details have changed.
        context.registerReceiver(this.wifiBroadcastReceiver, intentFilter);

        try {
            socket = new DatagramSocket(9876);
            new Thread(listeningThread).start();
        } catch (SocketException e) {
            Log.e(TAG, "Unable to create DatagramSocket: " + e.getMessage());
        }

    }

    @Override
    public void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener) {
        super.setOnPeerDiscoveredListener(listener);

        // Start peer discovery
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                updatePeers();
            }

            @Override
            public void onFailure(int reason) {
                Log.w(TAG, "peer discovery failed: " + reason);
            }
        });
    }

    private void updatePeers() {
        manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                for (WifiP2pDevice device : peers.getDeviceList()) {
                    Log.d(TAG, "Wifi found device: " + device.deviceAddress);
                    peerListener.discoveredPeer(WifiProtocol.this, Peer.addressFromString(device.deviceAddress), device.deviceName);
                }
            }
        });
    }

    private final Runnable listeningThread = new Runnable() {
        @Override
        public void run() {
            try {
                DatagramPacket pack = new DatagramPacket(new byte[2048], 2048);

                while (!socket.isClosed()) {
                    socket.receive(pack);
                    Log.i(TAG, "UDP Packet received");
                    try {
                        ByteArrayInputStream stream = new ByteArrayInputStream(pack.getData(), pack.getOffset(), pack.getLength());
                        ObjectInputStream obj = new ObjectInputStream(stream);
                        byte[] macAddress = (byte[]) obj.readObject();
                        Packet packet = (Packet) obj.readObject();
                        packet.addPathNode(macAddress);
                        packet.addTracerouteSegment(new TracerouteHopSegment(TracerouteHopSegment.ProtocolType.WIFI, packet.getLastHopTimeSent(), new Date()));
                        obj.close();
                        packetListener.onPacketReceived(WifiProtocol.this, packet);
                    } catch (ClassNotFoundException cnfe) {
                        Log.e(TAG, "ClassNotFoundException while parsing datagram: " + cnfe.getMessage());
                    }
                }
                Log.w(TAG, "STOPPING WIFI RECV :: closed");
            } catch (IOException ex) {
                Log.e(TAG, "STOPPING WIFI RECV :: IOException in DatagramSocket listeningThread: " + ex.getMessage());
            }
        }
    };


    private void sendPacketViaUDP(final Packet packet) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG, "Broadcasting via UDP: "+packet.toString());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                try {
                    ObjectOutputStream obj = new ObjectOutputStream(stream);
                    obj.writeObject(myMacAddress);
                    obj.writeObject(packet);
                    byte[] buffer=stream.toByteArray();
                    DatagramPacket udpPacket = new DatagramPacket(buffer, buffer.length,
                            InetAddress.getByName("192.168.49.255"), 9876);
                    socket.send(udpPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private final BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    //activity.setIsWifiP2pEnabled(true);
                } else {
                    //activity.setIsWifiP2pEnabled(false);
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

                // The peer list has changed!  We should probably do something about
                // that.

                //updatePeers();

                WifiP2pDeviceList list = (WifiP2pDeviceList) intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
                for (WifiP2pDevice device : list.getDeviceList()) {
                    Log.d(TAG, "Wifi found device: " + device.status + "|" +device.deviceName+"|"+ device.deviceAddress);
                    if (device.status == WifiP2pDevice.AVAILABLE) {
                        //connectToDevice(device);
                    }
                    peerListener.discoveredPeer(WifiProtocol.this, Peer.addressFromString(device.deviceAddress), device.deviceName);
                }

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                Log.i(TAG, "Wifi p2p connection changed");
                if (manager == null) {
                    return;
                }

                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);


                synchronized (packetsToSend) {
                    for(Packet p : packetsToSend)
                        sendPacketViaUDP(p);
                    packetsToSend.clear();
                }

                if (networkInfo.isConnected()) {
                    Log.i(TAG, "network is connected: ");
                    // We are connected with the other device, request connection
                    // info to find group owner IP

                    manager.requestConnectionInfo(channel, connectionInfoListener);
                }


            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                WifiP2pDevice thisDevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                Log.i(TAG, "My wifi device info: " + thisDevice.deviceAddress + " | " + thisDevice.deviceName);
                myMacAddress = Peer.addressFromString(thisDevice.deviceAddress);

            }
        }
    };

    private void connectToDevice(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        Log.i(TAG, "Connecting to " + config.deviceAddress);

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Log.w(TAG, "peer connect failed: " + reason);
                Toast.makeText(context, "Connect failed. Retry." + reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
            Log.i(TAG, "connection info available");
            // InetAddress from WifiP2pInfo struct.
            String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
            Log.i(TAG, "groupFormed="+info.groupFormed);
            Log.i(TAG, "group owner="+groupOwnerAddress);
            Log.i(TAG, "isGroupOwner="+ info.isGroupOwner);

            // After the group negotiation, we can determine the group owner.
            if (info.groupFormed && info.isGroupOwner) {
                // Do whatever tasks are specific to the group owner.
                // One common case is creating a server thread and accepting
                // incoming connections.
            } else if (info.groupFormed) {
                // The other device acts as the client. In this case,
                // you'll want to create a client thread that connects to the group
                // owner.
            }
        }
    };

    @Override
    public void sendPacket(final Packet packet, Peer peer) {

        packetsToSend.add(packet);
        sendPacketViaUDP(packet);

    }

    @Override
    public boolean canSend() {
        return true;
    }

    @Override
    public void shutdown() {

    }


    @Override
    public String toString() {
        return "WifiProtocol=" + Peer.formatMacAddress(myMacAddress);
    }
}
