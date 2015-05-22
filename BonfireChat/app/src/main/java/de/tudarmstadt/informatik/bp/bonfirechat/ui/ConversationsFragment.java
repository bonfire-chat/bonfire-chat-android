package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;

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

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        adapter.add(BonfireData.getInstance(getActivity()).getConversationById(
                                intent.getLongExtra(ConnectionManager.EXTENDED_DATA_CONVERSATION_ID, -1)
                        ));
                        adapter.notifyDataSetChanged();
                    }
                },
                new IntentFilter(ConnectionManager.NEW_CONVERSATION_BROADCAST_EVENT));
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

        adapter = new ConversationsAdapter(this.getActivity(), BonfireData.getInstance(getActivity()).getConversations());
        conversationsList.setAdapter(adapter);
        conversationsList.setOnItemClickListener(itemClickListener);
        conversationsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        conversationsList.setMultiChoiceModeListener(multiChoiceListener);
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
/*
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
        }*/

        return super.onOptionsItemSelected(item);
    }

    public OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent i = new Intent(getActivity(), MessagesActivity.class);
            Log.i("ConversationsFragment", "starting MessagesActivity with ConversationId=" + adapter.getItem(position).rowid);
            i.putExtra("ConversationId", adapter.getItem(position).rowid);
            startActivity(i);
        }
    };

    private void deleteSelectedItems() {
        BonfireData db = BonfireData.getInstance(getActivity());
        boolean[] mySelected = adapter.itemSelected;

        for (int position = adapter.getCount() - 1; position >= 0; position--) {
            if (mySelected[position]) {
                db.deleteConversation(adapter.getItem(position));
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
            inflater.inflate(R.menu.menu_conversation, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Here you can make any necessary updates to the activity when
            // the CAB is removed. By default, selected items are deselected/unchecked.
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here you can perform updates to the CAB due to
            // an invalidate() request
            return false;
        }
    };


}