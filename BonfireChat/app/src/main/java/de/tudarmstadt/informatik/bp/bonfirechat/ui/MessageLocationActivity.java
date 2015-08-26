package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;

/**
 * Created by johannes on 26.08.15.
 */
public class MessageLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MessageLocationActivity";
    private final BonfireData db = BonfireData.getInstance(this);
    private Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_message_location);
        UUID msgUuid = (UUID) getIntent().getSerializableExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID);
        message = db.getMessageByUUID(msgUuid);
        if (message == null) {
            Log.e(TAG, "Error, message with id " + msgUuid + " not found");
        }
        getActionBar().setTitle(R.string.message_location);

        // display map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // extract position from message
        String[] coords = message.body.split(":");
        LatLng position = new LatLng(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));

        // show position marker
        map.addMarker(new MarkerOptions()
                .position(position)
                .title(message.sender.getNickname()));
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
