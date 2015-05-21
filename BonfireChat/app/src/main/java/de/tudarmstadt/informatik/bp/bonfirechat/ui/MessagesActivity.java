package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.InputBox;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;


public class MessagesActivity extends Activity {

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
        lv.setAdapter(new MessagesAdapter(this, messages));

        findViewById(R.id.textSendButton).setOnClickListener(onSendButtonClickListener);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        messages.add(new Message(intent.getStringExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_TEXT)
                                , Message.MessageDirection.Received
                        ));
                        ((MessagesAdapter)lv.getAdapter()).notifyDataSetChanged();
                    }
                },
                new IntentFilter(ConnectionManager.NEW_CONVERSATION_BROADCAST_EVENT));
    }

    private View.OnClickListener onSendButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText ed = (EditText) findViewById(R.id.textinput);
            String msg = ed.getText().toString();
            Message message = new Message(msg, Message.MessageDirection.Sent);
            db.createMessage(message, conversation);
            messages.add(message);
            ListView lv = (ListView) findViewById(R.id.messages_view);
            ((MessagesAdapter)lv.getAdapter()).notifyDataSetChanged();
            ed.setText("");
        }
    };


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
        }

        return super.onOptionsItemSelected(item);
    }
}
