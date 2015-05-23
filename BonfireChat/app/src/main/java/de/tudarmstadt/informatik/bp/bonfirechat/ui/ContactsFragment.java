package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.text.style.UpdateLayout;
import android.util.JsonReader;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.content.Context;
import android.widget.SearchView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.InputBox;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;

/**
 * contacts list
 */
public class ContactsFragment extends Fragment {

    private ContactsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        BonfireData db = BonfireData.getInstance(getActivity());
        List<Contact> contacts = db.getContacts();
        adapter = new ContactsAdapter(this.getActivity(), contacts);

    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contacts, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

    }


    ActionMode mActionMode;
    ListView contactsList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        contactsList = (ListView) rootView.findViewById(R.id.contactsList);
        contactsList.setAdapter(adapter);

        contactsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        contactsList.setMultiChoiceModeListener(multiChoiceListener);

        contactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = adapter.getItem(position);
                Intent intent = new Intent(getActivity(), ContactDetailsActivity.class);
                intent.putExtra(ContactDetailsActivity.EXTRA_CONTACT_ID, contact.rowid);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final BonfireData bonfireData = BonfireData.getInstance(adapter.getContext());

        if (item.getItemId() == R.id.action_add_contact) {
            Contact c = new Contact("");
            bonfireData.createContact(c);
            Intent intent = new Intent(this.getActivity(), ContactDetailsActivity.class);
            intent.putExtra(ContactDetailsActivity.EXTRA_CONTACT_ID, c.rowid);
            startActivity(intent);
            return true;
/*
        } else if (item.getItemId() == R.id.action_search_contact) {
            InputBox.InputBox(getActivity(), getString(R.string.new_contact), getString(R.string.search_contact_by_name), "",
            new InputBox.OnOkClickListener() {
                @Override
                public void onOkClicked(String input) {
                    contactSearch.execute(input);
                }
            });
            return true;*/
        }

        return super.onOptionsItemSelected(item);
    }



    AsyncTask<String, Object, Contact> contactSearch = new AsyncTask<String, Object, Contact>() {
        @Override
        protected Contact doInBackground(String... params) {


            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://bonfire.teamwiki.net/search.php?nickname=%"+params[0]+"%");
            try {
                HttpResponse response = httpclient.execute(httppost);
                java.util.Scanner s = new java.util.Scanner(response.getEntity().getContent()).useDelimiter("\\A");
                JSONTokener tokener = new JSONTokener(s.next());
                JSONArray array = (JSONArray) tokener.nextValue();
/*
                Contact contact = new Contact();
                adapter.add(contact);
                BonfireData.getInstance(getActivity()).createContact(contact);
*/

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    };


    private void deleteSelectedItems() {
        BonfireData db = BonfireData.getInstance(getActivity());
        boolean[] mySelected = adapter.itemSelected;

        for (int position = adapter.getCount() - 1; position >= 0; position--) {
            if (mySelected[position]) {
                db.deleteContact(adapter.getItem(position));
                adapter.remove(adapter.getItem(position));
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void createConversationWithSelectedItems() {
        BonfireData db = BonfireData.getInstance(getActivity());
        boolean[] mySelected = adapter.itemSelected;

        for (int position = adapter.getCount() - 1; position >= 0; position--) {
            if (mySelected[position]) {
                Conversation conversation = new Conversation(adapter.getItem(position), adapter.getItem(position).getNickname(), 0);
                db.createConversation(conversation);
                Intent i = new Intent(getActivity(), MessagesActivity.class);
                Log.i("ConversationsFragment", "starting MessagesActivity with ConversationId=" + conversation.rowid);
                i.putExtra("ConversationId", conversation.rowid);
                startActivity(i);
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
                case R.id.action_delete:
                    deleteSelectedItems();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.action_create_conversation:
                    createConversationWithSelectedItems();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate the menu for the CAB
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_contact, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.itemSelected = new boolean[adapter.getCount()];
            adapter.notifyDataSetChanged();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here you can perform updates to the CAB due to
            // an invalidate() request
            return false;
        }
    };


}