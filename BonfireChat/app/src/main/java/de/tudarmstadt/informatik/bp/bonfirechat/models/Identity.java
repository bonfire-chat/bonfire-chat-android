package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mw on 18.05.15.
 */
public class Identity {

    public String nickname, privateKey, publicKey, server, username, password, phone;
    public long rowid;

    public Identity(String nickname, String privateKey, String publicKey, String server, String username, String password, String phone) {
        this.nickname = nickname; this.privateKey = privateKey; this.publicKey = publicKey; this.phone = phone;
        this.server = server; this.username = username; this.password = password;
    }

    public static Identity generate(Context ctx) {
        // TODO generate a real key pair
        Identity i= new Identity("", String.valueOf(Math.random()*100000000000f), String.valueOf(Math.random()*100000000000f),
                "teamwiki.de", "", String.valueOf(Math.random()*100000000000f), getMyPhoneNumber(ctx));
        return i;
    }

    public String getPublicKeyHash() {
        return Identity.hash("MD5", this.publicKey);
        //return Identity.hash("SHA-256", this.publicKey);
    }

    public static String hash(String algorithm, String string) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            return null;
        }
        digest.reset();
        try {
            return Base64.encodeToString(digest.digest(string.getBytes("UTF-8")), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
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
        values.put("phone", phone);
        return values;
    }

    public static Identity fromCursor(Cursor cursor){
        Log.d("Identity", TextUtils.join(",", cursor.getColumnNames()));
        Identity id = new Identity(cursor.getString(cursor.getColumnIndex("nickname")),
                cursor.getString(cursor.getColumnIndex("privatekey")),
                cursor.getString(cursor.getColumnIndex("publickey")),
                cursor.getString(cursor.getColumnIndex("server")),
                cursor.getString(cursor.getColumnIndex("username")),
                cursor.getString(cursor.getColumnIndex("password")),
                cursor.getString(cursor.getColumnIndex("phone")));
        id.rowid = cursor.getInt(cursor.getColumnIndex("rowid"));
        return id;
    }

    public void registerWithServer() {

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://bonfire.teamwiki.net/register.php");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("nickname", nickname));
            nameValuePairs.add(new BasicNameValuePair("xmppid", username));
            nameValuePairs.add(new BasicNameValuePair("publickey", publicKey));
            nameValuePairs.add(new BasicNameValuePair("phone", phone));
            nameValuePairs.add(new BasicNameValuePair("gcmid", publicKey));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);


        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static String getMyPhoneNumber(Context ctx){
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

}
