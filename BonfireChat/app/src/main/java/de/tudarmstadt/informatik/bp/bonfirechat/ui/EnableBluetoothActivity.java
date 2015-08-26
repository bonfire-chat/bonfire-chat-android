package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;

/**
 * Created by mw on 26.08.15.
 */
public class EnableBluetoothActivity extends Activity {

    private static final String TAG = "EnableBluetoothActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enablebluetooth);

        ((Button)findViewById(R.id.enable_bluetooth)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEnableBluetoothUI();
            }
        });

        showEnableBluetoothUI();
    }

    private void showEnableBluetoothUI() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        //discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(discoverableIntent, 42);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42) {
            Log.i(TAG, "Bluetooth request returned resultCode=" + resultCode);

            if (resultCode == 1 ) {
                final Intent intent = new Intent(this, ConnectionManager.class);
                intent.setAction(ConnectionManager.CONTINUE_BLUETOOTH_STARTUP_ACTION);
                startService(intent);

                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
