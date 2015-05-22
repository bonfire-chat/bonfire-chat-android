package de.tudarmstadt.informatik.bp.bonfirechat.network;


import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.util.dns.HostAddress;

import java.io.IOException;

import javax.net.ssl.SSLContext;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
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

    public ClientServerProtocol(Context ctx) {
        this.ctx = ctx;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public static String getJidByHash(String hash) {
        return "u" + hash + "@teamwiki.de";
    }

    private void createMyAccount(Context ctx) {
        AccountManager a = AccountManager.getInstance(connection);
        try {
            a.createAccount(StringUtils.parseName(identity.username), identity.password);
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

        Log.d(TAG, "connecting XMPP server="+identity.server+", username="+identity.username+", password="+identity.password);
        ConnectionConfiguration config = new ConnectionConfiguration("teamwiki.de");
        //config.setCustomSSLContext(SSLCon);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        //config.set
        connection = new XMPPTCPConnection(config);
        connection.addConnectionListener(this);
        try {
            connection.connect();
            Log.d(TAG, " XMPP connected");

            if (identity.username == null || identity.username.equals("")) {
                identity.username = getJidByHash(identity.getPublicKeyHash());
                Log.d(TAG, "calling createMyAccount, with username="+identity.username);
                createMyAccount(ctx);
            }

            Log.d(TAG, "before connection login");
            connection.login(StringUtils.parseName(identity.username), identity.password, "Bonfire");
            connection.addPacketListener(onXMPPMessageReceivedListener, new PacketTypeFilter(org.jivesoftware.smack.packet.Message.class));
            connection.addPacketListener(onXMPPPresenceReceivedListener, new PacketTypeFilter(org.jivesoftware.smack.packet.Presence.class));
            Log.d(TAG, "after connection login");
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.ConnectionException e) {
            e.printStackTrace();
            for(HostAddress host : e.getFailedAddresses()) Log.e(TAG, "Failed address: "+host.toString());
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private PacketListener onXMPPMessageReceivedListener = new PacketListener() {
        @Override
        public void processPacket(Packet packet) throws SmackException.NotConnectedException {
            org.jivesoftware.smack.packet.Message msg = (org.jivesoftware.smack.packet.Message) packet;
            Log.i(TAG, "processPacket : " + msg.getFrom() + " : " + msg.getBody());
            if (msg.getBody() == null) return;

            BonfireData db = BonfireData.getInstance(ctx);
            Contact c = db.getContactByXmppId(msg.getFrom());
            if (c == null) {
                c = new Contact(StringUtils.parseName(msg.getFrom()));
                c.setXmppId(msg.getFrom());
                db.createContact(c);
            }
            listener.onMessageReceived(ClientServerProtocol.this, new Message(msg.getBody(), c, Message.MessageDirection.Received, DateHelper.getNowString()));
        }
    };

    private PacketListener onXMPPPresenceReceivedListener = new PacketListener() {
        @Override
        public void processPacket(Packet packet) throws SmackException.NotConnectedException {
            Log.i(TAG, "presence: "+packet.getFrom());
        }
    };


    // ###########################################################################
    // ###    Implementation of IProtocol
    // ###########################################################################
    @Override
    public void sendMessage(Contact target, Message message) {
        String jid = target.getXmppId(); // getJidByHash(target);
        org.jivesoftware.smack.packet.Message msg = new org.jivesoftware.smack.packet.Message(jid);
        msg.setBody(message.body);
        try {
            connection.sendPacket(msg);
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
    public void authenticated(XMPPConnection xmppConnection) {
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
