package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Trace;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.bouncycastle.util.Arrays;
import org.jivesoftware.smack.SmackAndroid;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.RingBuffer;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Envelope;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.ui.MainActivity;
import de.tudarmstadt.informatik.bp.bonfirechat.ui.MessagesActivity;

/**
 * Created by mw on 21.05.15.
 *
 */
public class ConnectionManager extends NonStopIntentService {

    private static final String TAG = "ConnectionManager";

    // action in Intents which are sent to the service
    public static final String GO_ONLINE_ACTION = "de.tudarmstadt.informatik.bp.bonfirechat.GO_ONLINE";
    public static final String SENDMESSAGE_ACTION = "de.tudarmstadt.informatik.bp.bonfirechat.SENDMESSAGE";

    // action in event Intents broadcasted by the service
    public static final String MSG_RECEIVED_BROADCAST_EVENT =
            "de.tudarmstadt.informatik.bp.bonfirechat.MSG_RECEIVED_BROADCAST";
    public static final String MSG_SENT_BROADCAST_EVENT =
            "de.tudarmstadt.informatik.bp.bonfirechat.MSG_SENT_BROADCAST";
    public static final String CONNECTED_BROADCAST_EVENT =
            "de.tudarmstadt.informatik.bp.bonfirechat.CONNECTED_BROADCAST";
    public static final String CONNECTION_CLOSED_BROADCAST_EVENT =
            "de.tudarmstadt.informatik.bp.bonfirechat.CONNECTION_CLOSED_BROADCAST";
    public static final String NEW_CONVERSATION_BROADCAST_EVENT =
            "de.tudarmstadt.informatik.bp.bonfirechat.NEW_CONVERSATION_BROADCAST";



    public static final String EXTENDED_DATA_CONVERSATION_ID =
            "de.tudarmstadt.informatik.bp.bonfirechat.CONVERSATION_ID";
    public static final String EXTENDED_DATA_PEER_ID =
            "de.tudarmstadt.informatik.bp.bonfirechat.PEER_ID";
    public static final String EXTENDED_DATA_MESSAGE_TEXT =
            "de.tudarmstadt.informatik.bp.bonfirechat.MESSAGE_TEXT";
    public static final String EXTENDED_DATA_MESSAGE_UUID =
            "de.tudarmstadt.informatik.bp.bonfirechat.MESSAGE_UUID";
    public static final String EXTENDED_DATA_ERROR =
            "de.tudarmstadt.informatik.bp.bonfirechat.ERROR";


    // maximum hops for a message until it will be discarded
    public static final int MAX_HOP_COUNT = 20;

    // buffer for storing which messages have already been sent
    // those won't be sent again, to avoid routing loops
    // as a circular buffer, old messages will gradually be forgotten,
    // because they won't be sent again due to their hopCount anyway
    private static RingBuffer<UUID> sentMessages = new RingBuffer<>(250);

    /**
     * Creates the ConnectionManager, called by Android creating the service
     */
    public ConnectionManager() {
        super("BonFire_ConnectionManager");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(this, "ConnectionManager created", Toast.LENGTH_SHORT).show();
        SmackAndroid.init(this);
    }

    public static List<IProtocol> connections = new ArrayList<IProtocol>();

