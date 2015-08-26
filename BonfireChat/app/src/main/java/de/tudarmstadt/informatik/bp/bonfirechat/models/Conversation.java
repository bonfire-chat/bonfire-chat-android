package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;

/**
 * Created by johannes on 05.05.15.
 */
public class Conversation {
    private final Contact peer;
    private final ArrayList<Message> messages;
    public long rowid;
    public String title;
    public ConversationType conversationType = ConversationType.Single;

    public enum ConversationType {
        Single,
        GroupChat
    }

    public Conversation(Contact peer, String title, int rowid, List<Message> messages) {
        this.peer = peer;
        this.title = title;
        this.messages = new ArrayList<>(messages);
        this.rowid = rowid;
    }

    public Conversation(Contact peer, String title, int rowid){
        this.peer = peer;
        this.title = title;
        this.messages = new ArrayList<>();
        this.rowid = rowid;
    }

    public void addMessages(ArrayList<Message> messages){
        this.messages.addAll(messages);
    }

    public String getLastMessage(Context context) {
        if(messages.size() > 0) {
            Message message = messages.get(messages.size() - 1);
            // show placeholder descriptions for images and locations
            if (message.hasFlag(Message.FLAG_IS_FILE)) {
                return context.getString(R.string.image);
            }
            else if (message.hasFlag(Message.FLAG_IS_LOCATION)) {
                return context.getString(R.string.location);
            }
            else {
                return message.toString();
            }
        }
        else {
            return "";
        }
    }
    public String getLastMessageDate() {
        if(messages.size() > 0)
            return DateHelper.formatTimeOrDate(messages.get(messages.size() - 1).sentTime);
        else
            return "";
    }
    public String getName() {
        if (title != null && !title.equals("")) return title;
        if (peer != null) return peer.getNickname();
        return "(unnamed)";
    }
    public Contact getPeer() {
        return peer;
    }

    public ContentValues getContentValues(){
        ContentValues values = new ContentValues();
        if (peer != null) values.put("peer", peer.rowid);
        values.put("conversationType", conversationType.ordinal());
        values.put("title", title);
        return values;
    }

    public static Conversation fromCursor(Contact contact, Cursor cursor){
        Conversation conversation = new Conversation(contact,
               cursor.getString(cursor.getColumnIndex("title")),
               cursor.getInt(cursor.getColumnIndex("rowid")));
        conversation.conversationType = ConversationType.values()[cursor.getInt(cursor.getColumnIndex("conversationType"))];
        return conversation;
    }

}
