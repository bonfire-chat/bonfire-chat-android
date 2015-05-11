package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.R;

import static android.widget.AdapterView.*;

/**
 * conversations list
 */
public class ConversationsFragment extends Fragment {

    private ConversationsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.conversations, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_conversations, container, false);

        final ListView conversationsList = (ListView) rootView.findViewById(R.id.conversationsList);
        final ArrayList<Conversation> conversationsListItems = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            Message[] messages = {
                    new Message("hallo", Message.MessageDirection.Received)
            };
            conversationsListItems.add(new Conversation(
                    new Contact("Johannes Lauinger"),
                    Arrays.asList(messages)
            ));
        }
        adapter = new ConversationsAdapter(this.getActivity(), BonfireData.getInstance(getActivity()).getConversations());
        conversationsList.setAdapter(adapter);
        conversationsList.setOnItemClickListener(itemClickListener);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached("conversations");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.getItemId() == R.id.action_add_conversation) {
            Toast.makeText(getActivity(), "Conversation added.", Toast.LENGTH_SHORT).show();
            Message[] messages = {
                    new Message("hallo", Message.MessageDirection.Received)
            };
            Conversation myConversation = new Conversation( new Contact("Johnny Lauinger" + new Random().nextInt()), Arrays.asList(messages));
            adapter.add(myConversation);
            BonfireData.getInstance(getActivity()).createConversation(myConversation);
            for(Message message : messages)
                BonfireData.getInstance(getActivity()).createMessage(message, myConversation);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent i = new Intent(getActivity(), MessagesActivity.class);
            startActivity(i);
        }
    };
}