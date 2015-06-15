package de.tudarmstadt.informatik.bp.bonfirechat.helper;

import android.util.Base64;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;

/**
 * Created by mw on 08.06.15.
 */
public class CryptoHelper {

    public static ECParameterSpec getEllipticCurveParameters() {
        X9ECParameters ecP = CustomNamedCurves.getByName("curve25519");
        ECParameterSpec ecSpec=new ECParameterSpec(ecP.getCurve(), ecP.getG(),
                ecP.getN(), ecP.getH(), ecP.getSeed());
        return ecSpec;
    }

    public static KeyPair generateKeyPair() {
        try {
            Provider bcProvider = new BouncyCastleProvider();
            KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", bcProvider);
            g.initialize(getEllipticCurveParameters(), new SecureRandom());
            KeyPair keyPair = g.generateKeyPair();
            return keyPair;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toBase64(byte[] b) {
        return Base64.encodeToString(b, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }
    public static byte[] fromBase64(String base64) {
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
