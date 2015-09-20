package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.zxing.QRcodeHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;

public class ContactDetailsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String EXTRA_CONTACT_ID = "ContactId";

    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact_details);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.contact));

        BonfireData db = BonfireData.getInstance(this);
        Intent in = getIntent();
        if (in.hasExtra(EXTRA_CONTACT_ID)) {
            contact = db.getContactById(getIntent().getLongExtra(EXTRA_CONTACT_ID, -1));

        } else if (in.getAction().equals(Intent.ACTION_VIEW) && in.getData().getScheme().equals("bonfire")) {
            Uri url = in.getData();

            contact = QRcodeHelper.contactFromUri(url);
            db.createContact(contact);

        } else {
            Log.e("ContactDetailsAct", "invalid intent: " + getIntent().toString());
            finish();
        }

        getEdit(R.id.nickname).setText(contact.getNickname());
        getEdit(R.id.phone).setText(contact.phoneNumber);
        ((CheckBox) findViewById(R.id.share_location)).setChecked(contact.isShareLocation());
        ((TextView) findViewById(R.id.stats)).setText("coming soon");

        String pubkey = contact.getPublicKey().asBase64();
        pubkey = pubkey.substring(0,21) + "\n" + pubkey.substring(22);
        ((TextView) findViewById(R.id.publickey)).setText(pubkey);

        if (contact.getLastKnownLocation() != null) {
            // hide notice
            findViewById(R.id.group_map).setVisibility(View.VISIBLE);
            findViewById(R.id.no_location_for_contact).setVisibility(View.GONE);
            // display map
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            // fix scroll issue
            fixMapScrolling();
        }
        else {
            // show notice
            findViewById(R.id.group_map).setVisibility(View.GONE);
            findViewById(R.id.no_location_for_contact).setVisibility(View.VISIBLE);
        }
    }

    private EditText getEdit(int id) {
        return ((EditText) findViewById(id));
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
            saveContact();
            finish();
            return true;
        }
        else if (id == R.id.action_create_conversation) {
            // save contact before starting conversation, to prevent data loss on wrong button click
            saveContact();
            MessagesActivity.startConversationWithPeer(this, contact);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveContact() {
        contact.setNickname(getEdit(R.id.nickname).getText().toString());
        contact.phoneNumber = getEdit(R.id.phone).getText().toString();
        contact.setShareLocation(((CheckBox) findViewById(R.id.share_location)).isChecked());
        BonfireData db = BonfireData.getInstance(this);
        db.updateContact(contact);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // extract position from message
        LatLng position = contact.getLastKnownLocation();

        // set map position
        CameraUpdate center = CameraUpdateFactory.newLatLng(position);
        map.moveCamera(center);

        // show position marker
        map.addMarker(new MarkerOptions()
                .position(position)
                .title(contact.getNickname()));
    }

    private void fixMapScrolling() {
        final ScrollView mainScrollView = (ScrollView) findViewById(R.id.scrollView);
        final View transparentView = findViewById(R.id.transparent);

        transparentView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                transparentView.performClick();
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });
    }
}
