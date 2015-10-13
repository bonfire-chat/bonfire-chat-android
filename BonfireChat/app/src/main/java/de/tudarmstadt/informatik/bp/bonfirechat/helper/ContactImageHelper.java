package de.tudarmstadt.informatik.bp.bonfirechat.helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.models.IPublicIdentity;

/**
 * Created by jonas on 03.09.15.
 */
public final class ContactImageHelper {

    private ContactImageHelper() { }

    private static final String TAG = "ContactImageHelper";

    public static void displayContactImage(IPublicIdentity contact, ImageView icon) {
        displayContactImage(contact.getImage(), icon);
    }

    public static void displayContactImage(String image, ImageView icon) {
        if (image.equals("")) {
            icon.setImageResource(R.mipmap.ic_launcher);
        } else {
            icon.setImageURI(Uri.parse(image));
        }
    }

    public static void displayCompoundContactImage(Context context, IPublicIdentity contact, TextView name) {
        displayCompoundContactImage(context, contact.getImage(), name);
    }

    public static void displayCompoundContactImage(Context context, String image, TextView name) {
        if (image.equals("")) {
            name.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_launcher, 0, 0, 0);
        } else {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(image));
                Drawable drawable = Drawable.createFromStream(inputStream, Uri.parse(image).toString());
                name.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "failed to create Drawable from URI: " + image);
                name.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_launcher, 0, 0, 0);
            }
        }
    }
}
