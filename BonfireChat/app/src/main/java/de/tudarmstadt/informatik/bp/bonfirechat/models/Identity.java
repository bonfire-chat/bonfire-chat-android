package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by mw on 18.05.15.
 */
public class Identity {

    public String nickname, privateKey, publicKey, server, username, password;
    public int rowid;

    public Identity(String nickname, String privateKey, String publicKey, String server, String username, String password) {
        this.nickname = nickname; this.privateKey = privateKey; this.publicKey = publicKey;
        this.server = server; this.username = username; this.password = password;
    }

    public static Identity generate() {
        Identity i= new Identity("nobody", String.valueOf(Math.random()*100000000000f), String.valueOf(Math.random()*100000000000f),
                "", String.valueOf(Math.random()*100000000000f), "teamwiki.de");
        return i;
    }

    public String getPublicKeyHash() {
        return Identity.sha256(this.publicKey);
    }

    public static String sha256(String string) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            return null;
        }
        digest.reset();
        try {
            return digest.digest(string.getBytes("UTF-8")).toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }


    public ContentValues getContentValues(){
        ContentValues values = new ContentValues();
        values.put("nickname", nickname);
        values.put("privatekey", privateKey);
        values.put("publickey", publicKey);
        values.put("server", server);
        values.put("username", username);
        values.put("password", password);
        return values;
    }

    public static Identity fromCursor(Cursor cursor){
        Log.d("Identity", TextUtils.join(",",cursor.getColumnNames()));
        Identity id = new Identity(cursor.getString(cursor.getColumnIndex("nickname")),
                cursor.getString(cursor.getColumnIndex("privatekey")),
                cursor.getString(cursor.getColumnIndex("publickey")),
                cursor.getString(cursor.getColumnIndex("server")),
                cursor.getString(cursor.getColumnIndex("username")),
                cursor.getString(cursor.getColumnIndex("password")));
        id.rowid = cursor.getInt(cursor.getColumnIndex("rowid"));
        return id;
    }

}
