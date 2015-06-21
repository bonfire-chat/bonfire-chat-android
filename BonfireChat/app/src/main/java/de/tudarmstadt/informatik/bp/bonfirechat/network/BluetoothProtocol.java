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
    private List<OutputStream> output;
    private List<IncomingConnectionHandler> connections;
    private Handler searchLoopHandler;

    BluetoothProtocol(Context ctx) {
        this.ctx = ctx;
        output = new ArrayList<>();
        nearby = new ArrayList<>();
        sockets = new ArrayList<>();
        connections = new ArrayList<>();
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

    private void connect() {
        if (adapter.isDiscovering()) {
            Log.d(TAG, "stopping discovery for connecting");
            adapter.cancelDiscovery();
        }
        sockets.clear();
        output.clear();
        // create local copy to prevent ConcurrentModificationException (thread safe)
        BluetoothDevice[] localNearby = nearby.toArray(new BluetoothDevice[0]);
        for (BluetoothDevice device : localNearby) {
            try {
                BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
                socket.connect();
                sockets.add(socket);
                output.add(socket.getOutputStream());
            } catch (IOException e) {
                // do nothing, this exception will occur a lot because there will be Bluetooth
                // devices nearby that do not run BonfireChat, resulting in no suitable
                // ServerSocket to accept this connection
            }
        }
    }

    private void disconnect() {
        for (BluetoothSocket socket: sockets) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        nearby.clear();
        if (!adapter.isDiscovering()) {
            Log.d(TAG, "starting discovery after disconnecting");
            adapter.startDiscovery();
        }
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
                    IncomingConnectionHandler handler = new IncomingConnectionHandler(socket);
                    connections.add(handler);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    public class IncomingConnectionHandler extends Thread {
        BluetoothSocket socket;
        InputStream input;

        public IncomingConnectionHandler(BluetoothSocket socket) {
            this.socket = socket;
            try {
                input = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.start();
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "Client connected: " + socket.getRemoteDevice().getAddress());
                Envelope envelope = receiveEnvelope(input);
                Log.d(TAG, "Recieved envelope with uuid " + envelope.uuid + " from: " + envelope.senderNickname);

                // hand over to the onMessageReceivedListener, which will take account for displaying
                // the message and/or redistribute it to further recipients
                listener.onMessageReceived(BluetoothProtocol.this, envelope);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    // ###########################################################################
    // ###    Implementation of IProtocol
    // ###########################################################################

    @Override
    public void sendMessage(Envelope envelope) {
        Log.d(TAG, "broadcasting message via Bluetooth");

        // send the envelope
        connect();
        for (OutputStream stream : output) {
            sendEnvelope(stream, envelope);
        }
        try {        Thread.sleep(50); }catch(InterruptedException ex){}
        disconnect();
    }

    @Override
    public boolean canSend() {
        return true;
    }
}
