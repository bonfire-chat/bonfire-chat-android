package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by johannes on 22.05.15.
 */
public class BluetoothProtocol implements IProtocol {

    private static final String TAG = "BluetoothProtocol";
    private static final UUID BTMODULEUUID = UUID.fromString("D5AD0434-34AA-4B5C-B100-4964BFE3E739");
    private Identity identity;
    private OnMessageReceivedListener listener;
    private Context ctx;
    private BluetoothAdapter adapter;
    private List<BluetoothDevice> nearby;
    private List<BluetoothSocket> sockets;
    private List<OutputStream> output;
    private List<InputStream> input;
    private List<ConnectionHandler> connections;

    BluetoothProtocol(Context ctx) {
        this.ctx = ctx;
        output = new ArrayList<>();
        input = new ArrayList<>();
        nearby = new ArrayList<>();
        sockets = new ArrayList<>();
        connections = new ArrayList<>();

        adapter = BluetoothAdapter.getDefaultAdapter();
        ctx.registerReceiver(onDeviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        searchDevices();
    }

    private boolean ensureBluetoothUp() {
        if (adapter == null) {
            Log.d(TAG, "device does not support Bluetooth");
            if (adapter.isEnabled()) {
                Log.d(TAG, "bluetooth enabled");
                return true;
            } else  {
                Log.d(TAG, "bluetooth disabled - please enable it!");
            }
        }
        return false;
    }

    private void connect() {
        if (adapter.isDiscovering()) {
            Log.d(TAG, "stopping discovery for connecting");
            adapter.cancelDiscovery();
        }
        for (BluetoothDevice device: nearby) {
            try {
                BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
                socket.connect();
                sockets.add(socket);
                output.add(socket.getOutputStream());
                input.add(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
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
        if (!adapter.isDiscovering()) {
            Log.d(TAG, "starting discovery after disconnecting");
            adapter.startDiscovery();
        }
    }

    public BroadcastReceiver onDeviceFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); //may need to chain this to a recognizing function
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                nearby.add(device);
                Log.i(TAG, "device found: " + device.getName() + " - " + device.getAddress());
            }
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

    public Thread startListeningThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "running listener thread");
            try {

                BluetoothServerSocket ss = adapter.listenUsingInsecureRfcommWithServiceRecord("bonfire", BTMODULEUUID);
                while(true) {
                    BluetoothSocket socket = ss.accept();
                    ConnectionHandler handler = new ConnectionHandler(socket);
                    connections.add(handler);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    public class ConnectionHandler extends Thread {
        BluetoothSocket socket;
        public ConnectionHandler(BluetoothSocket socket) {
            this.socket = socket;
            this.start();
        }

        @Override
        public void run() {
            Log.i(TAG, "Client connected : " + socket.getRemoteDevice().getAddress());
        }
    }


    // ###########################################################################
    // ###    Implementation of IProtocol
    // ###########################################################################

    @Override
    public void sendMessage(Contact target, Message message) {
        Log.d(TAG, "broadcasting message via Bluetooth");

        // TODO: move to another function
        ensureBluetoothUp();
        connect();

        for (OutputStream stream: output) {
            byte[] buf = message.body.getBytes();
            try {
                stream.write(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        disconnect();
    }

    @Override
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    @Override
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }
}
