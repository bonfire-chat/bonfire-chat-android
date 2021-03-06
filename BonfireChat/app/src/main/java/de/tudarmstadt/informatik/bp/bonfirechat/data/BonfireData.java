package de.tudarmstadt.informatik.bp.bonfirechat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.stats.StatsEntry;

/**
 * Created by simon on 05.05.15.
 */
public final class BonfireData extends SQLiteOpenHelper {

    private static final int SQLITE_DATABASE_VERSION = 19;

    private static final String CONTACTS = "contacts";
    private static final String CONVERSATIONS = "conversations";
    private static final String MESSAGES = "messages";
    private static final String STATS = "stats";

    private static final String IDENTITIES = "identity";
    // rowid is not included in "*" by default
    private static final String[] ALL_COLS = new String[]{"rowid", "*"};

    private static final String TAG = "BonfireData";

    private Identity cachedDefaultIdentity = null;

    private static BonfireData instance;

    public static BonfireData getInstance(Context ctx) {
        if (instance == null) {
            instance = new BonfireData(ctx);
        }
        return instance;
    }

    private BonfireData(Context context) {
        super(context, "CommunicationData", null, SQLITE_DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE if not exists " + CONTACTS + "(nickname TEXT, firstName TEXT, lastName TEXT, phoneNumber TEXT, publicKey TEXT "
                + "UNIQUE, wifiMacAddress TEXT, bluetoothMacAddress TEXT, lastKnownLocation TEXT, shareLocation INT, image TEXT)");
        db.execSQL("CREATE TABLE if not exists " + CONVERSATIONS + "(peer INT, conversationType INT, title TEXT)");
        db.execSQL("CREATE TABLE if not exists " + MESSAGES + "(uuid TEXT NOT NULL PRIMARY KEY, conversation INT NOT NULL, sender INT NOT NULL, "
                + "flags INT NOT NULL, protocol TEXT, body TEXT, sentDate TEXT, insertDate INT, traceroute BLOB, retries INT, error TEXT)");
        db.execSQL("CREATE TABLE if not exists " + IDENTITIES + "(nickname TEXT, privatekey TEXT, publickey TEXT, username TEXT, phone TEXT, "
                + "image TEXT)");
        db.execSQL("CREATE TABLE if not exists " + STATS + "(timestamp DATETIME, batterylevel INT, powerusage FLOAT, messages_sent INT, "
                + "messages_received INT, lat FLOAT, lng FLOAT)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= newVersion) {
            return;
        }

        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + CONVERSATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + IDENTITIES);

        onCreate(db);
    }


