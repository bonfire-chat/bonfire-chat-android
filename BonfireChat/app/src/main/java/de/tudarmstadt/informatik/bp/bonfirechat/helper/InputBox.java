package de.tudarmstadt.informatik.bp.bonfirechat.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import de.tudarmstadt.informatik.bp.bonfirechat.R;

/**
 * Created by mw on 21.05.15.
 */
public class InputBox {

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
}
