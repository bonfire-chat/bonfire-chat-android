package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireAPI;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.data.NetworkOptions;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.InputBox;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Envelope;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.network.BluetoothProtocol;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;
import de.tudarmstadt.informatik.bp.bonfirechat.network.GcmProtocol;


public class MessagesActivity extends Activity {

    private static final String TAG = "MessagesActivity";
    List<Message> messages = new ArrayList<>();
    private MessagesAdapter adapter;
    private Conversation conversation;
    private final BonfireData db = BonfireData.getInstance(this);
    int preferredProtocol = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_messages);
        long convId = getIntent().getLongExtra("ConversationId", 0);
        conversation = db.getConversationById(convId);
        if (conversation == null) {
            Log.e("MessagesActivity", "Error, conversation with id " + convId + " not found");
        }
        getActionBar().setTitle(conversation.title);

        final ListView lv = (ListView) findViewById(R.id.messages_view);
        messages = db.getMessages(conversation);
        adapter = new MessagesAdapter(this, messages);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MessagesActivity.this, MessageDetailsActivity.class);
                Log.i(TAG, "starting MessagesActivity with message uuid=" + adapter.getItem(position).uuid);
                intent.putExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID, adapter.getItem(position).uuid);
                startActivity(intent);
            }
        });

        findViewById(R.id.textSendButton).setOnClickListener(onSendButtonClickListener);

        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(multiChoiceListener);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        long conversationId = intent.getLongExtra(ConnectionManager.EXTENDED_DATA_CONVERSATION_ID, -1);
                        if (conversationId != conversation.rowid) return;
                        UUID uuid = (UUID) intent.getSerializableExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID);
                        appendMessage(db.getMessageByUUID(uuid));
                    }
                },
                new IntentFilter(ConnectionManager.MSG_RECEIVED_BROADCAST_EVENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        UUID sentUUID = (UUID)intent.getSerializableExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID);
                        Log.i(TAG, "MSG_SENT: " + sentUUID.toString() + " - " + intent.getStringExtra(ConnectionManager.EXTENDED_DATA_ERROR));
                        for(Message m : messages) {
                            if (m.uuid.equals(sentUUID)) {
                                if (intent.hasExtra(ConnectionManager.EXTENDED_DATA_ERROR)) {
                                    m.error = intent.getStringExtra(ConnectionManager.EXTENDED_DATA_ERROR);
                                    m.flags |= Message.FLAG_FAILED;
                                } else {
                                    m.error = "Sent(" + (String.valueOf(intent.getIntExtra(ConnectionManager.EXTENDED_DATA_RETRANSMISSION_COUNT, -1))) + ")";
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
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        UUID ackedUUID = (UUID)intent.getSerializableExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID);
                        Log.i(TAG, "MSG_ACKED: "+ackedUUID.toString());

                        // bestätigte Nachricht suchen
                        for(Message m : messages) {
                            if (m.uuid.equals(ackedUUID)) {
                                // Haken anzeigen
                                m.flags |= Message.FLAG_ACKNOWLEDGED;
                                // Protokoll(e) anzeigen
                                // TODO wenn mehrere Protokolle verwendet, evtl mehrere Icons?
                                m.setTransferProtocol((Class)intent.getSerializableExtra(ConnectionManager.EXTENDED_DATA_PROTOCOL_CLASS));
                                m.error = null;

                                // TODO: DB Update in ConnectionManager verlagern, damit es auch stattfindet
                                // wenn die zugehörige Conversation nicht im vordergrund ist
                                BonfireData db = BonfireData.getInstance(MessagesActivity.this);
                                db.updateMessage(m);

                                ((MessagesAdapter) lv.getAdapter()).notifyDataSetChanged();
                                return;
                            }
                        }
                    }
                },
                new IntentFilter(ConnectionManager.MSG_ACKED_BROADCAST_EVENT));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent: " + intent);
    }

    private View.OnClickListener onSendButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText ed = (EditText) findViewById(R.id.textinput);
            String msg = ed.getText().toString();
            ed.setText("");

            Message message = new Message(msg, db.getDefaultIdentity(), new Date(), Message.FLAG_ENCRYPTED, conversation.getPeer());
            message.error = "Sending";

            db.createMessage(message, conversation);
            appendMessage(message);

            Log.d(TAG, "sending message id " + message.uuid);
            ConnectionManager.sendMessage(MessagesActivity.this, message);
        }
    };

    private void appendMessage(Message message) {
        ListView lv = (ListView) findViewById(R.id.messages_view);
        ((MessagesAdapter)lv.getAdapter()).add(message);
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
            Message m = new Message("TRACEROUTE\n", db.getDefaultIdentity(), new Date(), 0, conversation.getPeer());
            Envelope e = Envelope.fromMessage(m, false);
            e.flags = Envelope.FLAG_TRACEROUTE;
            m.body = "TRACEROUTE: " + BonfireAPI.API_ENDPOINT + "/traceroute?uuid=" + e.uuid.toString();
            appendMessage(m);
            Log.d(TAG, "sending tracert id " + e.uuid);
            ConnectionManager.sendEnvelope(MessagesActivity.this, e);
            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BonfireData.API_ENDPOINT + "/traceroute.php?uuid=" + e.uuid.toString())));
        } else if (id == R.id.action_select_protocol) {
            showSelectProtocol();
        } else if (id == R.id.action_show_debug) {
            String debug = NetworkOptions.getDebugInfo();
            Log.d("DEBUG", debug);
            InputBox.Info(this, "Debug", debug);
        }

        return super.onOptionsItemSelected(item);
    }


    public void showSelectProtocol() {
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(new CharSequence[]{"Auto-select", "Bluetooth", "Wifi", "Server"},
                        preferredProtocol,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        preferredProtocol = i;
                        dialogInterface.dismiss();
                    }
                })
                .setTitle("Select protocol")
        .show();
    }

    private Class getPreferredProtocol() {
        switch(preferredProtocol) {
            case 1: return BluetoothProtocol.class;
            case 3: return GcmProtocol.class;
            default: return null;
        }
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

    private void deleteSelectedItems() {
        BonfireData db = BonfireData.getInstance(this);
        boolean[] mySelected = adapter.itemSelected;

        for (int position = adapter.getCount() - 1; position >= 0; position--) {
            if (mySelected[position]) {
                db.deleteMessage(adapter.getItem(position));
                adapter.remove(adapter.getItem(position));
            }
        }

        adapter.notifyDataSetChanged();
    }

    private AbsListView.MultiChoiceModeListener multiChoiceListener = new AbsListView.MultiChoiceModeListener() {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            // Here you can do something when items are selected/de-selected,
            // such as update the title in the CAB
            adapter.itemSelected[position] = checked;
            adapter.notifyDataSetChanged();
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteSelectedItems();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate the menu for the CAB
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_messages_selected, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.itemSelected = new boolean[adapter.getCount()];
            adapter.notifyDataSetChanged();
        }
    };
}
