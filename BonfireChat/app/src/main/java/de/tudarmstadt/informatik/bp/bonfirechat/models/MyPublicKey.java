package de.tudarmstadt.informatik.bp.bonfirechat.models;

import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;

/**
 * Created by mw on 15.06.15.
 */
public class MyPublicKey {

    private BCECPublicKey thisKey;

    public MyPublicKey(BCECPublicKey key) {
        thisKey = key;
    }
    public BCECPublicKey get() {
        return thisKey;
    }
    public byte[] asByteArray() {
        return thisKey.getEncoded();
    }
    public String asBase64() {
        return CryptoHelper.toBase64(thisKey.getEncoded());
    }
    public String asHash() {
        return CryptoHelper.hash("MD5", thisKey.getEncoded());
    }



    public static MyPublicKey deserialize(byte[] publicKey) {
        try {
            KeyFactory fact = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
            return new MyPublicKey ((BCECPublicKey) fact.generatePublic(new X509EncodedKeySpec(publicKey)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static MyPublicKey deserialize(String base64publicKey) {
        return deserialize(CryptoHelper.fromBase64(base64publicKey));
    }

}
