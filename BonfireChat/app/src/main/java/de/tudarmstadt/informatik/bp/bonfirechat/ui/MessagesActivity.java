package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
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
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.UIHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.location.GpsTracker;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;
import de.tudarmstadt.informatik.bp.bonfirechat.network.Peer;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.LocationUdpPacket;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.TracerouteSegment;


public class MessagesActivity extends Activity {

    private static final String TAG = "MessagesActivity";
    List<Message> messages = new ArrayList<>();
    private MessagesAdapter adapter;
    private Conversation conversation;
    private final BonfireData db = BonfireData.getInstance(this);

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
        lv.setItemsCanFocus(true);

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
                Message theMessage = adapter.getItem(position);
                if (theMessage.hasFlag(Message.FLAG_IS_LOCATION)) {
                    Intent intent = new Intent(MessagesActivity.this, MessageLocationActivity.class);
                    Log.i(TAG, "starting MessageLocationActivity with message uuid=" + theMessage.uuid);
                    intent.putExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID, theMessage.uuid);
                    startActivity(intent);
                } else if (theMessage.hasFlag(Message.FLAG_IS_FILE)) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri imageUri = Uri.fromFile(theMessage.getImageFile());
                    Log.i(TAG, "opening image in system viewer: "+imageUri.toString());
                    intent.setDataAndType(imageUri, "image/*");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MessagesActivity.this, MessageDetailsActivity.class);
                    Log.i(TAG, "starting MessageDetailsActivity with message uuid=" + theMessage.uuid);
                    intent.putExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID, theMessage.uuid);
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
                                }
                                // set retransmission count for UI
                                m.retransmissionCount = intent.getIntExtra(ConnectionManager.EXTENDED_DATA_RETRANSMISSION_COUNT, m.retransmissionCount);
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
                                // update
                                Message newMessage = db.getMessageByUUID(m.uuid);
                                m.flags = newMessage.flags;
                                ((MessagesAdapter) lv.getAdapter()).notifyDataSetChanged();
                                return;
                            }
                        }
                    }
                },
                new IntentFilter(ConnectionManager.MSG_ACKED_BROADCAST_EVENT));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // reload messages (possibly changed on disk)
        messages = db.getMessages(conversation);
        adapter.clear();
        adapter.addAll(messages);
        adapter.notifyDataSetChanged();
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
                    showcaseView.setContentTitle(getString(R.string.tutorial_messages_change_title));
                    showcaseView.setContentText(getString(R.string.tutorial_messages_change_title_desc));
                    showcaseView.setTarget(new ActionItemTarget(MessagesActivity.this, R.id.action_edit_title));
                    showcaseView.setButtonText(getString(R.string.next));
                    break;
                case 1:
                    showcaseView.setContentTitle(getString(R.string.tutorial_messages_share_image));
                    showcaseView.setContentText(getString(R.string.tutorial_messages_share_image_desc));
                    showcaseView.setTarget(new ActionItemTarget(MessagesActivity.this, R.id.action_share_image));
                    showcaseView.setButtonText(getString(R.string.next));
                    break;
                case 2:
                    showcaseView.setContentTitle(getString(R.string.tutorial_messages_share_location));
                    showcaseView.setContentText(getString(R.string.tutorial_messages_share_location_desc));
                    showcaseView.setTarget(new ActionItemTarget(MessagesActivity.this, R.id.action_share_location));
                    showcaseView.setButtonText(getString(R.string.next));
                    break;
                case 3:
                    showcaseView.setContentTitle(getString(R.string.tutorial_messages_details));
                    showcaseView.setContentText(getString(R.string.tutorial_messages_details_desc));
                    showcaseView.setTarget(new PointTarget(200, 600));
                    showcaseView.setButtonText(getString(R.string.got_it));
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

            db.createMessage(message, conversation);
            appendMessage(message);

            Log.d(TAG, "sending message id " + message.uuid);
            ConnectionManager.sendMessage(MessagesActivity.this, message);

            // store initial traceroute
            db.updateMessage(message);
        }
    };

    private void appendMessage(Message message) {
        messages.add(message);
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

        } else if (id == R.id.action_send_location_udp) {
            GpsTracker gps = GpsTracker.getInstance();
            if (gps.canGetLocation()) {
                LocationUdpPacket p = new LocationUdpPacket(db.getDefaultIdentity(), conversation.getPeer().getPublicKey().asByteArray(), gps.getLatitude(), gps.getLongitude());
                ConnectionManager.sendLocationUdpPacket(this, p);
            } else {
                Toast toast = Toast.makeText(this, R.string.toast_location_not_available, Toast.LENGTH_SHORT);
                toast.show();
            }

        } else if (id == R.id.action_show_contact_location) {
            // location available?
            if (conversation.getPeer().getLastKnownLocation() != null) {
                Intent intent = new Intent(MessagesActivity.this, ContactLocationActivity.class);
                Log.i(TAG, "starting ContactLocationActivity with contact id=" + conversation.getPeer().rowid);
                intent.putExtra(ConnectionManager.EXTENDED_DATA_CONTACT_ID, conversation.getPeer().rowid);
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(this, R.string.toast_location_not_available, Toast.LENGTH_SHORT);
                toast.show();
            }

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
        if (requestCode == 42 && data.getData() != null) {
            Log.d(TAG, "result from image picker: "+data.getData().toString());
            UUID uuid = UUID.randomUUID();

            File file = Message.getImageFile(uuid);

            try {
                FileOutputStream out = new FileOutputStream(file);
                StreamHelper.writeImageToStream(this.getContentResolver(), data.getData(), out);
                out.close();

                Message message = new Message(file.getAbsolutePath(), db.getDefaultIdentity(), new Date(), Message.FLAG_IS_FILE | Message.FLAG_ENCRYPTED, uuid, conversation.getPeer(), new ArrayList<TracerouteSegment>());

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

    private void resendSelectedItems() {
        boolean[] mySelected = adapter.itemSelected;
        boolean receivedMessagesInSelection = false;

        for (int position = adapter.getCount() - 1; position >= 0; position--) {
            if (mySelected[position]) {
                Message adapterMessage = adapter.getItem(position);
                // is it a received message?
                if (!adapterMessage.sender.getPublicKey().equals(db.getDefaultIdentity().getPublicKey())) {
                    // don't resend that
                    receivedMessagesInSelection = true;
                    continue;
                }
                for (Message message : messages) {
                    // correct message?
                    if (message.uuid.equals(adapterMessage.uuid)) {
                        ConnectionManager.retrySendMessage(this, message);
                        db.updateMessage(message);
                    }
                }
            }
        }
        adapter.clear();
        adapter.addAll(messages);
        adapter.notifyDataSetChanged();

        // notify the user that received messages can't be resent, in case he intended to do so
        if (receivedMessagesInSelection) {
            Toast.makeText(this, R.string.cant_resend_received_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void detailsForSelectedItem() {
        boolean[] mySelected = adapter.itemSelected;

        for (int position = adapter.getCount() - 1; position >= 0; position--) {
            if (mySelected[position]) {
                Intent intent = new Intent(MessagesActivity.this, MessageDetailsActivity.class);
                Log.i(TAG, "starting MessageDetailsActivity with message uuid=" + adapter.getItem(position).uuid);
                intent.putExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID, adapter.getItem(position).uuid);
                startActivity(intent);
                // show details only for one message
                break;
            }
        }
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
                case R.id.action_retry:
                    resendSelectedItems();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.action_details:
                    detailsForSelectedItem();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
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
