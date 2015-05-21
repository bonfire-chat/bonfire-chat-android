package de.tudarmstadt.informatik.bp.bonfirechat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by simon on 05.05.15.
 */
public class BonfireData extends SQLiteOpenHelper{

    private static final String CONTACTS = "contacts";
    private static final String CONVERSATIONS = "conversations";
    private static final String MESSAGES = "messages";
    private static final String IDENTITIES = "identity";

    // rowid is not included in "*" by default
    private static final String[] ALL_COLS = new String[]{"rowid","*"};

    private static BonfireData instance;

    public static BonfireData getInstance(Context ctx) {
        if (instance == null) instance = new BonfireData(ctx);
        return instance;
    }

    private SQLiteOpenHelper helper;

    private BonfireData(Context context) {
        super(context, "CommunicationData", null, 1);

    }


    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + CONTACTS + "(uid TEXT UNIQUE PRIMARY KEY, firstName TEXT, lastName TEXT)");
        db.execSQL("CREATE TABLE " + CONVERSATIONS + "(peer TEXT UNIQUE PRIMARY KEY)");
        db.execSQL("CREATE TABLE " + MESSAGES + "(id INTEGER PRIMARY KEY AUTOINCREMENT, peer TEXT NOT NULL, messageDirection INTEGER NOT NULL, body TEXT)");
        db.execSQL("CREATE TABLE " + IDENTITIES + "(id INTEGER PRIMARY KEY AUTOINCREMENT, nickname TEXT, privatekey TEXT, publickey TEXT, server TEXT, username TEXT, password TEXT)");

    }

    public void createContact(Contact contact){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(CONTACTS, null, contact.getContentValues());
        db.close();
    }

    public void createIdentity(Identity identity){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(CONTACTS, null, identity.getContentValues());
        db.close();
    }

    public void createConversation(Conversation conversation){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(CONVERSATIONS, null, conversation.getContentValues());
        db.close();
    }

    public void createMessage(Message message, Conversation conversation){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = message.getContentValues();
        values.putAll(conversation.getContentValues());
        db.insert(MESSAGES, null, values);
        db.close();
    }

    public Identity getDefaultIdentity() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(IDENTITIES, ALL_COLS, null, null, null, null, null, "1");
        Identity i = null;
        if (cursor.moveToNext()) {
            i = Identity.fromCursor(cursor);
        }
        db.close();
        return i;
    }

    public ArrayList<Conversation> getConversations(){
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Conversation> conversations = new ArrayList<>();
        ArrayList<Message> messages = new ArrayList<>();
        Cursor conversationCursor = db.query(CONVERSATIONS, null, null, null, null, null, null);
        while(conversationCursor.moveToNext()){
            Conversation conversation = Conversation.fromCursor(conversationCursor);
            Cursor messageCursor = db.query(MESSAGES, null, "peer=?", new String[]  {conversation.getName()}, null, null, null);
            while(messageCursor.moveToNext()){
                messages.add(Message.fromCursor(messageCursor));
            }
            conversation.addMessages(messages);
            conversations.add(conversation);
        }
        db.close();
        return conversations;
    }

    public boolean deleteContact(Contact contact){

        SQLiteDatabase db = this.getWritableDatabase();
        try
        {
            db.delete(CONTACTS, "uid=?", new String[] { contact.getNickname() });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            db.close();
        }
        ;
        return true;

    }

    public ArrayList<Contact> getContacts(){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Contact> contacts = new ArrayList<>();
        Cursor cursor = db.query(CONTACTS, null, null, null, null, null, null);
        while(cursor.moveToNext()){
            contacts.add(Contact.fromCursor(cursor));
        }
        return contacts;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        if (oldVersion >= newVersion)
            return;
        ArrayList<Contact> buffer = new ArrayList<>();
        buffer = getContacts();
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS);
        onCreate(db);
        for(Contact c : buffer){
            createContact(c);
        }

    }

    public void updateIdentity(Identity identity) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(IDENTITIES, identity.getContentValues(), " rowid = ? ", new String[]{String.valueOf(identity.rowid)});

    }
}
