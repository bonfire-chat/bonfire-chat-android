package de.wikilab.bonfirechat;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * conversations list
 */
public class ConversationsFragment extends Fragment {

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
        for (int i = 0; i < 10; i++) {
            Message[] messages = {
                    new Message("hallo")
            };
            conversationsListItems.add(new Conversation(
                    new Contact("Johannes Lauinger"),
                    Arrays.asList(messages)
            ));
        }
        final ConversationsAdapter adapter = new ConversationsAdapter(this.getActivity(), conversationsListItems);
        conversationsList.setAdapter(adapter);

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
            Toast.makeText(getActivity(), "Much conversations added.", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}