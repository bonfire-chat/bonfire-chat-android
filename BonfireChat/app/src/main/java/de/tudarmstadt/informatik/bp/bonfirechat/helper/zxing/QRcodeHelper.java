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
public final class QRcodeHelper {

    private QRcodeHelper() { }

    public static void shareQRcode(Activity ctx, IPublicIdentity pubident) {
        String url = getIdentityURL(pubident);

        IntentIntegrator integrator = new IntentIntegrator(ctx);
        integrator.shareText(url);

    }

    public static Contact contactFromUri(Uri url) {
        Contact contact = new Contact(
                url.getQueryParameter("name"), "", "", url.getQueryParameter("tel"),
                url.getQueryParameter("key"), "", "", 0);
        return contact;
    }

    public static String getIdentityURL(IPublicIdentity pubident) {
        try {
            return "bonfire://contact?name=" + URLEncoder.encode(pubident.getNickname(), "utf-8")
                    + "&tel=" + URLEncoder.encode(pubident.getPhoneNumber(), "utf-8")
                    + "&key=" + URLEncoder.encode(pubident.getPublicKey().asBase64(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
