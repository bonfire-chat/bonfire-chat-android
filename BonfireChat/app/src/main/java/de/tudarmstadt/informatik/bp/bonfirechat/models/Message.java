package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;

/**
 * Created by johannes on 05.05.15.
 */
public class Message implements Serializable {
    public enum MessageDirection {
        Unknown,
        Sent,
        MessageDirection, Received
    }
    public long rowid;
    public List<Contact> recipients;
    public Contact sender;
    public String body;
    public MessageDirection direction = MessageDirection.Unknown;
    public Date sentTime;
    public String dateTime;

    public Message(String body, Contact sender, MessageDirection dir, Date sentTime) {
        this.body = body; this.sender = sender; this.direction = dir; this.sentTime = sentTime; this.dateTime = DateHelper.formatTime(sentTime);
    }

    public Message(String body, Contact sender, MessageDirection dir, String dateTime) {
        this.body = body; this.sender = sender; this.direction = dir; this.dateTime = dateTime;
    }

    public Message(String body, MessageDirection dir, String dateTime) {
        this.body = body; this.direction = dir; this.dateTime = dateTime;
    }

    public Message(String body, MessageDirection dir, String dateTime, long rowid) {
        this.body = body; this.direction = dir; this.dateTime = dateTime; this.rowid = rowid;
    }

    public Message(String body, Contact sender, MessageDirection dir, String dateTime, long rowid) {
        this.body = body; this.direction = dir; this.dateTime = dateTime; this.rowid = rowid;
        this.sender = sender;
    }

    @Override
    public String toString() {
        return body;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        if (this.sender != null) values.put("sender", this.sender.rowid);
        values.put("messageDirection", direction.ordinal());
        values.put("body", body);
        values.put("dateTime", dateTime);
        return values;
    }

    public static Message fromCursor(Cursor cursor, BonfireData db){
        Contact peer = db.getContactById(cursor.getLong(cursor.getColumnIndex("sender")));
        return new Message(cursor.getString(cursor.getColumnIndex("body")),
                peer,
                MessageDirection.values()[cursor.getInt(cursor.getColumnIndex("messageDirection"))],
                cursor.getString(cursor.getColumnIndex("dateTime")),
                cursor.getLong(cursor.getColumnIndex("rowid")));
    }
}
