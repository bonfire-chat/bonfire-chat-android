package de.tudarmstadt.informatik.bp.bonfirechat.stats;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.text.ParseException;
import java.util.Date;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;

/**
 * Created by johannes on 10.07.15.
 */
public class StatsEntry {
    public Date timestamp;
    public Date created_at;
    public int batteryLevel;
    public float powerUsage;
    public int messagesSent;
    public int messageReceived;
    public float lat, lng;
    public long rowid;

    public StatsEntry() {
        this(new Date(), new Date(), 0, 0, 0, 0, 0, 0, 0);
    }
    public StatsEntry(Date timestamp, Date created_at, int batteryLevel, float powerUsage, int messagesSent, int messageReceived, float lat, float lng, long rowid) {
        this.timestamp = timestamp;
        this.created_at = created_at;
        this.batteryLevel = batteryLevel;
        this.powerUsage = powerUsage;
        this.messagesSent = messagesSent;
        this.messageReceived = messageReceived;
        this.lat = lat;
        this.lng = lng;
        this.rowid = rowid;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put("timestamp", DateHelper.formatDateTime(timestamp));
        values.put("created_at", DateHelper.formatDateTime(created_at));
        values.put("batterylevel", batteryLevel);
        values.put("powerusage", powerUsage);
        values.put("messages_sent", messagesSent);
        values.put("messages_received", messageReceived);
        values.put("lat", lat);
        values.put("lng", lng);
        return values;
    }

    public static StatsEntry fromCursor(Cursor cursor) {
        try {
            StatsEntry stats = new StatsEntry(
                    DateHelper.parseDateTime(cursor.getString(cursor.getColumnIndex("timestamp"))),
                    DateHelper.parseDateTime(cursor.getString(cursor.getColumnIndex("created_at"))),
                    cursor.getInt(cursor.getColumnIndex("batterylevel")),
                    cursor.getFloat(cursor.getColumnIndex("powerusage")),
                    cursor.getInt(cursor.getColumnIndex("messages_sent")),
                    cursor.getInt(cursor.getColumnIndex("messages_received")),
                    cursor.getFloat(cursor.getColumnIndex("lat")),
                    cursor.getFloat(cursor.getColumnIndex("lng")),
                    cursor.getLong(cursor.getColumnIndex("rowid"))
            );
            return stats;
        }
        catch (ParseException e) {
            Log.e("StatsEntry", "error parsing stats entry from database");
            return new StatsEntry();
        }
    }
}
