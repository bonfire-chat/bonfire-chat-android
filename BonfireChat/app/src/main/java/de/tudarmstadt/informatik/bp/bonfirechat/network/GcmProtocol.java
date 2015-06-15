package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Envelope;
import de.tudarmstadt.informatik.bp.bonfirechat.network.gcm.GcmBroadcastReceiver;

/**
 * Created by mw on 15.06.15.
 */
public class GcmProtocol extends SocketProtocol {
    private static final String TAG = "GcmProtocol";

    URL myEndpointUrl;
    public GcmProtocol(Context context) throws MalformedURLException {
        myEndpointUrl = new URL(BonfireData.API_ENDPOINT + "/notify.php");
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
                out.write(("--Je8PPsja_x\r\nContent-Disposition: form-data; name=\"publickey\"\r\n\r\n" + key + "\r\n").getBytes("UTF-8"));
            }
            out.write(("--Je8PPsja_x\r\nContent-Disposition: form-data; name=\"msg\"\r\n\r\n").getBytes("UTF-8"));
            sendEnvelope(out, envelope);
            out.write(("\r\n").getBytes("UTF-8"));

            //BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getInputStream())));
            String theString = StreamHelper.convertStreamToString(urlConnection.getInputStream());
            Log.i(TAG, "successful sent message");
            Log.i(TAG, theString);

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error sending message: " + e.toString());
        } finally {
            if(urlConnection == null) urlConnection.disconnect();
        }

    }

    @Override
    public boolean canSend() {
        return true;
    }
}
