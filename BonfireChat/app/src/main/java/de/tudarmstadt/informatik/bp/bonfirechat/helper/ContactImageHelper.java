package de.tudarmstadt.informatik.bp.bonfirechat.helper;

import android.net.Uri;
import android.widget.ImageView;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.models.IPublicIdentity;

/**
 * Created by jonas on 03.09.15.
 */
public class ContactImageHelper {

    public static void displayContactImage(IPublicIdentity contact, ImageView icon){
        displayContactImage(contact.getImage(), icon);
    }

    public static void displayContactImage(String image, ImageView icon){
        if(image.equals("")) {
            icon.setImageResource(R.mipmap.ic_launcher);
        }
        else
            icon.setImageURI(Uri.parse(image));
    }
}
