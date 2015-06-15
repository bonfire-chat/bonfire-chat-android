package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.io.Serializable;

/**
 * Created by johannes on 05.05.15.
 */
public class Contact implements Serializable {
    private String nickname;
    private String firstName;
    private String lastName;
    public String phoneNumber;
    public String publicKey;
    public String xmppId;
    public String wifiMacAddress;
    public String bluetoothMacAddress;
    public long rowid;

    public String getPublicKeyHash() {
        return Identity.hash("MD5", this.publicKey);
        //return Identity.hash("SHA-256", this.publicKey);
    }

    public String getXmppId() {
        return xmppId;
    }
    public void setXmppId(String xmppId) {
        this.xmppId = xmppId;
    }

    public Contact(String nickname) {
        this(nickname, nickname, "", null, null, null, null, null, 0);
    }

    public Contact(String nickname, String firstName, String lastName, String phoneNumber, String publicKey, String xmppId, String wifiMacAddress, String bluetoothMacAddress, int rowid) {
        this.nickname = nickname;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.publicKey = publicKey;
        this.xmppId = xmppId;
        this.wifiMacAddress = wifiMacAddress;
        this.bluetoothMacAddress = bluetoothMacAddress;
        this.rowid = rowid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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
        values.put("nickname", nickname);
        values.put("firstName", firstName);
        values.put("lastName", lastName);
        values.put("phoneNumber", phoneNumber);
        values.put("publicKey", publicKey);
        values.put("xmppId", xmppId);
        values.put("wifiMacAddress", wifiMacAddress);
        values.put("bluetoothMacAddress", bluetoothMacAddress);
        return values;
    }

    public static Contact fromCursor(Cursor cursor){
        Contact contact = new Contact(cursor.getString(cursor.getColumnIndex("nickname")),
                    cursor.getString(cursor.getColumnIndex("firstName")),
                cursor.getString(cursor.getColumnIndex("lastName")),
                cursor.getString(cursor.getColumnIndex("phoneNumber")),
                cursor.getString(cursor.getColumnIndex("publicKey")),
                cursor.getString(cursor.getColumnIndex("xmppId")),
                cursor.getString(cursor.getColumnIndex("wifiMacAddress")),
                cursor.getString(cursor.getColumnIndex("bluetoothMacAddress")),
                cursor.getInt(cursor.getColumnIndex("rowid")));
        return contact;
    }

    // TODO: implement
    public static Contact findOrCreate(Context ctx, byte[] publicKey) {
        return new Contact("foo");
    }
}
