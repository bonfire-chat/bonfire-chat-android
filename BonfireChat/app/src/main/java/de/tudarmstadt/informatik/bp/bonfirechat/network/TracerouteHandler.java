package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Envelope;

/**
 * Created by mw on 16.06.15.
 */
public class TracerouteHandler {

    private static final String TAG = "TracerouteHandler";

    public static void handleTraceroute(Context ctx, IProtocol protocol, String mode, Envelope envelope) {
        if (!envelope.hasFlag(Envelope.FLAG_TRACEROUTE)) return;
        String traceMsg = mode + " via " + protocol.getClass().getSimpleName() + " by " + BonfireData.getInstance(ctx).getDefaultIdentity().getNickname() + "\n";
        envelope.encryptedBody = concatByteArrays(envelope.encryptedBody, traceMsg.getBytes());
    }

    public static byte[] concatByteArrays(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c= new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static void publishTraceroute(Envelope envelope) {
        if (!envelope.hasFlag(Envelope.FLAG_TRACEROUTE)) return;
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(BonfireData.API_ENDPOINT + "/traceroute").openConnection();
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=Je8PPsja_x");
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(("--Je8PPsja_x\r\nContent-Disposition: form-data; name=\"uuid\"\r\n\r\n" + envelope.uuid.toString() + "\r\n").getBytes("UTF-8"));
            out.write(("--Je8PPsja_x\r\nContent-Disposition: form-data; name=\"traceroute\"\r\n\r\n").getBytes("UTF-8"));
            out.write(envelope.encryptedBody);
            out.write(("\r\n").getBytes("UTF-8"));
            out.flush();

            String theString = StreamHelper.convertStreamToString(urlConnection.getInputStream());
            Log.i(TAG, "successfully published traceroute");
            Log.i(TAG, theString);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(urlConnection == null) urlConnection.disconnect();
        }
    }

}
