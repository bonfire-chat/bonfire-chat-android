package de.tudarmstadt.informatik.bp.bonfirechat.helper;

import java.util.Formatter;

/**
 * Created by mw on 16.06.15.
 */
public class StreamHelper {

    public static String convertStreamToString(java.io.InputStream is) {
        if (is == null) return "";
        java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static String byteArrayToHexString(byte[] byteArray) {
        Formatter formatter = new Formatter();
        for (byte b : byteArray) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

}
