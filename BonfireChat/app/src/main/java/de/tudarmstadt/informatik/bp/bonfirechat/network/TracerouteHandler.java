package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;

import java.nio.charset.Charset;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Envelope;

/**
 * Created by mw on 16.06.15.
 */
public final class TracerouteHandler {

    private TracerouteHandler() { }

    public static void handleTraceroute(Context ctx, IProtocol protocol, String mode, Envelope envelope) {
        if (!envelope.hasFlag(Envelope.FLAG_TRACEROUTE)) {
            return;
        }
        final String traceMsg = mode + " via " + protocol.getClass().getSimpleName()
                + " by " + BonfireData.getInstance(ctx).getDefaultIdentity().getNickname() + "\n";
        envelope.encryptedBody = concatByteArrays(envelope.encryptedBody, traceMsg.getBytes(Charset.defaultCharset()));
    }

    public static byte[] concatByteArrays(byte[] a, byte[] b) {
        final int aLen = a.length;
        final int bLen = b.length;
        final byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }


}