    public IProtocol getOrCreateConnection(Class typ) {
        IProtocol p = getConnection(typ);
        if (p == null) {
            try {
                p = (IProtocol) typ.getDeclaredConstructor(Context.class).newInstance(ConnectionManager.this);
                p.setIdentity(BonfireData.getInstance(this).getDefaultIdentity());
                p.setOnMessageReceivedListener(listener);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            connections.add(p);
        }
        return p;
    }

    private IProtocol getConnection(Class typ) {
        for(IProtocol p : connections) {
            if (typ.isInstance(p)) return p;
        }
        return null;
    }

    private OnMessageReceivedListener listener = new OnMessageReceivedListener() {
        @Override
        public void onMessageReceived(IProtocol sender, Envelope envelope) {
            Log.i(TAG, "Received message from "+sender.getClass().getName()+"   uuid="+envelope.uuid.toString());
            TracerouteHandler.handleTraceroute(ConnectionManager.this, sender, "Recv", envelope);
            // is this envelope sent to us?
            if (envelope.containsRecipient(BonfireData.getInstance(ConnectionManager.this).getDefaultIdentity())) {
                TracerouteHandler.publishTraceroute(envelope);
                Message message = envelope.toMessage(ConnectionManager.this);
                message.transferProtocol = sender.getClass().getName();
                storeAndDisplayMessage(message);
                // redistribute the envelope if there are further recipients
                if (envelope.recipientsPublicKeys.size() > 1) {
                    redistributeEnvelope(envelope);
                }
            } else {
                redistributeEnvelope(envelope);
            }
        }

        private void redistributeEnvelope(Envelope envelope) {
            envelope.hopCount += 1;
            // if the envelope has been sent less than 20 hops, redistribute it
            if (envelope.hopCount < MAX_HOP_COUNT) {
                sendEnvelope(ConnectionManager.this, envelope);
            }
        }

        private void storeAndDisplayMessage(Message message) {
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(ConnectionManager.this);
            Log.d(TAG, "display message: " + message.body);
            BonfireData data = BonfireData.getInstance(ConnectionManager.this);
            Conversation conversation = data.getConversationByPeer((Contact) message.sender);
            if (conversation == null) {
                Log.d(TAG, "creating new conversation for peer "+message.sender.getNickname());
                conversation = new Conversation((Contact) message.sender, message.sender.getNickname(), 0);
                data.createConversation(conversation);

                Intent localIntent = new Intent(NEW_CONVERSATION_BROADCAST_EVENT)
                        // Puts the status into the Intent
                        .putExtra(EXTENDED_DATA_CONVERSATION_ID, conversation.rowid);
                broadcastManager.sendBroadcast(localIntent);

            }
            Log.d(TAG, "conversationId=" + conversation.rowid);

            data.createMessage(message, conversation);
            Log.d(TAG, "message stored in db with uuid=" + message.uuid);

            Intent localIntent = new Intent(MSG_RECEIVED_BROADCAST_EVENT)
                    .putExtra(EXTENDED_DATA_CONVERSATION_ID, conversation.rowid)
                    .putExtra(EXTENDED_DATA_MESSAGE_UUID, message.uuid)
                    .putExtra(EXTENDED_DATA_MESSAGE_TEXT, message.body);
            // Broadcasts the Intent to receivers in this app.
            broadcastManager.sendBroadcast(localIntent);


            Intent intent = new Intent(ConnectionManager.this, MessagesActivity.class);
            intent.putExtra("ConversationId", conversation.rowid);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ConnectionManager.this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pi = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
            //PendingIntent.getActivity(ConnectionManager.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.correct);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ConnectionManager.this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(conversation.title)
                            .setContentText(message.body)
                            .setContentIntent(pi)
                            .setSound(sound)
                            .setAutoCancel(true)
                            .setVibrate(new long[]{500, 500, 150, 150, 150, 150, 300, 300, 300, 0});

            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(1, mBuilder.build());
        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        BonfireData db = BonfireData.getInstance(this);
        if (intent.getAction() == GO_ONLINE_ACTION) {
            Identity id = db.getDefaultIdentity();
            id.registerWithServer();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (preferences.getBoolean("enable_xmpp", true)) {
                //getOrCreateConnection(ClientServerProtocol.class);
                getOrCreateConnection(GcmProtocol.class);
            }
            if (preferences.getBoolean("enable_bluetooth", true)) {
                getOrCreateConnection(BluetoothProtocol.class);
            }
            if (preferences.getBoolean("enable_wifi", true)) {
                // getOrCreateConnection(WifiProtocol.class);
            }
        } else if (intent.getAction() == SENDMESSAGE_ACTION) {
            Exception error = null;
            Envelope envelope = (Envelope) intent.getSerializableExtra("envelope");
            Log.d(TAG, "Loading envelope with uuid "+envelope.uuid+": from "+envelope.senderNickname);
            try {
                IProtocol protocol = chooseConnection();
                TracerouteHandler.handleTraceroute(this, protocol, "Send", envelope);
                if (null != protocol) {
                    protocol.sendMessage(envelope);
                } else {
                    error = new RuntimeException("No connection available for sending :(");
                }

            } catch(Exception ex) {
                ex.printStackTrace();
                error = ex;
            }

            // if a message object is specified, this envelope was just generated on this phone
            // notify UI about success or failure
            Intent localIntent = new Intent(MSG_SENT_BROADCAST_EVENT)
                    .putExtra(EXTENDED_DATA_MESSAGE_UUID, envelope.uuid);
            if (error != null) localIntent.putExtra(EXTENDED_DATA_ERROR, error.toString());

            LocalBroadcastManager.getInstance(ConnectionManager.this).sendBroadcast(localIntent);

        } else if (!extras.isEmpty()) {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            String messageType = gcm.getMessageType(intent);

            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(intent.getAction())) {
                Log.w(TAG, "GCM: Send error: " + extras.toString());

            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(intent.getAction())) {
                Log.w(TAG, "GCM: Deleted messages on server: " + extras.toString());

            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                GcmProtocol gcmProto = (GcmProtocol)getConnection(GcmProtocol.class);
                Log.i(TAG, "gcmProto="+gcmProto);
                if (gcmProto == null) return;
                gcmProto.onHandleGcmIntent(intent);
            }
        }
    }



    private IProtocol chooseConnection() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Bluetooth enabled and ready?
        if (preferences.getBoolean("enable_bluetooth", true)) {
            IProtocol p = getOrCreateConnection(BluetoothProtocol.class);
            if (p.canSend()) {
                return p;
            }
        }
        // WiFi enabled and ready?
        /*if (preferences.getBoolean("enable_wifi", true)) {
            IProtocol p = getOrCreateConnection(WiFiProtocol.class);
            if (p.canSend()) {
                return p;
            }
        }*/
        // ClientServer enabled and ready?
        if (preferences.getBoolean("enable_xmpp", true)) {
            IProtocol p = getOrCreateConnection(GcmProtocol.class);
            if (p.canSend()) {
                return p;
            }
        }
        return null;
    }


    // static helper method to enqueue
    public static void sendEnvelope(Context ctx, Envelope envelope) {
        // check whether this envelope has already been sent
        if (!sentMessages.contains(envelope.uuid)) {
            // remember this envelope
            sentMessages.enqueue(envelope.uuid);
            // and dispatch sending intent
            Intent intent = new Intent(ctx, ConnectionManager.class);
            intent.setAction(ConnectionManager.SENDMESSAGE_ACTION);
            intent.putExtra("envelope", envelope);
            ctx.startService(intent);
        }
    }

    // you know, for convenience and stuff
    public static void sendMessage(Context ctx, Message message) {
        sendEnvelope(ctx, Envelope.fromMessage(message));
    }
}
