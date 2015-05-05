package de.wikilab.bonfirechat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by simon on 05.05.15.
 */
public class BonfireData extends SQLiteOpenHelper{

    private static String CONTACTS = "contacts";

    BonfireData(Context context) {
        super(context,"CommunicationData", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + CONTACTS + "(uid TEXT UNIQUE PRIMARY KEY, firstName TEXT, lastName TEXT)");
    }

    public void createContact(Contact contact){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(CONTACTS, null, contact.getContentValues());
    }

    public ArrayList<Contact> getContacts(){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Contact> contacts = new ArrayList<>();
        Cursor cursor = db.query(CONTACTS, null, null, null, null, null, null);
        while(cursor.moveToNext()){
            contacts.add(new Contact(cursor.getString(cursor.getColumnIndex("uid")),
                    cursor.getString(cursor.getColumnIndex("firstName")),
                    cursor.getString(cursor.getColumnIndex("lastName"))));
        }
        return contacts;
    }

    

    @Override
    public void onUpgrade(SQLiteDatabase base, int oldVersion, int newVersion){
    //todo
    }
}
