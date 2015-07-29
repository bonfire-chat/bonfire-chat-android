package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Envelope;

/**
 * Created by mw on 15.06.15.
 */
public class GcmProtocol extends SocketProtocol {
    private static final String TAG = "GcmProtocol";

    //FIXME TODO HACK
    public static GcmProtocol instance;

    URL myEndpointUrl;
    public GcmProtocol(Context context) throws MalformedURLException {
        myEndpointUrl = new URL(BonfireData.API_ENDPOINT + "/notify");
        instance = this;
    }


    public void onHandleGcmIntent(Intent intent) {
        try {
            String dataString = intent.getStringExtra("msg");
            Log.i("GcmProtocol", "onHandleGcmIntent: "+ dataString);
            //byte[] data = dataString.getBytes("ascii");
            //Log.i("GcmProtocol", "onHandleGcmIntent: "+ StreamHelper.byteArrayToHexString(data));
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(dataString, Base64.DEFAULT));
            Envelope envelope = receiveEnvelope(bais);

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
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) myEndpointUrl.openConnection();
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=Je8PPsja_x");
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            for(byte[] pubkeyBytes : envelope.recipientsPublicKeys) {
                String key = CryptoHelper.toBase64(pubkeyBytes);
                out.write(("--Je8PPsja_x\r\nContent-Disposition: form-data; name=\"publickey[]\"\r\n\r\n" + key + "\r\n").getBytes("UTF-8"));
            }
            out.write(("--Je8PPsja_x\r\nContent-Disposition: form-data; name=\"msg\"\r\n\r\n").getBytes("UTF-8"));
            sendEnvelope(out, envelope);
            out.write(("\r\n").getBytes("UTF-8"));
            out.flush();

            //BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getInputStream())));
            String theString = StreamHelper.convertStreamToString(urlConnection.getInputStream());
            Log.i(TAG, "successfully sent message");
            Log.i(TAG, theString);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error sending message: " + e.toString());
            String theErrMes = StreamHelper.convertStreamToString(urlConnection.getErrorStream());
            throw new RuntimeException("Send Error: "+theErrMes);
        } finally {
            if(urlConnection == null) urlConnection.disconnect();
        }

    }

    @Override
    public boolean canSend() {
        return true;
    }
}
