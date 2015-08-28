package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StringHelper;
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

    boolean isWelcomeScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra("isWelcomeScreen")) {
            isWelcomeScreen = true;
            setContentView(R.layout.activity_identity_welcome);
            getActionBar().setTitle(getString(R.string.title_welcome));
            getActionBar().hide();
        }else {
            setContentView(R.layout.activity_identity);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(getString(R.string.title_edit_account));
        }

        BonfireData db = BonfireData.getInstance(this);
        identity = db.getDefaultIdentity();

        getEdit(R.id.txt_nickname).setText(identity.nickname);
        String pubkey = identity.getPublicKey().asBase64();
        pubkey = pubkey.substring(0,21) + "\n" + pubkey.substring(22);
        TextView txtPublickey = (TextView)findViewById(R.id.txt_publicKey);
        if (txtPublickey != null) txtPublickey.setText(pubkey);
        getEdit(R.id.txt_phoneNumber).setText(identity.phone);

        Button saveButton = (Button) findViewById(R.id.save);
        if (saveButton != null ) saveButton.setOnClickListener(onSaveButtonClicked);

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

    private View.OnClickListener onSaveButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            EditText nickname = getEdit(R.id.txt_nickname),
                     phone = getEdit(R.id.txt_phoneNumber);

            // validate user input
            if (! (StringHelper.regexMatch("\\w+", nickname.getText().toString()) &&
                    StringHelper.regexMatch("\\d+", phone.getText().toString())) ) {

                if (!StringHelper.regexMatch("\\d+", phone.getText().toString())) {
                    phone.setError(getString(R.string.phone_error));
                }
                // validate nickname at last, so that it will be the one error message
                // to be shown (the others will collapse to a red error sign)
                if (!StringHelper.regexMatch("\\w+", nickname.getText().toString())) {
                    nickname.setError(getString(R.string.nickname_error));
                }
                return;

            } else {
                nickname.setError(null);
                phone.setError(null);
            }

            // register with BonfireAPI server
            identity.nickname = nickname.getText().toString();
            identity.phone = phone.getText().toString();
            final BonfireData db = BonfireData.getInstance(IdentityActivity.this);
            db.updateIdentity(identity);

            final AlertDialog progress =
                    new ProgressDialog.Builder(IdentityActivity.this)
                            .setTitle(getString(R.string.registering_title))
                            .setMessage(getString(R.string.registering_message))
                            .show();
            new AsyncTask<Identity, Object, String>() {
                @Override
                protected String doInBackground(Identity... params) {
                    String ok = params[0].registerWithServer();
                    db.updateIdentity(params[0]);
                    return ok;
                }
                @Override
                protected void onPostExecute(String s) {
                    progress.dismiss();
                    if (s != null ) {
                        new AlertDialog.Builder(IdentityActivity.this)
                                .setTitle(getString(R.string.registering_failed))
                                .setMessage(s)
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    } else {
                        finish();
                        if (isWelcomeScreen) {
                            startActivity(new Intent(IdentityActivity.this, MainActivity.class));
                        }
                    }
                }
            }.execute(identity);

            SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(IdentityActivity.this).edit();
            preferences.putString("my_nickname", identity.nickname);
            preferences.commit();

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            onSaveButtonClicked.onClick(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

