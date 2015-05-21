package de.tudarmstadt.informatik.bp.bonfirechat.network;


import android.content.Context;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.io.IOException;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by mw on 13.05.15.
 */
public class ClientServerProtocol implements IProtocol, ConnectionListener {
    private static final String TAG = "ClientServerProtocol";
    XMPPTCPConnection connection;
    Identity identity;
    private OnMessageReceivedListener listener;
    private Context ctx;

    public ClientServerProtocol(Identity i) {
        identity = i;

    }

    public static String getUsernameByHash(String hash) {
        return "u" + hash + "@teamwiki.de";
    }

    private void createMyAccount(Context ctx) {
        AccountManager a = AccountManager.getInstance(connection);
        try {
            a.createAccount(identity.username, identity.password);
            BonfireData.getInstance(ctx).updateIdentity(identity);

        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public void connectToServer(Context ctx) {

        connection = new XMPPTCPConnection(identity.username, identity.password, identity.server);
        connection.addConnectionListener(this);
        try {
            Log.d(TAG, "connecting XMPP");
            connection.connect();
            Log.d(TAG, " XMPP connected");

            if (identity.username == null || identity.username.equals("")) {
                identity.username = getUsernameByHash(identity.getPublicKeyHash());
                Log.d(TAG, "calling createMyAccount, with username="+identity.username);
                createMyAccount(ctx);
                return;
            }

            Log.d(TAG, "before connection login");
            connection.login();
            connection.addAsyncStanzaListener(onXMPPMessageReceivedListener, new StanzaTypeFilter(org.jivesoftware.smack.packet.Message.class));
            connection.addAsyncStanzaListener(onXMPPPresenceReceivedListener, new StanzaTypeFilter(org.jivesoftware.smack.packet.Presence.class));
            Log.d(TAG, "after connection login");
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private StanzaListener onXMPPMessageReceivedListener = new StanzaListener() {
        @Override
        public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
            org.jivesoftware.smack.packet.Message msg = (org.jivesoftware.smack.packet.Message) packet;
            Log.i(TAG, "processPacket : "+msg.getFrom()+" : " +msg.getBody());

            listener.onMessageReceived(ClientServerProtocol.this, new Message(msg.getBody(), Message.MessageDirection.Received));
        }
    };

    private StanzaListener onXMPPPresenceReceivedListener = new StanzaListener() {
        @Override
        public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
            Log.i(TAG, "presence: "+packet.getFrom());
        }
    };


    // ###########################################################################
    // ###    Implementation of IProtocol
    // ###########################################################################
    @Override
    public void sendMessage(String target, Message message) {
        String jid = getUsernameByHash(target);
        org.jivesoftware.smack.packet.Message msg = new org.jivesoftware.smack.packet.Message(jid, message.body);
        try {
            connection.sendStanza(msg);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }


    // ###########################################################################
    // ###    Implementation of ConnectionListener
    // ###########################################################################
    @Override
    public void connected(XMPPConnection connection) {
        Log.i(TAG, "XMPP : connected");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.i(TAG, "XMPP : authenticated");

    }

    @Override
    public void connectionClosed() {
        Log.i(TAG, "XMPP : connectionClosed");

    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.i(TAG, "XMPP : connectionClosedOnError");
        e.printStackTrace();
    }

    @Override
    public void reconnectionSuccessful() {
        Log.i(TAG, "XMPP : reconnectionSuccessful");

    }

    @Override
    public void reconnectingIn(int seconds) {
        Log.i(TAG, "XMPP : reconnectingIn");

    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.i(TAG, "XMPP : reconnectionFailed");

    }
}
