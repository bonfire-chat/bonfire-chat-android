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
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.data.ConstOptions;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Packet;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.PacketType;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.TracerouteHopSegment;
import de.tudarmstadt.informatik.bp.bonfirechat.stats.StatsCollector;
import de.tudarmstadt.informatik.bp.bonfirechat.ui.EnableBluetoothActivity;

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

    public static boolean isSupported() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null;
    }

    public Set<Map.Entry<String,ConnectionHandler>> getConnections() {
        synchronized (connections) {
            return connections.entrySet();
        }
    }

    private boolean ensureBluetoothUp() {
        if (adapter == null) {
            Log.d(TAG, "device does not support Bluetooth");
        } else {
            Log.d(TAG, "enabling bluetooth and requesting infinite discoverability");
            Intent intent =new Intent(ctx, EnableBluetoothActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        }
        return false;
    }


    /**
     * Called by ConnectionManager on receipt of CONTINUE_BLUETOOTH_STARTUP_ACTION.
     * This gets sent by EnableBluetoothActivity after the user has accepted the activation
     * of bluetooth discoverability via the required system UI.
     */
    public void continueStartup() {
        Log.i(TAG, "initializing Bluetooth");
        initializeBluetooth();
        
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
                String name = device.getName();
                Log.d(TAG, "device found: " + name + " - " + device.getAddress());

                //TODO HACK: ignore non-bonfire devices...
                //maybe blacklisting after n unsuccessful socket.connect
                if (name != null && name.contains("BEACON")) return;

                peerListener.discoveredPeer(BluetoothProtocol.this, Peer.addressFromString(device.getAddress()), name);
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
                    try {
                        BluetoothSocket socket = server.accept();
                        ConnectionHandler handler = new ConnectionHandler(socket);
                        synchronized (connections) {
                            connections.put(socket.getRemoteDevice().getAddress(), handler);
                        }
                    } catch(IOException ex) {
                        Log.e(TAG, "ConnectionHandler constructor fail");
                        Log.e(TAG, ex.getMessage());
                    }
                }

            } catch (IOException e) {
                String errMes = "MEEP MEEP MEEP: Exception in Blueooth Listener Thread, bluetooth connections won't work until app restart! ";
                Log.e(TAG, errMes);
                StatsCollector.publishError(BluetoothProtocol.class, "ERR", null, errMes);
                e.printStackTrace();
            }
        }
    });

    public class ConnectionHandler extends Thread {
        final BluetoothSocket socket;
        final InputStream input;
        final ObjectOutputStream stream;
        final byte[] peerMacAddress;
        final String formattedMacAddress;
        int sent=0,received=0;
        public ConnectionHandler(BluetoothSocket socket) throws IOException {
            this.socket = socket;
            formattedMacAddress = socket.getRemoteDevice().getAddress();
            peerMacAddress = Peer.addressFromString(formattedMacAddress);
            input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            stream = new ObjectOutputStream(output);
            this.start();
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "Client connected: " + formattedMacAddress);

                final ObjectInputStream stream = new ObjectInputStream(input);
                while(true) {
                    final Packet packet = (Packet) stream.readObject();
                    Log.d(TAG, "Received " + packet.toString());
                    packet.addPathNode(peerMacAddress);
                    // add traceroute segment to payload packets
                    if (packet.getType() == PacketType.Payload) {
                        packet.addTracerouteSegment(new TracerouteHopSegment(TracerouteHopSegment.ProtocolType.BLUETOOTH, packet.getLastHopTimeSent(), new Date()));
                    }
                    // hand over to the onMessageReceivedListener, which will take account for displaying
                    // the message and/or redistribute it to further recipients
                    packetListener.onPacketReceived(BluetoothProtocol.this, packet);
                    received++;
                }
            } catch(ClassNotFoundException ex) {
                Log.e(TAG, "Unable to deserialize packet, class not found ("+ex.getMessage() + "), closing connection!");
                teardown();
            } catch (IOException e) {
                // On connection errors, tear down this connection and remove from the list
                // of active connections.
                StatsCollector.publishError(BluetoothProtocol.class, "ERR", null, "Error in recv thread: "+e.getMessage());
                e.printStackTrace();
                teardown();
            }
        }

        public void sendNetworkPacket(final Packet packet) {
            Log.d(TAG, "sendNetworkPacket to "+formattedMacAddress+" | "+packet.toString());
            synchronized (ConnectionHandler.this.stream) {
                try {
                    stream.writeObject(packet);
                    stream.flush();
                    sent++;
                } catch(IOException ex) {
                    Log.w(TAG, "sendNetworkPacket: Could not send to "+formattedMacAddress+" : "+packet.toString());
                    Log.w(TAG, ex.getMessage());
                    StatsCollector.publishError(BluetoothProtocol.class, "ERR", packet.uuid, "Could not send to " + formattedMacAddress + " : " + ex.getMessage());
                    // Connection is broken, remove from list
                    teardown();
                }
            }
        }
        private void teardown() {
            Log.w(TAG,"ConnectionHandler: tearing down "+formattedMacAddress);
            synchronized (connections) {
                connections.remove(formattedMacAddress);
            }
            try {
                socket.close();
            } catch (IOException e) {/*ignore*/}
        }

        @Override
        public String toString() {
            return "BT.ConnectionHandler("+formattedMacAddress+", sent="+sent+", received="+received+")";
        }
    }

    ConnectionHandler connectToDevice(BluetoothDevice device, UUID uuid) {
        try {
            synchronized (connections) {
                if (connections.containsKey(device.getAddress()))
                    return connections.get(device.getAddress());
            }
            if (ConstOptions.DELAYED_BT_CONNECTION > 0) {
                int delay = new Random().nextInt(ConstOptions.DELAYED_BT_CONNECTION) + 1;
                Log.d(TAG, "delaying connection by "+delay+" ms");
                Thread.sleep(delay);
            }

            BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
            socket.connect();
            ConnectionHandler handler = new ConnectionHandler(socket);
            synchronized (connections) {
                connections.put(device.getAddress(), handler);
            }
            return handler;
        }catch(InterruptedException e){
            //ignore
            return null;
        } catch (IOException e) {
            // do nothing, this exception will occur a lot because there will be Bluetooth
            // devices nearby that do not run BonfireChat, resulting in no suitable
            // ServerSocket to accept this connection
            Log.e(TAG, "Unable to connect to bluetooth device "+device.getAddress()+", ignoring");
            Log.e(TAG, e.getMessage());
            StatsCollector.publishError(BluetoothProtocol.class, "ERR", uuid,
                    "Unable to connect to bluetooth device "+device.getAddress()+": "+e.getMessage());

            try {
                Log.w(TAG, "Trying ugly workaround from the internet - here be dragons ...");

                BluetoothSocket socket = (BluetoothSocket) device.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class}).invoke(device,1);
                socket.connect();
                Log.i(TAG, "W O W  - success!");

                ConnectionHandler handler = new ConnectionHandler(socket);
                synchronized (connections) {
                    connections.put(device.getAddress(), handler);
                }
                return handler;

            } catch(Exception ex2) {
                // all kinds of exceptions...
                Log.e(TAG, "Ugly workaround did not work around: "+ex2.getMessage());

                return null;
            }
        }
    }

    // ###########################################################################
    // ###    Implementation of IProtocol
    // ###########################################################################

    @Override
    public void sendPacket(Packet packet, Peer peer) {
        Log.d(TAG, "sending packet to peers via Bluetooth");
        // Prevent discovery start while sending
        searchLoopHandler.removeCallbacksAndMessages(null);
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }


        // send packet only to specified peer. Just try sending it to
        // the addresses, no discovering necessary to send the packet
        ConnectionHandler socket = connectToDevice(adapter.getRemoteDevice(peer.getAddress()), packet.uuid);
        if (socket != null) {
            socket.sendNetworkPacket(packet);
        }

        // Start discovery again after five seconds, if sendPacket is not called in the mean time.
        searchLoopHandler.postDelayed(searchDevicesThread, 5555);
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
        synchronized (connections) {
            for (ConnectionHandler c : connections.values()) {
                c.teardown();
            }
        }
    }

    @Override
    public String toString() {
        if (adapter != null) {
            return "BluetoothProtocol(name=" + this.adapter.getName() + ", mac=" + this.adapter.getAddress() + ")";
        } else {
            return "BluetoothProtocol(device unsupported!)";
        }
    }
}
