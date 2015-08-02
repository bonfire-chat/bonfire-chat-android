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

import de.tudarmstadt.informatik.bp.bonfirechat.models.Envelope;

/**
 * Created by johannes on 22.05.15.
 */
public class BluetoothProtocol extends SocketProtocol {

    private static final String TAG = "BluetoothProtocol";
    private static final UUID BTMODULEUUID = UUID.fromString("D5AD0434-34AA-4B5C-B100-4964BFE3E739");

    private Context ctx;
    private BluetoothAdapter adapter;
    private List<BluetoothDevice> nearby;
    private List<BluetoothSocket> sockets;
    private Handler searchLoopHandler;

    private Hashtable<String, ConnectionHandler> connections = new Hashtable<>();

    BluetoothProtocol(Context ctx) {
        this.ctx = ctx;
        nearby = new ArrayList<>();
        sockets = new ArrayList<>();
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
                nearby.add(device);
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
        nearby.clear();
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

        public ConnectionHandler(BluetoothSocket socket) {
            this.socket = socket;
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
                while(true) {
                    Log.d(TAG, "Client connected: " + socket.getRemoteDevice().getAddress());
                    Envelope envelope = receiveEnvelope(input);
                    Log.d(TAG, "Recieved envelope with uuid " + envelope.uuid + " from: " + envelope.senderNickname);

                    // hand over to the onMessageReceivedListener, which will take account for displaying
                    // the message and/or redistribute it to further recipients
                    listener.onMessageReceived(BluetoothProtocol.this, envelope);
                }
            } catch (IOException e) {
                // do nothing, this exception will occur a lot because there will be Bluetooth
                // devices nearby that do not run BonfireChat, resulting in no suitable
                // ServerSocket to accept this connection
                e.printStackTrace();
                teardown();
            }
        }
        public void sendNetworkPacket(final Envelope envelope) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (ConnectionHandler.this.output) {
                        sendEnvelope(ConnectionHandler.this.output, envelope);
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
    public void sendMessage(Envelope envelope) {
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }

        // create local copy to prevent ConcurrentModificationException (thread safe)
        BluetoothDevice[] localNearby = nearby.toArray(new BluetoothDevice[0]);
        for (BluetoothDevice device : localNearby) {
            ConnectionHandler socket = connectToDevice(device);
            if (socket == null) continue;
            socket.sendNetworkPacket(envelope);
        }

        if (!adapter.isDiscovering()) {
            adapter.startDiscovery();
        }
    }

    @Override
    public boolean canSend() {
        return true;
    }
}
