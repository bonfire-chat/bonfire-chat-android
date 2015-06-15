package de.tudarmstadt.informatik.bp.bonfirechat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
    private static final String[] ALL_COLS = new String[]{ "rowid", "*" };
    private static final String TAG = "BonfireData";

	/**
	 * URL of the rendezvous server API endpoint
	 */
	public static final String API_ENDPOINT = "https://bonfire.projects.teamwiki.net";

	private static BonfireData instance;

    public static BonfireData getInstance(Context ctx) {
        if (instance == null) instance = new BonfireData(ctx);
        return instance;
    }

    private SQLiteOpenHelper helper;

    private BonfireData(Context context) {
        super(context, "CommunicationData", null, 9);

    }


    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE if not exists " + CONTACTS + "(nickname TEXT, firstName TEXT, lastName TEXT, phoneNumber TEXT, publicKey TEXT, xmppId TEXT, wifiMacAddress TEXT, bluetoothMacAddress TEXT)");
        db.execSQL("CREATE TABLE if not exists " + CONVERSATIONS + "(peer INT, conversationType INT, title TEXT)");
        db.execSQL("CREATE TABLE if not exists " + MESSAGES + "(conversation INT NOT NULL, sender INT NOT NULL, messageDirection INTEGER NOT NULL, body TEXT, dateTime TEXT)");
        db.execSQL("CREATE TABLE if not exists " + IDENTITIES + "(nickname TEXT, privatekey TEXT, publickey TEXT, server TEXT, username TEXT, password TEXT, phone TEXT)");

    }

    public void createContact(Contact contact){
        SQLiteDatabase db = getWritableDatabase();
        contact.rowid = db.insert(CONTACTS, null, contact.getContentValues());
        //db.close();
    }

    public void createIdentity(Identity identity){
        SQLiteDatabase db = getWritableDatabase();
        identity.rowid = db.insert(IDENTITIES, null, identity.getContentValues());
        //db.close();
    }

    public void createConversation(Conversation conversation){
        SQLiteDatabase db = getWritableDatabase();
        conversation.rowid = db.insert(CONVERSATIONS, null, conversation.getContentValues());
        //db.close();
    }

    public void createMessage(Message message, Conversation conversation){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = message.getContentValues();
        values.put("conversation", conversation.rowid);
        message.rowid = db.insert(MESSAGES, null, values);
        //db.close();
    }

    public Identity getDefaultIdentity() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(IDENTITIES, ALL_COLS, null, null, null, null, null, "1");
        Identity i = null;
        if (cursor.moveToNext()) {
            i = Identity.fromCursor(cursor);
        }
        //db.close();
        return i;
    }

    public ArrayList<Conversation> getConversations(){
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Conversation> conversations = new ArrayList<>();
        Cursor conversationCursor = db.query(CONVERSATIONS, ALL_COLS, null, null, null, null, null);
        while(conversationCursor.moveToNext()){
            Contact contact = getContactById(conversationCursor.getInt(conversationCursor.getColumnIndex("peer")));
            Conversation conversation = Conversation.fromCursor(contact, conversationCursor);
            conversation.addMessages(this.getMessages(conversation));
            conversations.add(conversation);
        }
        //db.close();
        return conversations;
    }

    public Conversation getConversationByPeer(Contact peer){
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Conversation> conversations = new ArrayList<>();
        Cursor conversationCursor = db.query(CONVERSATIONS, ALL_COLS, "peer = ?", new String[]{String.valueOf(peer.rowid)}, null, null, null);
        Conversation conversation = null;
        if(conversationCursor.moveToNext()){
            conversation = Conversation.fromCursor(peer, conversationCursor);
        }
        //db.close();
        return conversation;
    }
    public Conversation getConversationById(long rowid){
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Conversation> conversations = new ArrayList<>();
        Cursor conversationCursor = db.query(CONVERSATIONS, ALL_COLS, "rowid = ?", new String[]{String.valueOf(rowid)}, null, null, null);
        Conversation conversation = null;
        if(conversationCursor.moveToNext()){
            long peerId = conversationCursor.getInt(conversationCursor.getColumnIndex("peer"));
            Log.d(TAG, "Found conversation with id=" + rowid + " and peerId=" + peerId);
            conversation = Conversation.fromCursor(
                    getContactById(peerId),
                    conversationCursor);
        }
        //db.close();
        return conversation;
    }

    public Message getMessageById(long rowid){
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Message> messages = new ArrayList<>();
        Cursor cursor = db.query(MESSAGES, ALL_COLS, "rowid=?", new String[]  {String.valueOf(rowid)}, null, null, null);
        if (!cursor.moveToNext()) return null;
        Message message = Message.fromCursor(cursor, this);
        //db.close();
        return message;
    }
    public ArrayList<Message> getMessages(Conversation conversation){
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Message> messages = new ArrayList<>();
        Cursor messageCursor = db.query(MESSAGES, ALL_COLS, "conversation=?", new String[]{String.valueOf(conversation.rowid)}, null, null, null);
        while(messageCursor.moveToNext()){
            messages.add(Message.fromCursor(messageCursor, this));
        }
        //db.close();
        return messages;
    }

    public boolean deleteContact(Contact contact){

        SQLiteDatabase db = this.getWritableDatabase();
        try
        {
            db.delete(CONTACTS, "rowid=?", new String[] { String.valueOf(contact.rowid) });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            //db.close();
        }
        ;
        return true;

    }
    
    public boolean deleteConversation(Conversation conversation){

        SQLiteDatabase db = this.getWritableDatabase();
        try
        {
            db.delete(CONVERSATIONS, "rowid=?", new String[] { String.valueOf(conversation.rowid) });
            db.delete(MESSAGES, "conversation=?", new String[] { String.valueOf(conversation.rowid) });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            //db.close();
        }
        ;
        return true;

    }
    public ArrayList<Contact> getContacts(){
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Contact> contacts = new ArrayList<>();
        Cursor cursor = db.query(CONTACTS, ALL_COLS, null, null, null, null, null);
        while(cursor.moveToNext()){
            contacts.add(Contact.fromCursor(cursor));
        }
        return contacts;
    }
    public Contact getContactByXmppId(String xmppId){
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Contact> contacts = new ArrayList<>();
        Cursor cursor = db.query(CONTACTS, ALL_COLS, "xmppId = ?", new String[]{ xmppId }, null, null, null);
        if (!cursor.moveToNext()) return null;
        return Contact.fromCursor(cursor);
    }
    public Contact getContactByPublicKey(String publicKey){
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Contact> contacts = new ArrayList<>();
        Cursor cursor = db.query(CONTACTS, ALL_COLS, "publicKey = ?", new String[]{ publicKey }, null, null, null);
        if (!cursor.moveToNext()) return null;
        return Contact.fromCursor(cursor);
    }
    public Contact getContactById(long id){
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Contact> contacts = new ArrayList<>();
        Cursor cursor = db.query(CONTACTS, ALL_COLS, "rowid = ?", new String[]{ String.valueOf(id) }, null, null, null);
        if (!cursor.moveToNext()) return null;
        return Contact.fromCursor(cursor);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        if (oldVersion >= newVersion)
            return;

        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + CONVERSATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + IDENTITIES);

        onCreate(db);
    }

    public void updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(CONTACTS, contact.getContentValues(), " rowid = ? ", new String[]{String.valueOf(contact.rowid)});
    }
    public void updateMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(MESSAGES, message.getContentValues(), " rowid = ? ", new String[]{String.valueOf(message.rowid)});
    }
    public void updateIdentity(Identity identity) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(IDENTITIES, identity.getContentValues(), " rowid = ? ", new String[]{String.valueOf(identity.rowid)});
    }
    public void updateConversation(Conversation conversation) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(CONVERSATIONS, conversation.getContentValues(), " rowid = ? ", new String[]{String.valueOf(conversation.rowid)});
    }
}
