package de.tudarmstadt.informatik.bp.bonfirechat.ui;

/**
 * Created by jonas on 16.06.15.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.ContactImageHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.zxing.IntentIntegrator;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.zxing.IntentResult;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.zxing.QRCodeEncoder;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.zxing.QRcodeHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.IPublicIdentity;

public class ShareMyIdentityActivity extends Activity implements CreateNdefMessageCallback {
    NfcAdapter mNfcAdapter;
    TextView textView;
    BonfireData db;
    IPublicIdentity pubident;
    public static final String EXTRA_CONTACT_ID = "ContactId";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        db = BonfireData.getInstance(this);
        pubident = db.getDefaultIdentity();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        ContactImageHelper.displayContactImage(pubident, (ImageView) findViewById(R.id.imageView2));

        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, getString(R.string.nfc_is_not_available), Toast.LENGTH_LONG).show();
            findViewById(R.id.nfcNotice).setVisibility(View.GONE);
        } else {
            // Register callback
            mNfcAdapter.setNdefPushMessageCallback(this, this);
        }

        ((TextView) findViewById(R.id.txt_nickname)).setText(pubident.getNickname());

        ImageView imageView = (ImageView) findViewById(R.id.qrCode);

        final int qrCodeDimension = 800;

        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(QRcodeHelper.getIdentityURL(pubident), null,
                "TEXT_TYPE", BarcodeFormat.QR_CODE.toString(), qrCodeDimension);

        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String url = QRcodeHelper.getIdentityURL(pubident);
        NdefMessage msg = null;
        try {
            msg = new NdefMessage(
                    new NdefRecord[] {NdefRecord.createMime(
                            "application/de.tudarmstadt.informatik.bp.bonfirechat", url.getBytes("UTF-8")),
                            NdefRecord.createApplicationRecord("de.tudarmstadt.informatik.bp.bonfirechat")
                    });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return msg;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    void processIntent(Intent intent) {
        textView = (TextView) findViewById(R.id.action_scan_nfc);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        Log.d("TEST", new String(msg.getRecords()[0].getPayload(), Charset.defaultCharset()));
        Contact contact = null;
        try {
            contact = contactFromUri(Uri.parse(new String(msg.getRecords()[0].getPayload(), "UTF-8")));
            Contact dbContact = db.getContactByPublicKey(contact.getPublicKey().asBase64());
            if (dbContact == null) {
                db.createContact(contact);
            } else {
                contact = dbContact;
            }
            Intent intent1 = new Intent(this, ContactDetailsActivity.class);
            intent1.putExtra(EXTRA_CONTACT_ID, contact.rowid);
            startActivity(intent1);
            finish();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static Contact contactFromUri(Uri url) {
        Contact contact = new Contact(
                url.getQueryParameter("name"), "", "", url.getQueryParameter("tel"),
                url.getQueryParameter("key"), "", "", 0);
        return contact;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sharemyidentity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_scan_qr) {
            IntentIntegrator inte = new IntentIntegrator(this);
            inte.initiateScan();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * handle results from qr code scanning
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            Intent ii = new Intent(this, ContactDetailsActivity.class);
            ii.setAction(Intent.ACTION_VIEW);
            ii.setData(Uri.parse(result.getContents()));
            startActivity(ii);
        }
    }
}
