package de.wikilab.bonfirechat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by simon on 05.05.15.
 */
public class Bonfiredata extends SQLiteOpenHelper{

    Bonfiredata(Context context) {
        super(context,"CommunicationData", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase database){
        database.execSQL("Create Table Kontakte(UID TEXT UNIQUE PRIMARY KEY, FirstName Text, LastName Text)");
        database.execSQL("Create Table Konversationen(KonvID INT UNIQUE PRIMARY KEY, UID TEXT, MsgID INT)");
        database.execSQL("Create Table MsgID INT, Nachricht TEXT");
    }
    @Override
    public void onUpgrade(SQLiteDatabase base, int oldVersion, int newVersion){
    //todo
    }
}
