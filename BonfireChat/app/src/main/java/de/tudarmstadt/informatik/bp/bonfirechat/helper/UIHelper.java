package de.tudarmstadt.informatik.bp.bonfirechat.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import de.tudarmstadt.informatik.bp.bonfirechat.data.ConstOptions;

/**
 * Created by mw on 21.05.15.
 */
public class UIHelper {

    public interface OnOkClickListener {
        void onOkClicked(String input);
    }

    public static void InputBox(Context ctx, String title, String message, String defaultValue, final OnOkClickListener okListener) {
        final EditText input = new EditText(ctx);
        input.setSingleLine();
        FrameLayout container = new FrameLayout(ctx);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //params.leftMargin = 60;
        //params.rightMargin = 60;
        input.setLayoutParams(params);
        input.setText(defaultValue);
        container.addView(input);
        AlertDialog.Builder b = new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        okListener.onOkClicked(input.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                })
                        //.setIcon(R.mipmap.ic_launcher)
                .setView(container);
        AlertDialog d = b.create();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        d.show();
    }
    public static void Info(Context ctx, String title, String message) {
        AlertDialog.Builder b = new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog d = b.create();
        d.show();
    }



    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    public static final String PREF_SHOW_OOBE = "show_oobe";
    public static final String PREF_SHOW_CONTACTS_SHOWCASE = "show_contacts_showcase";
    public static final String PREF_SHOW_CONVERSATION_SHOWCASE = "show_conversation_showcase";

    public static boolean shouldShowOobe(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SHOW_OOBE, true);
    }

    public static boolean shouldShowContactsTutorial(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SHOW_CONTACTS_SHOWCASE, true);
    }
    public static boolean shouldShowConversationTutorial(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SHOW_CONVERSATION_SHOWCASE, true);
    }

    public static void flagShownContactsTutorial(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREF_SHOW_CONTACTS_SHOWCASE, false);
        editor.apply();
    }
    public static void flagShownConversationTutorial(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREF_SHOW_CONVERSATION_SHOWCASE, false);
        editor.apply();
    }

}
