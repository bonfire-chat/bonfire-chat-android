package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.routing.Packet;

/**
 * Created by johannes on 22.05.15.
 */
public class BluetoothProtocol extends SocketProtocol {

    private static final String TAG = "BluetoothProtocol";
    private static final UUID BTMODULEUUID = UUID.fromString("D5AD0434-34AA-4B5C-B100-4964BFE3E739");

    private Context ctx;
    private BluetoothAdapter adapter;
    private Handler searchLoopHandler;

    private Hashtable<String, ConnectionHandler> connections = new Hashtable<>();

    BluetoothProtocol(Context ctx) {
        this.ctx = ctx;
        searchLoopHandler = new Handler();

        adapter = BluetoothAdapter.getDefaultAdapter();
        ensureBluetoothUp();
    }

    private boolean ensureBluetoothUp() {
        if (adapter == null) {
            Log.d(TAG, "device does not support Bluetooth");
        } else {
            Log.d(TAG, "enabling bluetooth and requesting infinite discoverability");
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(discoverableIntent);
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initializeBluetooth();
                }
            }, 7500);
        }
        return false;
    }

    private void initializeBluetooth() {
        ctx.registerReceiver(onDeviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        searchDevicesThread.run();
        listeningThread.start();
    }


    public BroadcastReceiver onDeviceFoundReceiver = new BroadcastReceiver() {
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                peerListener.discoveredPeer(BluetoothProtocol.this, Peer.addressFromString(device.getAddress()));
                Log.d(TAG, "device found: " + device.getName() + " - " + device.getAddress());
            }
        }
    };

    private Runnable searchDevicesThread = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "starting next discovery interval");
            searchDevices();
            searchLoopHandler.postDelayed(searchDevicesThread, 120000);
        }
    };

    public void searchDevices() {
        if (adapter.isDiscovering()) {
            Log.d(TAG, "stopping discovery");
            adapter.cancelDiscovery();
        }
        Log.d(TAG, "starting discovery");
        adapter.startDiscovery();
    }

    public Thread listeningThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "running listener thread");
            try {

                BluetoothServerSocket server = adapter.listenUsingInsecureRfcommWithServiceRecord("bonfire", BTMODULEUUID);
                while(true) {
                    BluetoothSocket socket = server.accept();
                    ConnectionHandler handler = new ConnectionHandler(socket);
                    connections.put(socket.getRemoteDevice().getAddress(), handler);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    public class ConnectionHandler extends Thread {
        BluetoothSocket socket;
        InputStream input;
        OutputStream output;
        byte[] peerMacAddress;

        public ConnectionHandler(BluetoothSocket socket) {
            this.socket = socket;
            peerMacAddress = Peer.addressFromString(socket.getRemoteDevice().getAddress());
            try {
                input = socket.getInputStream();
                output = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.start();
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "Client connected: " + socket.getRemoteDevice().getAddress());
                while(true) {
                    Packet packet = receive(input);
                    Log.d(TAG, "Recieved packet with uuid " + packet.uuid);
                    packet.addPathNode(peerMacAddress);
                    // hand over to the onMessageReceivedListener, which will take account for displaying
                    // the message and/or redistribute it to further recipients
                    packetListener.onPacketReceived(BluetoothProtocol.this, packet);
                }
            } catch (IOException e) {
                // do nothing, this exception will occur a lot because there will be Bluetooth
                // devices nearby that do not run BonfireChat, resulting in no suitable
                // ServerSocket to accept this connection
                e.printStackTrace();
                teardown();
            }
        }
        public void sendNetworkPacket(final Packet packet) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "sendNetworkPacket to "+socket.getRemoteDevice().getAddress()+" | "+packet.toString());
                    synchronized (ConnectionHandler.this.output) {
                        try {
                            send(ConnectionHandler.this.output, packet);
                        } catch(IOException ex) {
                            Log.w(TAG, "Could not send to "+socket.getRemoteDevice().getAddress()+" : "+packet.toString());
                            Log.w(TAG, ex.getMessage());
                            // Connection is broken, remove from list
                            teardown();
                        }
                    }
                }
            }).start();
        }
        private void teardown() {
            connections.remove(this);
            try {
                socket.close();
            } catch (IOException e) {/*ignore*/}
        }
    }

    ConnectionHandler connectToDevice(BluetoothDevice device) {
        try {
            if (connections.containsKey(device.getAddress()))
                return connections.get(device.getAddress());

            BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
            socket.connect();
            ConnectionHandler handler = new ConnectionHandler(socket);
            connections.put(device.getAddress(), handler);
            return handler;
        } catch (IOException e) {
            Log.e(TAG, "Unable to connect to bluetooth device "+device.getAddress()+", ignoring");
            e.printStackTrace();
            return null;
        }
    }

    // ###########################################################################
    // ###    Implementation of IProtocol
    // ###########################################################################

    @Override
    public void sendPacket(Packet packet, Peer peer) {
        Log.d(TAG, "sending packet to peers via Bluetooth");
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }

        // send packet only to specified peer. Just try sending it to
        // the addresses, no discovering necessary to send the packet
        ConnectionHandler socket = connectToDevice(adapter.getRemoteDevice(peer.getAddress()));
        if (socket != null) {
            socket.sendNetworkPacket(packet);
        }

        if (!adapter.isDiscovering()) {
            adapter.startDiscovery();
        }
    }

    @Override
    public boolean canSend() {
        return true;
    }

    @Override
    public void shutdown() {
        // stop discovering
        searchLoopHandler.removeCallbacksAndMessages(null);
        if (adapter.isDiscovering()) adapter.cancelDiscovery();

        // close all connections
        for(ConnectionHandler c : connections.values()) {
            c.teardown();
        }
    }
}
