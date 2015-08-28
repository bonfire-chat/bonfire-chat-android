package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
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

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionItemTarget;
import com.github.amlcurran.showcaseview.targets.PointTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.data.ConstOptions;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.UIHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.location.GpsTracker;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.network.BluetoothProtocol;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;
import de.tudarmstadt.informatik.bp.bonfirechat.network.GcmProtocol;
import de.tudarmstadt.informatik.bp.bonfirechat.network.Peer;


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

        // show tutorial on first use
        if (UIHelper.shouldShowConversationTutorial(this)) {
            // make sure the keyboard is hidden during tutorial
            ((EditText) findViewById(R.id.textinput)).setInputType(InputType.TYPE_NULL);
            showcaseView = new ShowcaseView.Builder(this)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .setOnClickListener(showcaseListener)
                    .build();
            showcaseListener.onClick(null);
        }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(!messages.get(position).hasFlag(Message.FLAG_IS_LOCATION)) {
                    Intent intent = new Intent(MessagesActivity.this, MessageDetailsActivity.class);
                    Log.i(TAG, "starting MessageDetailsActivity with message uuid=" + adapter.getItem(position).uuid);
                    intent.putExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID, adapter.getItem(position).uuid);
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(MessagesActivity.this, MessageLocationActivity.class);
                    Log.i(TAG, "starting MessageLocationActivity with message uuid=" + adapter.getItem(position).uuid);
                    intent.putExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID, adapter.getItem(position).uuid);
                    startActivity(intent);
                }
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

    // ####### First-Start Tutorial #####################################################
    private ShowcaseView showcaseView;
    private int tutorial_counter = 0;
    /**
     * Handles clicks on Close button of first-start tutorial view
     */
    private View.OnClickListener showcaseListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(tutorial_counter++) {
                case 0:
                    showcaseView.setContentTitle("Titel ändern");
                    showcaseView.setContentText("Hier klicken, um den Titel dieser Unterhaltung zu ändern");
                    showcaseView.setTarget(new ActionItemTarget(MessagesActivity.this, R.id.action_edit_title));
                    showcaseView.setButtonText("Weiter");
                    break;
                case 1:
                    showcaseView.setContentTitle("Bild verschicken");
                    showcaseView.setContentText("Klicke hier, um ein Bild zu versenden");
                    showcaseView.setTarget(new ActionItemTarget(MessagesActivity.this, R.id.action_share_image));
                    showcaseView.setButtonText("Weiter");
                    break;
                case 2:
                    showcaseView.setContentTitle("Standort teilen");
                    showcaseView.setContentText("Klicke hier, um deinen Standort zu übertragen");
                    showcaseView.setTarget(new ActionItemTarget(MessagesActivity.this, R.id.action_share_location));
                    showcaseView.setButtonText("Weiter");
                    break;
                case 3:
                    showcaseView.setContentTitle("Übrigens");
                    showcaseView.setContentText("Klicke auf eine Nachricht, um Details zur Übertragung anzusehen.");
                    showcaseView.setTarget(new PointTarget(200, 600));
                    showcaseView.setButtonText("Alles klar");
                    break;
                case 4:
                    UIHelper.flagShownConversationTutorial(MessagesActivity.this);
                    // reenable keyboard after tutorial
                    ((EditText) findViewById(R.id.textinput)).setInputType(InputType.TYPE_CLASS_TEXT);
                    findViewById(R.id.textinput).requestFocus();
                    showcaseView.hide();
                    break;
            }
        }
    };
    // ####### End First-Start Tutorial #####################################################

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
            UIHelper.Info(this, "Info", info);
            return true;

        } else if (id == R.id.action_edit_title) {
            UIHelper.InputBox(this, getString(R.string.action_edit_title), "",
                    conversation.getName(),
                    new UIHelper.OnOkClickListener() {
                        @Override
                        public void onOkClicked(String input) {
                            conversation.title = input;
                            getActionBar().setTitle(conversation.title);
                            BonfireData.getInstance(MessagesActivity.this).updateConversation(conversation);
                        }
                    });
            return true;

        } else if (id == R.id.action_set_path) {
            showSelectPath();

        } else if (id == R.id.action_clear_path) {
            ConnectionManager.routingManager.clearPath(conversation.getPeer().getPublicKey().asByteArray());

        } else if (id == R.id.action_share_image) {
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 42);

        } else if (id == R.id.action_show_debug) {
            String debug = ConstOptions.getDebugInfo();
            Log.d("DEBUG", debug);
            UIHelper.Info(this, "Debug", debug);

        } else if (id == R.id.action_share_location) {
            GpsTracker gps = GpsTracker.getInstance();
            if (gps.canGetLocation()) {
                Log.d("Location", gps.getLatitude() + " : " + gps.getLongitude());
                Message message = new Message(gps.getLatitude() + ":" + gps.getLongitude(), db.getDefaultIdentity(), new Date(), Message.FLAG_IS_LOCATION | Message.FLAG_ENCRYPTED, conversation.getPeer());
                message.error = "Sending";

                db.createMessage(message, conversation);
                appendMessage(message);

                Log.d(TAG, "sending message id " + message.uuid);
                ConnectionManager.sendMessage(MessagesActivity.this, message);
            }
            else {
                Toast toast = Toast.makeText(this, R.string.toast_location_not_available, Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42) {
            Log.d(TAG, "result from image picker: "+data.getData().toString());
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "Bonfire Images\\" + (new Date()).getTime() + ".jpg");

            try {
                FileOutputStream out = new FileOutputStream(file);
                StreamHelper.writeImageToStream(this.getContentResolver(), data.getData(), out);
                out.close();

                Message message = new Message(file.getAbsolutePath(), db.getDefaultIdentity(), new Date(), Message.FLAG_IS_FILE | Message.FLAG_ENCRYPTED, conversation.getPeer());
                message.error = "Sending";

                db.createMessage(message, conversation);
                appendMessage(message);

                Log.d(TAG, "sending message id " + message.uuid);
                ConnectionManager.sendMessage(MessagesActivity.this, message);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showSelectPath() {
        CharSequence[] peerList;
        synchronized (ConnectionManager.peers) {
            peerList = new CharSequence[ConnectionManager.peers.size()];
            for(int i = 0; i < ConnectionManager.peers.size(); i++) {
                peerList[i] = Peer.formatMacAddress(ConnectionManager.peers.get(i).getAddress());
            }
        }
        final CharSequence[] peerList2 = peerList;
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(peerList,
                        -1,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ConnectionManager.routingManager.overridePath(
                                conversation.getPeer().getPublicKey().asByteArray(),
                                Peer.addressFromString(peerList2[i].toString())
                        );
                        dialogInterface.dismiss();
                    }
                })
                .setTitle(getString(R.string.action_set_path))
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
