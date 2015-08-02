package de.tudarmstadt.informatik.bp.bonfirechat.data;

import android.util.Log;

import org.abstractj.kalium.keys.PublicKey;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Envelope;

/**
 * Created by mw on 29.07.15.
 */
public class BonfireAPI {

    private static final String TAG = "BonfireAPI";

    /**
     * URL of the rendezvous server API endpoint
     */
    public static final String API_ENDPOINT = "https://bonfire.projects.teamwiki.net/api/v1";
    public static final PublicKey SERVER_PUBLICKEY = new PublicKey("7c2bbc4c4d292479de59a1168f3b102ac9869b9ee00beb92745571e36bbb0b43");

    public static final String METHOD_TRACEROUTE = "traceroute";
    public static final String METHOD_SEND_MESSAGE = "notify";

    public static String httpPost(String apiMethod, Hashtable<String, byte[]> body) throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(API_ENDPOINT + "/" + apiMethod).openConnection();
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=Je8PPsja_x");
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            final BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            for (Map.Entry<String, byte[]> part : body.entrySet()) {
                out.write(("--Je8PPsja_x\r\nContent-Disposition: form-data; name=\"" + part.getKey() + "\"\r\n\r\n").getBytes("UTF-8"));
                out.write(part.getValue());
                out.write(("\r\n").getBytes("UTF-8"));
            }
            out.flush();

            final String theString = StreamHelper.convertStreamToString(urlConnection.getInputStream());
            Log.i(TAG, "successful HTTP Post request to "+apiMethod);
            Log.i(TAG, theString);
            return theString;
        } catch (IOException e) {
            String theErrMes = StreamHelper.convertStreamToString(urlConnection.getErrorStream());
            throw new IOException("HTTP Post request failed, Exception: "+e.toString()+", Body: "+theErrMes);
        } finally {
            if(urlConnection == null) urlConnection.disconnect();
        }
    }
    public static byte[] encode(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException("This device does not support UTF-8, go away!");
        }
    }


    public static void publishTraceroute(Envelope envelope) {
        try {
            if (!envelope.hasFlag(Envelope.FLAG_TRACEROUTE)) return;

            Hashtable<String, byte[]> body = new Hashtable<>();
            body.put("uuid", encode(envelope.uuid.toString()));
            body.put("traceroute", envelope.encryptedBody);
            httpPost(METHOD_TRACEROUTE, body);
        } catch (IOException e) {
            Log.e(TAG, "Failed to publish traceroute, ignoring!");
            e.printStackTrace();
        }
    }


    public static void sendGcmMessage(Identity identity, byte[] targetPubkey, String nextHop, byte[] serializedEnvelope) throws IOException {
        String key = CryptoHelper.toBase64(targetPubkey);

        Hashtable<String, byte[]> body = new Hashtable<>();
        body.put("senderId", encode(String.valueOf(identity.getServerUid())));
        body.put("recipientPublicKey", encode(key));
        body.put("nextHopId", encode(nextHop));
        body.put("msg", serializedEnvelope);
        httpPost(METHOD_SEND_MESSAGE, body);
    }


}
