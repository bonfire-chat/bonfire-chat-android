package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;

/**
 * Created by johannes on 30.08.15.
 */
public class ContactLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "ContactLocationActivity";
    private final BonfireData db = BonfireData.getInstance(this);
    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_contact_location);
        long contactId = getIntent().getLongExtra(ConnectionManager.EXTENDED_DATA_CONTACT_ID, -1);
        contact = db.getContactById(contactId);
        if (contact == null) {
            Log.e(TAG, "Error, contact with id " + contactId + " not found");
        }
        getActionBar().setTitle(contact.getNickname());

        // display map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // makes sure parameters like conversation id are present in parent activity
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
