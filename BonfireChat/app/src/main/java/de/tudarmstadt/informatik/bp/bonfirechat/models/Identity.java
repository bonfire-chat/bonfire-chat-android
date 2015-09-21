package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Base64;

import org.abstractj.kalium.crypto.Box;
import org.abstractj.kalium.keys.KeyPair;
import org.abstractj.kalium.keys.PrivateKey;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Hashtable;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireAPI;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.network.GcmBroadcastReceiver;

import static java.lang.Integer.parseInt;

/**
 * Created by mw on 18.05.15.
 */
public class Identity implements IPublicIdentity {

    private static final String TAG = "Identity";

    // the publickey is the globally unique identifier for a person/device
    public final MyPublicKey publicKey;
    public final PrivateKey privateKey;
    public String nickname;
    public String phone;
    public int serverUid;
    public long rowid;

    private String image;

    public Identity(String nickname, int serverUid, String privateKey, String publicKey, String phone) {
        this.nickname = nickname; this.phone = phone;
        this.serverUid = serverUid;
        this.privateKey = new PrivateKey(privateKey);
        this.publicKey = MyPublicKey.deserialize(publicKey);
        image = "";
    }

    public static Identity generate(Context ctx) {
        KeyPair keyPair = CryptoHelper.generateKeyPair();
        String pubkey = Base64.encodeToString(keyPair.getPublicKey().toBytes(), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        String privkey = keyPair.getPrivateKey().toString();

        Identity i = new Identity("", 0, privkey, pubkey, getMyPhoneNumber(ctx));
        return i;
    }


    public MyPublicKey getPublicKey() {
        return publicKey;
    }

    public int getServerUid() {
        return serverUid;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public String getPhoneNumber() {
        return phone;
    }


    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put("nickname", nickname);
        values.put("username", serverUid);
        values.put("privatekey", privateKey.toString());
        values.put("publickey", publicKey.asBase64());
        values.put("phone", phone);
        values.put("image", image);
        return values;
    }

    public static Identity fromCursor(Cursor cursor) {
        Identity id = new Identity(cursor.getString(cursor.getColumnIndex("nickname")),
                cursor.getInt(cursor.getColumnIndex("username")),
                cursor.getString(cursor.getColumnIndex("privatekey")),
                cursor.getString(cursor.getColumnIndex("publickey")),
                cursor.getString(cursor.getColumnIndex("phone")));
        id.rowid = cursor.getInt(cursor.getColumnIndex("rowid"));
        id.setImage(cursor.getString(cursor.getColumnIndex("image")));
        return id;
    }

    public String registerWithServer() {
        try {
            String plaintext = "nickname=" + URLEncoder.encode(nickname, "UTF-8")
                    + "&phone=" + URLEncoder.encode(phone, "UTF-8")
                    + "&gcmid=" + URLEncoder.encode(GcmBroadcastReceiver.getRegid(), "UTF-8");

            Box b = new Box(BonfireAPI.SERVER_PUBLICKEY, privateKey);
            byte[] nonce = CryptoHelper.generateNonce();
            String ciphertext = CryptoHelper.toBase64(b.encrypt(nonce, plaintext.getBytes("UTF-8")));

            Hashtable<String, byte[]> body = new Hashtable<>();
            body.put("publickey", BonfireAPI.encode(publicKey.asBase64()));
            body.put("nonce", BonfireAPI.encode(CryptoHelper.toBase64(nonce)));
            body.put("data", BonfireAPI.encode(ciphertext));
            String result = BonfireAPI.httpPost("register", body).trim();
            if (result.startsWith("ok=")) {
                serverUid = parseInt(result.substring(3));
                return null;
            } else {
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public String updateImage(Context ctx) {
            Hashtable<String, byte[]> body = new Hashtable<>();
            body.put("publickey", BonfireAPI.encode(getPublicKey().asBase64()));

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                StreamHelper.writeImageToStream(ctx.getContentResolver(), Uri.parse("file://" + getImage()), os);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            body.put("image", os.toByteArray());
            try {
                BonfireAPI.httpPost("avatar", body);
            } catch (IOException e) {
                e.printStackTrace();
            }

        /*if (result.startsWith("ok=")) {
            serverUid = Integer.valueOf(result.substring(3));
            return null;
        } else {
            return result;
        }*/
        return "";
    }

    private static String getMyPhoneNumber(Context ctx) {
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
