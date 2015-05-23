package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;

/**
 * A login screen that offers login via email/password and via Google+ sign in.
 * <p/>
 * ************ IMPORTANT SETUP NOTES: ************
 * In order for Google+ sign in to work with your app, you must first go to:
 * https://developers.google.com/+/mobile/android/getting-started#step_1_enable_the_google_api
 * and follow the steps in "Step 1" to create an OAuth 2.0 client for your package.
 */
public class IdentityActivity extends Activity  {

    //public static final String EXTRA_IDENTITY_ID = "IdentityId";

    Identity identity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity);
        if (getIntent().hasExtra("isWelcomeScreen")) {
            getActionBar().setTitle("Willkommen!");
        }else {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("Account bearbeiten");
        }

        BonfireData db = BonfireData.getInstance(this);
        identity = db.getDefaultIdentity();

        getEdit(R.id.txt_nickname).setText(identity.nickname);
        getEdit(R.id.email).setText(identity.username);
        getEdit(R.id.password).setText(identity.password);
        ((TextView)findViewById(R.id.textView3)).setText("Public Key:\n"+identity.publicKey);
        getEdit(R.id.txt_phoneNumber).setText(identity.phone);

    }

    private EditText getEdit(int id) {
        return ((EditText)findViewById(id));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            identity.nickname = getEdit(R.id.txt_nickname).getText().toString();
            identity.username = getEdit(R.id.email).getText().toString();
            identity.password = getEdit(R.id.password).getText().toString();
            identity.phone = getEdit(R.id.txt_phoneNumber).getText().toString();
            BonfireData db = BonfireData.getInstance(this);
            db.updateIdentity(identity);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    identity.registerWithServer();
                }
            }).start();
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

