package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.ContentValues;
import android.database.Cursor;

import org.bouncycastle.asn1.eac.ECDSAPublicKey;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;

/**
 * Created by johannes on 05.05.15.
 */
public class Contact implements Serializable {
    private String nickname;
    private String firstName;
    private String lastName;
    public String phoneNumber;
    public ECDSAPublicKey publicKey;
    public String xmppId;
    public String wifiMacAddress;
    public String bluetoothMacAddress;
    public long rowid;


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

        try {
            KeyFactory fact = KeyFactory.getInstance("ECDSA", "BC");
            this.publicKey = (ECDSAPublicKey) fact.generatePublic(new X509EncodedKeySpec(CryptoHelper.fromBase64(publicKey)));
        } catch (Exception e) {
            e.printStackTrace();
            this.publicKey = null;
        }
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

    public String getPublicKeyHash() {
        try {
            return CryptoHelper.hash("MD5", this.publicKey.getEncoded());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    public String getPublicKeyEncoded() {
        try {
            return CryptoHelper.toBase64(publicKey.getEncoded());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
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
        values.put("publicKey", this.getPublicKeyEncoded());
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
}
