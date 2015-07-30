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
        final String traceMsg = mode + " via " + protocol.getClass().getSimpleName() + " by " + BonfireData.getInstance(ctx).getDefaultIdentity().getNickname() + "\n";
        envelope.encryptedBody = concatByteArrays(envelope.encryptedBody, traceMsg.getBytes());
    }

    public static byte[] concatByteArrays(byte[] a, byte[] b) {
        final int aLen = a.length;
        final int bLen = b.length;
        final byte[] c= new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }


}
