package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.content.Context;

import java.util.ArrayList;

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
        adapter = new ContactsAdapter(this.getActivity(), new ArrayList<Contact>());
        BonfireData bonfireData = BonfireData.getInstance(adapter.getContext());
        for(Contact contact : bonfireData.getContacts()){
            adapter.add(contact);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contacts, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        final ListView contactsList = (ListView) rootView.findViewById(R.id.contactsList);
        contactsList.setAdapter(adapter);

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
                            // TODO: insert sophisticated contact existing check
                            if (!name.isEmpty()) {
                                Contact contact = new Contact(name);
                                bonfireData.createContact(contact);
                                adapter.add(contact);
                            }
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
}