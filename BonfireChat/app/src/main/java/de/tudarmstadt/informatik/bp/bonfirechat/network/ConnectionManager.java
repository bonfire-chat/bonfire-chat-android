package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackAndroid;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.ui.MainActivity;
import de.tudarmstadt.informatik.bp.bonfirechat.ui.MessagesActivity;

/**
 * Created by mw on 21.05.15.
 *
 */
public class ConnectionManager extends NonStopIntentService {

    private static final String TAG = "ConnectionManager";
    private static ConnectionManager instance;

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
    public static final String EXTENDED_DATA_MESSAGE_ID =
            "de.tudarmstadt.informatik.bp.bonfirechat.MESSAGE_ID";
    public static final String EXTENDED_DATA_ERROR =
            "de.tudarmstadt.informatik.bp.bonfirechat.ERROR";

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

    public Class getConnectionClassByName(String name) {
        if (name.equals("ClientServerProtocol")) {
            return ClientServerProtocol.class;
        } else if (name.equals("EchoProtocol")) {
            return EchoProtocol.class;
        } else {
            throw new IllegalArgumentException("Unknown Connection class "+name);
        }
    }

    public IProtocol getConnection(Class typ) {
        for(IProtocol p : connections) {
            if (typ.isInstance(p)) return p;
        }
        return null;
    }

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

    private OnMessageReceivedListener listener = new OnMessageReceivedListener() {
        @Override
        public void onMessageReceived(IProtocol sender, Message message) {
            Log.d(TAG, "received message : " + message.body);
            BonfireData data = BonfireData.getInstance(ConnectionManager.this);
            Conversation conversation = data.getConversationByPeer(message.peer);
            if (conversation == null) {
                Log.d(TAG, "creating new conversation for peer "+message.peer.getNickname());
                conversation = new Conversation(message.peer, message.peer.getNickname(), 0);
                data.createConversation(conversation);

                Intent localIntent = new Intent(NEW_CONVERSATION_BROADCAST_EVENT)
                                // Puts the status into the Intent
                                .putExtra(EXTENDED_DATA_CONVERSATION_ID, conversation.getName());
            }
            Log.d(TAG, "conversationId=" + conversation.rowid);

            data.createMessage(message, conversation);
            Log.d(TAG, "message stored in db with id=" + message.rowid);

            Intent localIntent = new Intent(MSG_RECEIVED_BROADCAST_EVENT)
                    .putExtra(EXTENDED_DATA_CONVERSATION_ID, conversation.rowid)
                    .putExtra(EXTENDED_DATA_PEER_ID, message.peer.rowid)
                            .putExtra(EXTENDED_DATA_MESSAGE_TEXT, message.body);
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(ConnectionManager.this).sendBroadcast(localIntent);


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
                    .setVibrate(new long[]{500, 500, 150, 150, 150, 150, 300, 300, 300, 0});

            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(1, mBuilder.build());
        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction() == GO_ONLINE_ACTION) {
            ClientServerProtocol xmpp = (ClientServerProtocol) getOrCreateConnection(ClientServerProtocol.class);
            xmpp.connectToServer(this);

        } else if (intent.getAction() == SENDMESSAGE_ACTION) {
            Exception error = null;
            BonfireData db = BonfireData.getInstance(this);
            Message message = db.getMessageById(intent.getLongExtra("messageId", -1));
            Log.d(TAG, "Loading message id "+intent.getLongExtra("messageId", -1)+" = "+message);
            try {
                Class protocolClass = getConnectionClassByName(intent.getStringExtra("protocolName"));

                IProtocol protocol = getConnection(protocolClass);
                protocol.sendMessage(db.getContactById(intent.getLongExtra("contactId", -1)),
                        message);

            }catch(Exception ex) {
                ex.printStackTrace();
                error = ex;
            }

            Intent localIntent = new Intent(MSG_SENT_BROADCAST_EVENT)
                    .putExtra(EXTENDED_DATA_MESSAGE_ID, message.rowid);
            if (error != null) localIntent.putExtra(EXTENDED_DATA_ERROR, error.toString());

            LocalBroadcastManager.getInstance(ConnectionManager.this).sendBroadcast(localIntent);

        }
    }
}
