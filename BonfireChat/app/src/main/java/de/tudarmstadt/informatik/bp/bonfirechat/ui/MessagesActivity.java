package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.InputBox;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Envelope;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;


public class MessagesActivity extends Activity {

    private static final String TAG = "MessagesActivity";
    List<Message> messages = new ArrayList<Message>();
    private Conversation conversation;
    private BonfireData db = BonfireData.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_messages);
        BonfireData db = BonfireData.getInstance(this);
        long convId = getIntent().getLongExtra("ConversationId", 0);
        conversation = db.getConversationById(convId);
        if (conversation == null) {
            Log.e("MessagesActivity", "Error, conversation with id " + convId + " not found");
        }
        getActionBar().setTitle(conversation.title);

        //new Conversation( new Contact(getIntent().getStringExtra("Conversation")));

        final ListView lv = (ListView) findViewById(R.id.messages_view);
        /*messages.add(new Message("At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.", Message.MessageDirection.Received));
        messages.add(new Message("Hallo", Message.MessageDirection.Sent));
        messages.add(new Message("Wie gehts?", Message.MessageDirection.Sent));
        messages.add(new Message("wie stehts?", Message.MessageDirection.Received));
        messages.add(new Message("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.", Message.MessageDirection.Received));
        messages.addAll(db.getMessages(conversation));*/
        messages = db.getMessages(conversation);
        lv.setAdapter(new MessagesAdapter(this, messages));

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Message msg = messages.get(position);
                Toast.makeText(MessagesActivity.this, "protocol=" + msg.transferProtocol + "  " + "error=" + msg.error, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        findViewById(R.id.textSendButton).setOnClickListener(onSendButtonClickListener);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        appendMessage(new Message(intent.getStringExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_TEXT)
                                , conversation.getPeer(), new Date(),
                                (UUID)intent.getSerializableExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID)
                        ));
                    }
                },
                new IntentFilter(ConnectionManager.MSG_RECEIVED_BROADCAST_EVENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        UUID sentUUID = (UUID)intent.getSerializableExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID);
                        Log.i(TAG, "MSG_SENT: "+sentUUID.toString()+" - "+intent.getStringExtra(ConnectionManager.EXTENDED_DATA_ERROR));
                        for(Message m : messages) {
                            if (m.uuid.equals(sentUUID)) {
                                if (intent.hasExtra(ConnectionManager.EXTENDED_DATA_ERROR)) {
                                    m.error = "ERR: "+intent.getStringExtra(ConnectionManager.EXTENDED_DATA_ERROR);
                                } else {
                                    m.error = null;
                                }
                                BonfireData db = BonfireData.getInstance(MessagesActivity.this);
                                db.updateMessage(m);
                                ((MessagesAdapter) lv.getAdapter()).notifyDataSetChanged();
                                return;
                            }
                        }
                    }
                },
                new IntentFilter(ConnectionManager.MSG_SENT_BROADCAST_EVENT));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent: "+intent);
    }

    private View.OnClickListener onSendButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText ed = (EditText) findViewById(R.id.textinput);
            String msg = ed.getText().toString();
            ed.setText("");

            Message message = new Message(msg, db.getDefaultIdentity(), new Date(), conversation.getPeer());
            message.error = "Sending";

            db.createMessage(message, conversation);
            appendMessage(message);

            Log.d(TAG, "sending message id " + message.uuid);
            ConnectionManager.sendMessage(MessagesActivity.this, message);
        }
    };

    private void appendMessage(Message message) {
        messages.add(message);
        ListView lv = (ListView) findViewById(R.id.messages_view);
        ((MessagesAdapter)lv.getAdapter()).notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_messages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            String info  = "Titel: " + conversation.title + "\nId: " + conversation.rowid +
                    "\nTyp: "+conversation.conversationType.toString()+
                    "";
            if (conversation.getPeer() != null) {
                info += "\nKontakt: " + conversation.getPeer().getNickname() +
                        "\nName: " + conversation.getPeer().getFirstName() + " "+conversation.getPeer().getLastName() +
                        "\nXMPP ID: " + conversation.getPeer().getXmppId();
            }
            InputBox.Info(this, "Info", info);
            return true;

        } else if (id == R.id.action_edit_title) {
            InputBox.InputBox(this, "Name ändern", "",
                    conversation.getName(),
                    new InputBox.OnOkClickListener() {
                        @Override
                        public void onOkClicked(String input) {
                            conversation.title = input;
                            getActionBar().setTitle(conversation.title);
                            BonfireData.getInstance(MessagesActivity.this).updateConversation(conversation);
                        }
                    });
            return true;

        } else if (id == R.id.action_tracert) {
            Message m = new Message("TRACEROUTE\n", db.getDefaultIdentity(), new Date(), conversation.getPeer());
            Envelope e = Envelope.fromMessage(m);
            e.flags = Envelope.FLAG_TRACEROUTE;
            m.body = "TRACEROUTE: " + BonfireData.API_ENDPOINT + "/traceroute.php?uuid=" + e.uuid.toString();
            appendMessage(m);
            Log.d(TAG, "sending tracert id " + e.uuid);
            ConnectionManager.sendEnvelope(MessagesActivity.this, e);
            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BonfireData.API_ENDPOINT + "/traceroute.php?uuid=" + e.uuid.toString())));
        }

        return super.onOptionsItemSelected(item);
    }

    public static void startConversationWithPeer(Context ctx, Contact contact) {
        // conversation with this contact already exists?
        BonfireData db = BonfireData.getInstance(ctx);
        Conversation conversation = db.getConversationByPeer(contact);
        if (conversation == null) {
            // add a new conversation
            conversation = new Conversation(contact, contact.getNickname(), 0);
            db.createConversation(conversation);
        }
        // start messages activity
        Intent i = new Intent(ctx, MessagesActivity.class);
        Log.i("ContactDetailsActivity", "starting MessagesActivity with ConversationId=" + conversation.rowid);
        i.putExtra("ConversationId", conversation.rowid);
        ctx.startActivity(i);
    }
}
