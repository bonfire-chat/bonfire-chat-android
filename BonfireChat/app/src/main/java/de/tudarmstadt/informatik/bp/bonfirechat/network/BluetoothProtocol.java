package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;
import android.util.Log;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by johannes on 22.05.15.
 */
public class BluetoothProtocol implements IProtocol {

    private static final String TAG = "BluetoothProtocol";
    private Identity identity;
    private OnMessageReceivedListener listener;
    private Context ctx;

    BluetoothProtocol(Context ctx) {
        this.ctx = ctx;
    }

    // ###########################################################################
    // ###    Implementation of IProtocol
    // ###########################################################################

    @Override
    public void sendMessage(Contact target, Message message) {
        Log.d(TAG, "broadcasting message via Bluetooth");
    }

    @Override
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    @Override
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }
}
