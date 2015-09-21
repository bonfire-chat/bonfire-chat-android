package de.tudarmstadt.informatik.bp.bonfirechat.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by johannes on 28.08.15.
 */
public final class StringHelper {

    private StringHelper() { }

    public static boolean regexMatch(String pattern, String... strings) {
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher;

        for (String string: strings) {
            matcher = regex.matcher(string);
            if (!matcher.matches()) {
                return false;
            }
        }

        return true;
    }

}
