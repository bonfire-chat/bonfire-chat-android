package de.tudarmstadt.informatik.bp.bonfirechat.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;

/**
 * Created by simon on 05.05.15.
 */
public class BonfireData extends SQLiteOpenHelper{

    private static String CONTACTS = "contacts";
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

    }

    public void createContact(Contact contact){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(CONTACTS, null, contact.getContentValues());
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
}
