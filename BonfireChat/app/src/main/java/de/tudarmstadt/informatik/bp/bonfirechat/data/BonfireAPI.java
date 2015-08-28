package de.tudarmstadt.informatik.bp.bonfirechat.data;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.abstractj.kalium.keys.PublicKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.TracerouteHopSegment;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.TracerouteSegment;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.routing.Envelope;

/**
 * Created by mw on 29.07.15.
 */
public class BonfireAPI {

    private static final String TAG = "BonfireAPI";

    /**
     * URL of the rendezvous server API endpoint
     */
    public static final String API_ENDPOINT = "https://bonfire.projects.teamwiki.net/api/v1";
    public static final PublicKey SERVER_PUBLICKEY = new PublicKey("7c2bbc4c4d292479de59a1168f3b102ac9869b9ee00beb92745571e36bbb0b43");

    public static final String METHOD_TRACEROUTE = "traceroute";
    public static final String METHOD_SEND_MESSAGE = "notify";
    public static final String METHOD_CHECK_CONTACTS = "phonecontacts";

    public static final String DOWNLOADS_DIRECTORY = "Bonfire Downloads\\";

    public static String httpGet(String apiMethod) throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(API_ENDPOINT + "/" + apiMethod).openConnection();
            final String theString = StreamHelper.convertStreamToString(urlConnection.getInputStream());
            Log.i(TAG, "successful HTTP Get request to "+apiMethod);
            Log.i(TAG, theString);
            return theString;
        } catch (IOException e) {
            String theErrMes = StreamHelper.convertStreamToString(urlConnection.getErrorStream());
            throw new IOException("HTTP Get request failed, Exception: "+e.toString()+", Body: "+theErrMes);
        } finally {
            if(urlConnection == null) urlConnection.disconnect();
        }
    }
    public static JSONObject httpGetJsonObject(String apiMethod) throws IOException {
        try {
            return new JSONObject(httpGet(apiMethod));
        } catch (JSONException e) {
            Log.e(TAG, "unable to parse JSON object");
            try { JSONObject o = new JSONObject("{}"); return o; } catch (Throwable t) { return null; }
        }
    }
    public static JSONArray httpGetJsonArray(String apiMethod) throws IOException {
        try {
            return new JSONArray(httpGet(apiMethod));
        } catch (JSONException e) {
            Log.e(TAG, "unable to parse JSON object");
            try { JSONArray o = new JSONArray("[]"); return o; } catch (Throwable t) { return null; }
        }
    }

    public static void httpGetToFile(String url, File target) throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            InputStream is = urlConnection.getInputStream();

            FileOutputStream os = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();

            Log.i(TAG, "successful HTTP Get file request to "+url);
            Log.i(TAG, target.getAbsolutePath());

        } catch (IOException e) {
            String theErrMes = StreamHelper.convertStreamToString(urlConnection.getErrorStream());
            throw new IOException("HTTP Get request failed, Exception: "+e.toString()+", Body: "+theErrMes);
        } finally {
            if(urlConnection == null) urlConnection.disconnect();
        }
    }

    public static String httpPost(String apiMethod, Hashtable<String, byte[]> body) throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(API_ENDPOINT + "/" + apiMethod).openConnection();
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=Je8PPsja_x");
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            final BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            for (Map.Entry<String, byte[]> part : body.entrySet()) {
                out.write(("--Je8PPsja_x\r\nContent-Disposition: form-data; name=\"" + part.getKey() + "\"\r\n\r\n").getBytes("UTF-8"));
                out.write(part.getValue());
                out.write(("\r\n").getBytes("UTF-8"));
            }
            out.flush();

            final String theString = StreamHelper.convertStreamToString(urlConnection.getInputStream());
            Log.i(TAG, "successful HTTP Post request to "+apiMethod);
            Log.i(TAG, theString);
            return theString;
        } catch (IOException e) {
            String theErrMes = StreamHelper.convertStreamToString(urlConnection.getErrorStream());
            throw new IOException("HTTP Post request failed, Exception: "+e.toString()+", Body: "+theErrMes);
        } finally {
            if(urlConnection == null) urlConnection.disconnect();
        }
    }
    public static byte[] encode(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException("This device does not support UTF-8, go away!");
        }
    }


    public static void publishTraceroute(Envelope envelope) {
        try {
            if (!envelope.hasFlag(Envelope.FLAG_TRACEROUTE)) return;

            Hashtable<String, byte[]> body = new Hashtable<>();
            body.put("uuid", encode(envelope.uuid.toString()));
            body.put("traceroute", envelope.encryptedBody);
            httpPost(METHOD_TRACEROUTE, body);
        } catch (IOException e) {
            Log.e(TAG, "Failed to publish traceroute, ignoring!");
            e.printStackTrace();
        }
    }

    public static List<TracerouteSegment> getTraceroute(UUID id) {
        try {
            JSONArray traceroute = httpGetJsonArray(METHOD_TRACEROUTE + "?uuid=" + id);
            List<TracerouteSegment> list = new ArrayList<>();

            // TODO: johannes: parse traceroute off the JSON response
            list.add(new Contact("Alice", "", "", "", "", null, "", "", 0));
            list.add(new TracerouteHopSegment(TracerouteHopSegment.ProtocolType.BLUETOOTH, new Date(new Date().getTime() - 3800), new Date()));
            list.add(new Contact("Eve", "", "", "", "", null, "", "", 0));
            list.add(new TracerouteHopSegment(TracerouteHopSegment.ProtocolType.GCM, new Date(new Date().getTime() - 5000), new Date()));
            list.add(new Contact("Bob", "", "", "", "", null, "", "", 0));

            return list;
        } catch (IOException e) {
            Log.e(TAG, "Failed to load traceroute for message uuid " + id);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public static void sendGcmMessage(Identity identity, byte[] targetPubkey, String nextHop, byte[] serializedEnvelope) throws IOException {
        String key = CryptoHelper.toBase64(targetPubkey);

        Hashtable<String, byte[]> body = new Hashtable<>();
        body.put("senderId", encode(String.valueOf(identity.getServerUid())));
        body.put("recipientPublicKey", encode(key));
        body.put("nextHopId", encode(nextHop));
        body.put("msg", serializedEnvelope);
        httpPost(METHOD_SEND_MESSAGE, body);
    }

    public static boolean downloadMapPreview(LatLng location, File cached) {
        // is this preview already cached?
        if (cached.exists()) {
            return true;
        } else {
            Log.d(TAG, "getMapPreviewAsFilename: cache miss");
            // build Google static map API URL
            String url = "http://maps.google.com/maps/api/staticmap?center=" +
                    location.latitude + "," + location.longitude +
                    "&markers=color:red%7C" +
                    location.latitude + "," + location.longitude +
                    "&zoom=15&size=150x150&sensor=false";

            // download map preview
            try {
                httpGetToFile(url, cached);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }


    public static int updateContacts(Context context) {
        List<String> numbers = new ArrayList<>();
        List<String> names = new ArrayList<>();
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            names.add(name);
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            numbers.add(phoneNumber);
        }
        phones.close();

        BonfireData db = BonfireData.getInstance(context);
        String self = db.getDefaultIdentity().getPublicKey().asBase64();

        Hashtable<String, byte[]> body = new Hashtable<>();
        body.put("numbers", encode(TextUtils.join(",", numbers)));
        int newContacts = 0;
        try {
            String result = httpPost(METHOD_CHECK_CONTACTS, body);
            String[] pubkeys = result.split("\\n");
            for(int i=0; i<pubkeys.length && i<names.size(); i++) {
                if ("".equals(pubkeys[i]))continue;
                if (self.equals(pubkeys[i]))continue;
                if (onNewPhoneContact(db, numbers.get(i), pubkeys[i], names.get(i)))
                    newContacts++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newContacts;
    }

    public static boolean onNewPhoneContact(BonfireData db, String phone, String pubkey, String nickname) {
        Contact contact = db.getContactByPublicKey(pubkey);
        if (contact == null) {
            contact = new Contact(nickname, "", "", phone, pubkey, "", "", "", 0);
            db.createContact(contact);
            return true;
        } else {
            // update phone number?
            return false;
        }
    }
}
