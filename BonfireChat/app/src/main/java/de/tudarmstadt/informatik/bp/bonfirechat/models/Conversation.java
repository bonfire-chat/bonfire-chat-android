package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johannes on 05.05.15.
 */
public class Conversation {
    private Contact peer;
    private ArrayList<Message> messages;

    public Conversation(Contact peer, List<Message> messages) {
        this.peer = peer;
        this.messages = new ArrayList<>(messages);
    }

    public Conversation(Contact peer){
        this.peer = peer;
        this.messages = new ArrayList<>();
    }

    public void addMessages(ArrayList<Message> messages){
        this.messages.addAll(messages);
    }

    public String getLastMessage() {
        return messages.get(messages.size() - 1).toString();
    }
    public String getName() {
        return peer.toString();
    }

    public ContentValues getContentValues(){
        ContentValues values = new ContentValues();
        values.put("peer", getName());
        return values;
    }

    public static Conversation fromCursor(Cursor cursor){
       return new Conversation(new Contact(cursor.getString(cursor.getColumnIndex("peer"))));
    }
}
