package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.abstractj.kalium.crypto.Box;
import org.abstractj.kalium.keys.KeyPair;
import org.abstractj.kalium.keys.PrivateKey;

import java.io.BufferedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.network.gcm.GcmBroadcastReceiver;

/**
 * Created by mw on 18.05.15.
 */
public class Identity implements IPublicIdentity {

    private static final String TAG = "Identity";

    // the publickey is the globally unique identifier for a person/device
    final public MyPublicKey publicKey;
    final public PrivateKey privateKey;
    public String nickname;
    public String phone;
    public long rowid;

    public Identity(String nickname, String privateKey, String publicKey, String phone) {
        this.nickname = nickname; this.phone = phone;
        this.privateKey = new PrivateKey(privateKey);
        this.publicKey = MyPublicKey.deserialize(publicKey);
    }

    public static Identity generate(Context ctx) {
        KeyPair keyPair = CryptoHelper.generateKeyPair();
        String pubkey = Base64.encodeToString(keyPair.getPublicKey().toBytes(), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        String privkey = keyPair.getPrivateKey().toString();

        Identity i= new Identity("", privkey, pubkey, getMyPhoneNumber(ctx));
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
    public String getPhoneNumber() {
        return phone;
    }


    public ContentValues getContentValues(){
        ContentValues values = new ContentValues();
        values.put("nickname", nickname);
        values.put("privatekey", privateKey.toString());
        values.put("publickey", publicKey.asBase64());
        values.put("phone", phone);
        return values;
    }

    public static Identity fromCursor(Cursor cursor){
        Log.d("Identity", TextUtils.join(",", cursor.getColumnNames()));
        Identity id = new Identity(cursor.getString(cursor.getColumnIndex("nickname")),
                cursor.getString(cursor.getColumnIndex("privatekey")),
                cursor.getString(cursor.getColumnIndex("publickey")),
                cursor.getString(cursor.getColumnIndex("phone")));
        id.rowid = cursor.getInt(cursor.getColumnIndex("rowid"));
        return id;
    }

    public String registerWithServer() {

        HttpURLConnection urlConnection = null;
        try {

            String plaintext = "nickname=" + URLEncoder.encode(nickname, "UTF-8")
                    + "&phone=" + URLEncoder.encode(phone, "UTF-8")
                    + "&gcmid=" + URLEncoder.encode(GcmBroadcastReceiver.regid, "UTF-8");

            Box b = new Box(BonfireData.SERVER_PUBLICKEY, privateKey);
            byte[] nonce = CryptoHelper.generateNonce();
            String ciphertext = CryptoHelper.toBase64(b.encrypt(nonce, plaintext.getBytes("UTF-8")));

            String postData = "publickey=" + publicKey.asBase64()
                    + "&nonce=" + CryptoHelper.toBase64(nonce)
                    + "&data=" + ciphertext;

            urlConnection = (HttpURLConnection) new URL(BonfireData.API_ENDPOINT + "/register").openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(postData.getBytes("UTF-8"));
            out.flush();

            String theString = StreamHelper.convertStreamToString(urlConnection.getInputStream());
            Log.d(TAG, "registered with server : " + urlConnection.getResponseCode());
            Log.i(TAG, theString);
            if (urlConnection.getResponseCode() == 200) return null;
            return theString;
        } catch (Exception e) {
            e.printStackTrace();
            if (urlConnection == null) return e.toString();
            return StreamHelper.convertStreamToString(urlConnection.getErrorStream());
        } finally {
            if(urlConnection != null) urlConnection.disconnect();
        }
    }

    private static String getMyPhoneNumber(Context ctx){
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

}
