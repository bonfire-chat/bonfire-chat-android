package de.tudarmstadt.informatik.bp.bonfirechat.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mw on 22.05.15.
 */
public class DateHelper {

    public static String getNowString() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static String formatTime(Date date) {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }
}
