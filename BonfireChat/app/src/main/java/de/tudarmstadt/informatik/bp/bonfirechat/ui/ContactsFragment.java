package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.style.UpdateLayout;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.R;

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
    }


    ActionMode mActionMode;
    ListView contactsList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        contactsList = (ListView) rootView.findViewById(R.id.contactsList);
        contactsList.setAdapter(adapter);

        //contactsList.setOnItemLongClickListener(ContactClickedHandler);
        contactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("ContactsFragment", "itemClick: "+position+"    mActionMode="+mActionMode);
                if (mActionMode != null) {
                    adapter.itemSelected[position] = true;
                    adapter.notifyDataSetChanged();
                }
            }
        });
        contactsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null) {
                    return false;
                }

                Log.d("ContactsFragment", "itemLongClick: "+position);
                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = getActivity().startActionMode(mActionModeCallback);
                //view.setSelected(true);
                //contactsList.setSelection(position);
                adapter.itemSelected[position] = true;
                adapter.notifyDataSetChanged();
                return true;
            }
        });


        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached("contacts");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final BonfireData bonfireData = BonfireData.getInstance(adapter.getContext());

        if (item.getItemId() == R.id.action_add_contact) {
            final EditText input = new EditText(getActivity());
            input.setSingleLine();
            FrameLayout container = new FrameLayout(getActivity());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 60;
            params.rightMargin = 60;
            input.setLayoutParams(params);
            container.addView(input);
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.new_contact)
                    .setMessage(R.string.search_contact_by_name)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String name = input.getText().toString();
                            addContact(name);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // do nothing
                        }
                    })
                    .setIcon(R.mipmap.ic_launcher)
                    .setView(container)
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    void addContact(String name) {
        // TODO: insert sophisticated contact existing check
        if (!name.isEmpty()) {
            Contact contact = new Contact(name);
            adapter.add(contact);
            BonfireData.getInstance(getActivity()).createContact(contact);
        }
    }

    private void deleteSelectedItems() {
        BonfireData db = BonfireData.getInstance(getActivity());
        boolean[] mySelected = adapter.itemSelected;

        for (int position = adapter.getCount() - 1; position >= 0; position--) {
            if (mySelected[position]) {
                db.deleteContact(adapter.getObjects().get(position));
                adapter.remove(adapter.getObjects().get(position));
            }
        }

        adapter.notifyDataSetChanged();
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_contact, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteSelectedItems();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };


}