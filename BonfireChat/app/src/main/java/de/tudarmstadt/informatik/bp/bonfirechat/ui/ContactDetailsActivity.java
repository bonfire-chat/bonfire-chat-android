package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;

public class ContactDetailsActivity extends Activity {


    public static final String EXTRA_CONTACT_ID = "ContactId";

    Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("Kontakt");

        BonfireData db = BonfireData.getInstance(this);
        contact = db.getContactById(getIntent().getLongExtra(EXTRA_CONTACT_ID, -1));

        getEdit(R.id.txt_nickname).setText(contact.getNickname());
        getEdit(R.id.txt_xmppId).setText(contact.getXmppId());
        getEdit(R.id.txt_publicKey).setText(contact.publicKey);
        getEdit(R.id.txt_phoneNumber).setText(contact.phoneNumber);

    }

    private EditText getEdit(int id) {
        return ((EditText)findViewById(id));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {

            contact.setNickname(getEdit(R.id.txt_nickname).getText().toString());
            contact.setXmppId(getEdit(R.id.txt_xmppId).getText().toString());
            contact.publicKey = getEdit(R.id.txt_publicKey).getText().toString();
            contact.phoneNumber = getEdit(R.id.txt_phoneNumber).getText().toString();
            BonfireData db = BonfireData.getInstance(this);
            db.updateContact(contact);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