    public boolean createContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();
        contact.rowid = db.insert(CONTACTS, null, contact.getContentValues());
        if (contact.rowid == -1) {
            return false;
        }
        return true;
    }

    public void createIdentity(Identity identity) {
        SQLiteDatabase db = getWritableDatabase();
        identity.rowid = db.insert(IDENTITIES, null, identity.getContentValues());
    }

    public void createConversation(Conversation conversation) {
        SQLiteDatabase db = getWritableDatabase();
        conversation.rowid = db.insert(CONVERSATIONS, null, conversation.getContentValues());
    }

    public long createMessage(Message message, Conversation conversation) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = message.getContentValues();
        values.put("conversation", conversation.rowid);
        values.put("insertDate", (new Date()).getTime());
        return db.insert(MESSAGES, null, values);
    }

    public Identity getDefaultIdentity() {
        if (cachedDefaultIdentity != null) {
            return cachedDefaultIdentity;
        }
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(IDENTITIES, ALL_COLS, null, null, null, null, null, "1");
        if (cursor.moveToNext()) {
            cachedDefaultIdentity = Identity.fromCursor(cursor);
        }
        cursor.close();
        return cachedDefaultIdentity;
    }

    public ArrayList<Conversation> getConversations() {
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Conversation> conversations = new ArrayList<>();
        String rawQuery = "SELECT c.rowid, c.*, m.sentDate FROM " + CONVERSATIONS + " c LEFT OUTER JOIN "
                + MESSAGES + " m ON c.rowid=m.conversation GROUP BY c.rowid ORDER BY m.sentDate DESC";
        Cursor conversationCursor = db.rawQuery(rawQuery, null);
        while (conversationCursor.moveToNext()) {
            Contact contact = getContactById(conversationCursor.getInt(conversationCursor.getColumnIndex("peer")));
            Conversation conversation = Conversation.fromCursor(contact, conversationCursor);
            conversation.addMessages(this.getMessages(conversation));
            conversations.add(conversation);
        }
        conversationCursor.close();
        return conversations;
    }

    public Conversation getConversationByPeer(Contact peer) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor conversationCursor = db.query(CONVERSATIONS, ALL_COLS, "peer = ?", new String[]{String.valueOf(peer.rowid)}, null, null, null);
        Conversation conversation = null;
        if (conversationCursor.moveToNext()) {
            conversation = Conversation.fromCursor(peer, conversationCursor);
        }
        conversationCursor.close();
        return conversation;
    }
    public Conversation getConversationById(long rowid) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor conversationCursor = db.query(CONVERSATIONS, ALL_COLS, "rowid = ?", new String[]{String.valueOf(rowid)}, null, null, null);
        Conversation conversation = null;
        if (conversationCursor.moveToNext()) {
            long peerId = conversationCursor.getInt(conversationCursor.getColumnIndex("peer"));
            Log.d(TAG, "Found conversation with id=" + rowid + " and peerId=" + peerId);
            conversation = Conversation.fromCursor(
                    getContactById(peerId),
                    conversationCursor);
        }
        conversationCursor.close();
        return conversation;
    }

    public Message getMessageByUUID(UUID id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(MESSAGES, null, "uuid=?", new String[]  {id.toString()}, null, null, null);
        if (!cursor.moveToNext()) {
            return null;
        }
        Message message = Message.fromCursor(cursor, this);
        cursor.close();
        return message;
    }
    public ArrayList<Message> getMessages(Conversation conversation) {
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Message> messages = new ArrayList<>();
        Cursor messageCursor = db.query(MESSAGES, null, "conversation=?",
                new String[]{String.valueOf(conversation.rowid)}, null, null, "sentDate ASC");
        while (messageCursor.moveToNext()) {
            messages.add(Message.fromCursor(messageCursor, this));
        }
        messageCursor.close();
        return messages;
    }
    public ArrayList<Message> getPendingMessages() {
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Message> messages = new ArrayList<>();
        Cursor messageCursor = db.query(MESSAGES, null, "retries < ? AND sender == -1 AND NOT flags & "
                + Message.FLAG_ACKNOWLEDGED, new String[]{String.valueOf(ConstOptions.MAX_RETRANSMISSIONS)}, null, null, "");
        while (messageCursor.moveToNext()) {
            messages.add(Message.fromCursor(messageCursor, this));
        }
        messageCursor.close();
        return messages;
    }

    public boolean deleteContact(Contact contact) {
        // delete conversation with this contact
        Conversation conversation = getConversationByPeer(contact);
        if (conversation != null) {
            deleteConversation(conversation);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(CONTACTS, "rowid=?", new String[] {String.valueOf(contact.rowid)});
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean deleteConversation(Conversation conversation) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(CONVERSATIONS, "rowid=?", new String[] {String.valueOf(conversation.rowid)});
            db.delete(MESSAGES, "conversation=?", new String[] {String.valueOf(conversation.rowid)});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }

    public boolean deleteMessage(Message message) {

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(MESSAGES, "uuid=?", new String[] {String.valueOf(message.uuid)});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }

    public ArrayList<Contact> getContacts() {
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Contact> contacts = new ArrayList<>();
        Cursor cursor = db.query(CONTACTS, ALL_COLS, null, null, null, null, "nickname COLLATE NOCASE");
        while (cursor.moveToNext()) {
            contacts.add(Contact.fromCursor(cursor));
        }
        cursor.close();
        return contacts;
    }
    public ArrayList<Contact> getContactsToShareLocationWith() {
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<Contact> contacts = new ArrayList<>();
        Cursor cursor = db.query(CONTACTS, ALL_COLS, "shareLocation == 1", null, null, null, null);
        while (cursor.moveToNext()) {
            contacts.add(Contact.fromCursor(cursor));
        }
        cursor.close();
        return contacts;
    }

    public Contact getContactByPublicKey(byte[] publicKey) {
        String publicKeyString = CryptoHelper.toBase64(publicKey);
        return getContactByPublicKey(publicKeyString);
    }
    public Contact getContactByPublicKey(String publicKey) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(CONTACTS, ALL_COLS, "publicKey = ?", new String[] {publicKey}, null, null, null);
        if (!cursor.moveToNext()) {
            return null;
        }
        Contact c = Contact.fromCursor(cursor);
        cursor.close();
        return c;
    }
    public Contact getContactById(long id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(CONTACTS, ALL_COLS, "rowid = ?", new String[] {String.valueOf(id)}, null, null, null);
        if (!cursor.moveToNext()) {
            return null;
        }
        Contact c = Contact.fromCursor(cursor);
        cursor.close();
        return c;
    }

    public void updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(CONTACTS, contact.getContentValues(), " rowid = ? ", new String[]{String.valueOf(contact.rowid)});
    }
    public void updateMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(MESSAGES, message.getContentValues(), " uuid = ? ", new String[]{String.valueOf(message.uuid)});
    }
    public void updateIdentity(Identity identity) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(IDENTITIES, identity.getContentValues(), " rowid = ? ", new String[]{String.valueOf(identity.rowid)});
    }
    public void updateConversation(Conversation conversation) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(CONVERSATIONS, conversation.getContentValues(), " rowid = ? ", new String[]{String.valueOf(conversation.rowid)});
    }

    public void addStatsEntry(StatsEntry stats) {
        SQLiteDatabase db = getWritableDatabase();
        db.insert(STATS, null, stats.getContentValues());
    }
    public StatsEntry getLatestStatsEntry() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(STATS, ALL_COLS, null, null, null, null, null);
        StatsEntry result;
        if (!cursor.moveToLast()) {
            Log.i(TAG, "no latest stats object found in database. Creating a new one...");
            result = new StatsEntry();
        } else {
            result = StatsEntry.fromCursor(cursor);
        }
        cursor.close();
        return result;
    }
}
