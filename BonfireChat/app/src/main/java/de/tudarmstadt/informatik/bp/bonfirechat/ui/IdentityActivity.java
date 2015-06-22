package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
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
        String pubkey = identity.getPublicKey().asBase64();
        pubkey = pubkey.substring(0,21) + "\n" + pubkey.substring(22);
        ((TextView)findViewById(R.id.txt_publicKey)).setText(pubkey);
        getEdit(R.id.txt_phoneNumber).setText(identity.phone);

    }

    private EditText getEdit(int id) {
        return ((EditText)findViewById(id));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_identity_details, menu);
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
            identity.phone = getEdit(R.id.txt_phoneNumber).getText().toString();
            BonfireData db = BonfireData.getInstance(this);
            db.updateIdentity(identity);

            final AlertDialog progress =
                new ProgressDialog.Builder(this)
                    .setTitle("Registering ...")
                    .setMessage("This will take only a few seconds.")
                    .show();
            new AsyncTask<Identity, Object, String>() {
                @Override
                protected String doInBackground(Identity... params) {
                    return params[0].registerWithServer();
                }
                @Override
                protected void onPostExecute(String s) {
                    progress.dismiss();
                    if (s != null ) {
                        new AlertDialog.Builder(IdentityActivity.this)
                                .setTitle("Registering failed")
                                .setMessage(s)
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    } else {

                        finish();
                    }
                }
            }.execute(identity);

            SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(this).edit();
            preferences.putString("my_nickname", identity.nickname);
            preferences.commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

