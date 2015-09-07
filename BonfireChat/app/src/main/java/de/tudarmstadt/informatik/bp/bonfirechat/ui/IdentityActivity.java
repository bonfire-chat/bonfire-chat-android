package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.StringHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

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

        getEdit(R.id.nickname).setText(identity.nickname);
        getEdit(R.id.phone).setText(identity.phone);
        ImageView contactImage = ((ImageView)findViewById(R.id.contact_image));
        if(!identity.getImage().equals(""))
            contactImage.setImageURI(Uri.parse("file://" + identity.getImage()));

        if (!isWelcomeScreen) {
            String pubkey = identity.getPublicKey().asBase64();
            pubkey = pubkey.substring(0, 21) + "\n" + pubkey.substring(22);
            ((TextView) findViewById(R.id.publickey)).setText(pubkey);
        }

        Button saveButton = (Button) findViewById(R.id.save);
        if (saveButton != null ) saveButton.setOnClickListener(onSaveButtonClicked);

        ImageView imageView = (ImageView) findViewById(R.id.contact_image);
        if(imageView != null) imageView.setOnClickListener(onImageClicked);
    }

    private EditText getEdit(int id) {
        return ((EditText) findViewById(id));
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

            final EditText nickname = getEdit(R.id.nickname);
            final EditText phone = getEdit(R.id.phone);
            final View boxRegistering = findViewById(R.id.linearLayoutRegistering);
            final View boxError = findViewById(R.id.linearLayoutError);
            final View button = findViewById(R.id.save);

            // validate user input
            if (!(StringHelper.regexMatch("\\w+", nickname.getText().toString()) &&
                    (phone.getText().toString().isEmpty() || StringHelper.regexMatch("\\+?\\d+", phone.getText().toString())))) {

                if (!phone.getText().toString().isEmpty() && !StringHelper.regexMatch("\\+?\\d+", phone.getText().toString())) {
                    phone.setError(getString(R.string.phone_error));
                    phone.requestFocus();
                }
                // validate nickname at last, so that it will be the one error message
                // to be shown (the others will collapse to a red error sign)
                if (!StringHelper.regexMatch("\\w+", nickname.getText().toString())) {
                    nickname.setError(getString(R.string.nickname_error));
                    nickname.requestFocus();
                }
                return;

            } else {
                nickname.setError(null);
                phone.setError(null);
            }

            // register with BonfireAPI server
            identity.nickname = nickname.getText().toString();
            identity.phone = phone.getText().toString();

            if (isWelcomeScreen) {
                boxRegistering.setVisibility(View.VISIBLE);
                boxError.setVisibility(View.GONE);
                button.setEnabled(false);
            }
            new AsyncTask<Identity, Object, String>() {
                @Override
                protected String doInBackground(Identity... params) {
                    String ok = params[0].registerWithServer();
                    return ok;
                }
                @Override
                protected void onPostExecute(String s) {
                    if (isWelcomeScreen) {
                        boxRegistering.setVisibility(View.GONE);
                        button.setEnabled(true);
                    }
                    if (s != null ) {
                        // nickname already taken?
                        if (s.contains("Duplicate entry")) {
                            nickname.setError(getString(R.string.nickname_already_taken));
                            nickname.requestFocus();
                        }
                        // other error
                        else {
                            if (isWelcomeScreen) boxError.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // save identity
                        final BonfireData db = BonfireData.getInstance(IdentityActivity.this);
                        db.updateIdentity(identity);
                        // close wlcome screen
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

    private View.OnClickListener onImageClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 42);
        }
    };

    public class myAsyncTask extends AsyncTask<Identity, Object, String> {

        private Context ctx;

        public myAsyncTask(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(Identity... params) {
            String ok = params[0].updateImage(ctx);
            return ok;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42 && data != null) {

            File file = Message.getImageFile(UUID.randomUUID());

            try {
                FileOutputStream out = new FileOutputStream(file);
                StreamHelper.writeImageToStream(this.getContentResolver(), data.getData(), out);
                out.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            String filename = file.getAbsolutePath();
            identity.setImage(filename);
            final BonfireData db = BonfireData.getInstance(IdentityActivity.this);
            db.updateIdentity(identity);

            new myAsyncTask(this).execute(identity);

            ((ImageView) findViewById(R.id.contact_image)).setImageURI(Uri.parse("file://" + identity.getImage()));
        }
    }

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

