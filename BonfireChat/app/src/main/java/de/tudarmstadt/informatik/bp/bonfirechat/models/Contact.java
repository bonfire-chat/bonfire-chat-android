package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by johannes on 05.05.15.
 */
public class Contact {
    private String nickname;
    private String firstName;
    private String lastName;

    public Contact(String nickname) {
        this.nickname = nickname;
        this.firstName = "";
        this.lastName = "";
    }

    public Contact(String nickname, String firstName, String lastName){
        this.nickname = nickname;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getNickname() {
        return nickname;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    @Override
    public String toString() {
        return nickname;
    }

    public ContentValues getContentValues(){
        ContentValues values = new ContentValues();
        values.put("uid", nickname);
        values.put("firstName", firstName);
        values.put("lastName", lastName);
        return values;
    }

    public static Contact fromCursor(Cursor cursor){
        return new Contact(cursor.getString(cursor.getColumnIndex("uid")),
                    cursor.getString(cursor.getColumnIndex("firstName")),
                    cursor.getString(cursor.getColumnIndex("lastName")));
    }
}
