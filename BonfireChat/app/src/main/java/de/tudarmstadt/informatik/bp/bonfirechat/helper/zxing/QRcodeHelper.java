package de.tudarmstadt.informatik.bp.bonfirechat.helper.zxing;

import android.app.Activity;
import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.IPublicIdentity;

/**
 * Created by mw on 15.06.15.
 */
public class QRcodeHelper {

    public static void shareQRcode(Activity ctx, IPublicIdentity pubident) {
        try {
            String url = "bonfire://contact?name=" + URLEncoder.encode(pubident.getNickname(), "utf-8")
                    + "&jid=" + URLEncoder.encode(pubident.getXmppId())
                    + "&tel=" + URLEncoder.encode(pubident.getPhoneNumber())
                    + "&key=" + URLEncoder.encode(pubident.getPublicKey().asBase64());

            IntentIntegrator integrator = new IntentIntegrator(ctx);
            integrator.shareText(url);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static Contact contactFromUri(Uri url) {
        Contact contact = new Contact(
                url.getQueryParameter("name"), "", "", url.getQueryParameter("tel"),
                url.getQueryParameter("key"), url.getQueryParameter("jid"), "", "", 0);
        return contact;
    }

}
