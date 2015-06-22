package de.wikilab.bonfiredebug;

import org.abstractj.kalium.crypto.Random;
import org.abstractj.kalium.keys.KeyPair;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created 21.06.15 23:50.
 *
 * @author Max Weller
 * @version 2015-06-21-001
 */
public class CryptoHelper {


	public static KeyPair generateKeyPair() {
		KeyPair keyPair = new KeyPair();
		return keyPair;
	}

	public static byte[] generateNonce() {
		return new Random().randomBytes(24);
	}



	public static String toBase64(byte[] data) {
		return Base64.getUrlEncoder().encodeToString(data).replaceFirst("[=]+$", "");
	}

	public static byte[] fromBase64(String dataStr) {
		return Base64.getUrlDecoder().decode(dataStr);
	}



}
