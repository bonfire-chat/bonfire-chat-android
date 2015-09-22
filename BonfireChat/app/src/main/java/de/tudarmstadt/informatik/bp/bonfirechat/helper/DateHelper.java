package de.tudarmstadt.informatik.bp.bonfirechat.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by mw on 22.05.15.
 */
public final class DateHelper {

    private DateHelper() { }

    /**
     * Formats the passed date as a HH:mm time if on the current day, as dd.MM. otherwise
     * @param date
     * @return
     */
    public static String formatTimeOrDate(Date date) {
        Date now = new Date();
        String formattedDate = formatDate(date);
        if (formatDate(now).equals(formattedDate)) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
        } else {
            return formattedDate;
        }
    }
    public static String formatTime(Date date) {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date);
    }
    public static String formatDateTime(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
    }
    public static String formatDate(Date date) {
        return new SimpleDateFormat("EEE dd.MM.yy", Locale.getDefault()).format(date);
    }
    public static Date parseDateTime(String string) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(string);
    }

    public static String formatTimeDelta(Date start, Date end) {
        // start after end?
        if (end.compareTo(start) < 0) {
            Date tmp = start;
            start = end;
            end = tmp;
        }
        long interval = end.getTime() - start.getTime();
        long seconds = TimeUnit.SECONDS.convert(interval, TimeUnit.MILLISECONDS);
        return "+ " + seconds + "s";
    }

}
