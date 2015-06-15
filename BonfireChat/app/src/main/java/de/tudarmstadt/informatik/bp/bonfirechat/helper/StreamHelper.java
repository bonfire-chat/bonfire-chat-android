package de.tudarmstadt.informatik.bp.bonfirechat.helper;

/**
 * Created by mw on 16.06.15.
 */
public class StreamHelper {

    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
