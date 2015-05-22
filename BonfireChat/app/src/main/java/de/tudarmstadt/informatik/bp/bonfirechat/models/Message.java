package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by johannes on 05.05.15.
 */
public class Message {
    public enum MessageDirection {
        Unknown,
        Sent,
        Received
    }
    public long rowid;
    public Contact peer;
    public String body;
    public MessageDirection direction = MessageDirection.Unknown;
    public String dateTime;

    public Message(String body, Contact peer, MessageDirection dir, String dateTime) {
        this.body = body; this.peer = peer; this.direction = dir; this.dateTime = dateTime;
    }

    public Message(String body, MessageDirection dir, String dateTime) {
        this.body = body; this.direction = dir; this.dateTime = dateTime;
    }

    public Message(String body, MessageDirection dir, String dateTime, long rowid) {
        this.body = body; this.direction = dir; this.dateTime = dateTime; this.rowid = rowid;
    }

    @Override
    public String toString() {
        return body;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        if (this.peer != null) values.put("peer", this.peer.rowid);
        values.put("messageDirection", direction.ordinal());
        values.put("body", body);
        values.put("dateTime", dateTime);
        return values;
    }

    public static Message fromCursor(Cursor cursor){
        return new Message(cursor.getString(cursor.getColumnIndex("body")),
                MessageDirection.values()[cursor.getInt(cursor.getColumnIndex("messageDirection"))],
                cursor.getString(cursor.getColumnIndex("dateTime")),
                cursor.getLong(cursor.getColumnIndex("rowid")));
    }
}
