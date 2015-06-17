package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;

import org.abstractj.kalium.keys.KeyPair;
import org.abstractj.kalium.keys.PrivateKey;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.network.gcm.GcmBroadcastReceiver;

/**
 * Created by mw on 18.05.15.
 */
public class Identity implements IPublicIdentity {

    private static final String TAG = "Identity";
    public String nickname, server, username, password, phone;
    public MyPublicKey publicKey;
    public PrivateKey privateKey;
    public long rowid;

    public Identity(String nickname, String privateKey, String publicKey, String server, String username, String password, String phone) {
        this.nickname = nickname; this.phone = phone; this.username = username; this.password = password;
        this.privateKey = new PrivateKey(privateKey);
        this.publicKey = MyPublicKey.deserialize(publicKey);
        this.server = server;
    }

    public static Identity generate(Context ctx) {
        KeyPair keyPair = CryptoHelper.generateKeyPair();
        String pubkey = Base64.encodeToString(keyPair.getPublicKey().toBytes(), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        String privkey = keyPair.getPrivateKey().toString();

        Identity i= new Identity("", privkey, pubkey,
                "teamwiki.de", "", String.valueOf(Math.random()*100000000000f), getMyPhoneNumber(ctx));
        return i;
    }


    public MyPublicKey getPublicKey() {
        return publicKey;
    }


    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public String getXmppId() {
        return username;
    }

    @Override
    public String getPhoneNumber() {
        return phone;
    }


    public ContentValues getContentValues(){
        ContentValues values = new ContentValues();
        values.put("nickname", nickname);
        values.put("privatekey", privateKey.toString());
        values.put("publickey", publicKey.asBase64());
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

    public String registerWithServer() {

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BonfireData.API_ENDPOINT + "/register.php");

        try {

            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("nickname", nickname));
            nameValuePairs.add(new BasicNameValuePair("xmppid", username));
            nameValuePairs.add(new BasicNameValuePair("publickey", publicKey.asBase64()));
            nameValuePairs.add(new BasicNameValuePair("phone", phone));
            nameValuePairs.add(new BasicNameValuePair("gcmid", GcmBroadcastReceiver.regid));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            Log.d(TAG, "registered with server : " + response.getStatusLine().toString());
            String firstLine = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).readLine();
            Log.d(TAG, "registered with server : " + firstLine);

            if (response.getStatusLine().getStatusCode() != 200) {
                return firstLine;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: "+e.getMessage();
        }
    }

    private static String getMyPhoneNumber(Context ctx){
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

}
