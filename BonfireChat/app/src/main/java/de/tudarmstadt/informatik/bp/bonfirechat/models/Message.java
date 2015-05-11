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

    public String body;
    public MessageDirection direction = MessageDirection.Unknown;

    public Message(String body, MessageDirection dir) {
        this.body = body; this.direction = dir;
    }

    @Override
    public String toString() {
        return body;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put("messageDirection", direction.ordinal());
        values.put("body", body);
        return values;
    }

    public static Message fromCursor(Cursor cursor){
        return new Message(cursor.getString(cursor.getColumnIndex("body")), MessageDirection.values()[cursor.getInt(cursor.getColumnIndex("messageDirection"))]);
    }
}
