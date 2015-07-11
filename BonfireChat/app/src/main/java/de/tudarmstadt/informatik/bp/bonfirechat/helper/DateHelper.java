package de.tudarmstadt.informatik.bp.bonfirechat.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mw on 22.05.15.
 */
public class DateHelper {

    public static String getNowString() {
        return formatTime(new Date());
    }

    public static String formatTime(Date date) {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }
    public static String formatDateTime(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
    public static Date parseDateTime(String string) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(string);
    }

}
