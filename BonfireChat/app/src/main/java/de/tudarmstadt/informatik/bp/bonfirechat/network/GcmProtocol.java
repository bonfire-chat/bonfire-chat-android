package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireAPI;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Envelope;

/**
 * Created by mw on 15.06.15.
 */
public class GcmProtocol extends SocketProtocol {
    private static final String TAG = "GcmProtocol";

    //FIXME TODO HACK
    public static GcmProtocol instance;

    public GcmProtocol(Context context) {
        instance = this;
    }


    public void onHandleGcmIntent(Intent intent) {
        try {
            final String dataString = intent.getStringExtra("msg");
            Log.i("GcmProtocol", "onHandleGcmIntent: "+ dataString);
            //byte[] data = dataString.getBytes("ascii");
            //Log.i("GcmProtocol", "onHandleGcmIntent: "+ StreamHelper.byteArrayToHexString(data));
            final ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(dataString, Base64.DEFAULT));
            final Envelope envelope = receiveEnvelope(bais);

            listener.onMessageReceived(this, envelope);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("GcmProtocol", "Unable to deserialize: "+e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(Envelope envelope) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            sendEnvelope(out, envelope);

            BonfireAPI.sendGcmMessage(envelope.recipientPublicKey, out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean canSend() {
        return true;
    }
}
