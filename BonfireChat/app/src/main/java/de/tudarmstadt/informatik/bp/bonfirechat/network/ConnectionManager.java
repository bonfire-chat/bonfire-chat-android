package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.jivesoftware.smack.SmackAndroid;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by mw on 21.05.15.
 *
 */
public class ConnectionManager extends IntentService {

    private static ConnectionManager instance;

    // action in Intents which are sent to the service
    public static final String GO_ONLINE_ACTION = "de.tudarmstadt.informatik.bp.bonfirechat.GO_ONLINE";

    // action in event Intents broadcasted by the service
    public static final String MSG_RECEIVED_BROADCAST_EVENT =
            "de.tudarmstadt.informatik.bp.bonfirechat.MSG_RECEIVED_BROADCAST";
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

    /**
     * Creates the ConnectionManager, called by Android creating the service
     */
    public ConnectionManager() {
        super("BonFire_ConnectionManager");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SmackAndroid.init(this);
    }

    public List<IProtocol> connections = new ArrayList<IProtocol>();

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
                p = (IProtocol) typ.newInstance();
                p.setIdentity(BonfireData.getInstance(this).getDefaultIdentity());
                p.setOnMessageReceivedListener(listener);
            } catch (InstantiationException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
            connections.add(p);
        }
        return p;
    }

    private OnMessageReceivedListener listener = new OnMessageReceivedListener() {
        @Override
        public void onMessageReceived(IProtocol sender, Message message) {
            BonfireData data = BonfireData.getInstance(ConnectionManager.this);
            Conversation conversation = data.getConversationByPeer(message.fromId);
            if (conversation == null) {
                conversation = new Conversation(new Contact(message.fromId), message.fromId, 0);
                data.createConversation(conversation);

                Intent localIntent =
                        new Intent(NEW_CONVERSATION_BROADCAST_EVENT)
                                // Puts the status into the Intent
                                .putExtra(EXTENDED_DATA_CONVERSATION_ID, conversation.getName());
            }
            data.createMessage(message, conversation);

            Intent localIntent =
                    new Intent(MSG_RECEIVED_BROADCAST_EVENT)
                            // Puts the status into the Intent
                            .putExtra(EXTENDED_DATA_CONVERSATION_ID, conversation.getName());
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(ConnectionManager.this).sendBroadcast(localIntent);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ConnectionManager.this)
                            .setSmallIcon(R.drawable.ic_drawer)
                            .setContentTitle(conversation.title)
                            .setContentText(message.body);
        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction() == GO_ONLINE_ACTION) {
            ClientServerProtocol xmpp = (ClientServerProtocol) getOrCreateConnection(ClientServerProtocol.class);
            xmpp.connectToServer(this);
        }
    }
}
