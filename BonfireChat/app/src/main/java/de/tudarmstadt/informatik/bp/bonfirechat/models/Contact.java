package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;

/**
 * Created by johannes on 05.05.15.
 */
public class Contact implements Serializable, IPublicIdentity {

    // the publickey is the globally unique identifier for a person/device
    public MyPublicKey publicKey;

    private String nickname;
    private String firstName;
    private String lastName;
    public String phoneNumber;
    private boolean shareLocation;

    private String lastKnownLocation;

    private String image;

    public String wifiMacAddress;
    public String bluetoothMacAddress;
    public long rowid;


    public Contact(String nickname, String firstName, String lastName, String phoneNumber, String publicKey, String wifiMacAddress, String bluetoothMacAddress, int rowid) {
        this(nickname, firstName, lastName, phoneNumber, MyPublicKey.deserialize(publicKey), wifiMacAddress, bluetoothMacAddress, rowid);
    }
    public Contact(String nickname, String firstName, String lastName, String phoneNumber, MyPublicKey publicKey, String wifiMacAddress, String bluetoothMacAddress, int rowid) {
        this.nickname = nickname;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.publicKey = publicKey;
        this.wifiMacAddress = wifiMacAddress;
        this.bluetoothMacAddress = bluetoothMacAddress;
        this.rowid = rowid;
        this.lastKnownLocation = "";
        this.shareLocation = false;
        this.image = "";
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
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public MyPublicKey getPublicKey() {
        return publicKey;
    }

    public LatLng getLastKnownLocation() {
        if (!lastKnownLocation.isEmpty()) {
            String[] coords = lastKnownLocation.split(":");
            return new LatLng(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
        }
        else {
            return null;
        }
    }
    public void setLastKnownLocation(String location) {
        this.lastKnownLocation = location;
    }

    public boolean isShareLocation() {
        return shareLocation;
    }

    public void setShareLocation(boolean shareLocation) {
        this.shareLocation = shareLocation;
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
        values.put("publicKey", publicKey == null ? null : publicKey.asBase64());
        values.put("wifiMacAddress", wifiMacAddress);
        values.put("bluetoothMacAddress", bluetoothMacAddress);
        values.put("lastKnownLocation", lastKnownLocation);
        values.put("shareLocation", shareLocation ? 1 : 0);
        values.put("image", image);
        return values;
    }

    public static Contact fromCursor(Cursor cursor){
        Contact contact = new Contact(cursor.getString(cursor.getColumnIndex("nickname")),
                    cursor.getString(cursor.getColumnIndex("firstName")),
                cursor.getString(cursor.getColumnIndex("lastName")),
                cursor.getString(cursor.getColumnIndex("phoneNumber")),
                cursor.getString(cursor.getColumnIndex("publicKey")),
                cursor.getString(cursor.getColumnIndex("wifiMacAddress")),
                cursor.getString(cursor.getColumnIndex("bluetoothMacAddress")),
                cursor.getInt(cursor.getColumnIndex("rowid")));
        contact.setLastKnownLocation(cursor.getString(cursor.getColumnIndex("lastKnownLocation")));
        contact.setShareLocation(cursor.getInt(cursor.getColumnIndex("shareLocation")) == 1);
        contact.setImage(cursor.getString(cursor.getColumnIndex("image")));
        return contact;
    }

    public static Contact findOrCreate(Context ctx, byte[] publicKey, String untrustedNickname) {
        BonfireData db = BonfireData.getInstance(ctx);
        Contact theContact = db.getContactByPublicKey(publicKey);
        if (theContact == null) {
            // TODO: better contact creation.... e.g. search in online directory
            theContact = new Contact(untrustedNickname, "", "", "", MyPublicKey.deserialize(publicKey), "", "", 0);
            db.createContact(theContact);
        }
        return theContact;
    }
}
