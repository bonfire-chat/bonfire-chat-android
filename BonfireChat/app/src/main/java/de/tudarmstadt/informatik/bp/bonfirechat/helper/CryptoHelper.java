package de.tudarmstadt.informatik.bp.bonfirechat.helper;

import android.util.Base64;

import org.abstractj.kalium.SodiumConstants;
import org.abstractj.kalium.crypto.Random;
import org.abstractj.kalium.keys.KeyPair;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by mw on 08.06.15.
 */
public class CryptoHelper {


    public static KeyPair generateKeyPair() {
        KeyPair keyPair = new KeyPair();
        return keyPair;
    }

    public static byte[] generateNonce() {
        return new Random().randomBytes(SodiumConstants.NONCE_BYTES);
    }


    public static String toBase64(byte[] b) {
        return Base64.encodeToString(b, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }
    public static byte[] fromBase64(String base64) {
        if (base64 == null) return null;
        return Base64.decode(base64, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    public static String hash(String algorithm, byte[] string) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            return null;
        }
        digest.reset();
        return Base64.encodeToString(digest.digest(string), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }

    public static String hash(String algorithm, String string) {
        try {
            return hash(algorithm, string.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
