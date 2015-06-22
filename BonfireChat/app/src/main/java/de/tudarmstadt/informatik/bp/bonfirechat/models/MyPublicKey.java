package de.tudarmstadt.informatik.bp.bonfirechat.models;

import org.abstractj.kalium.keys.PublicKey;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;

/**
 * Created by mw on 15.06.15.
 */
public class MyPublicKey {

    private PublicKey thisKey;

    public MyPublicKey(PublicKey key) {
        thisKey = key;
    }

    public PublicKey get() {
        return thisKey;
    }
    public byte[] asByteArray() {
        return thisKey.toBytes();
    }
    public String asBase64() {
        return CryptoHelper.toBase64(thisKey.toBytes());
    }
    public String asHash() {
        return asBase64();
        //return CryptoHelper.hash("MD5", thisKey.toBytes());
    }

    public static MyPublicKey deserialize(byte[] publicKey) {
        try {
            return new MyPublicKey(new PublicKey(publicKey));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static MyPublicKey deserialize(String base64publicKey) {
        return deserialize(CryptoHelper.fromBase64(base64publicKey));
    }

}
