package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by johannes on 30.08.15.
 *
 * this async task is called on app startup
 * it sends messages that were pending retransmissions just before the app was closed
 */
public class ResendOldMessagesTask extends AsyncTask<Context, Void, Void> {

    private static String TAG = "ResendOldMessagesTask";

    @Override
    protected Void doInBackground(Context... contexts) {
        Context ctx = contexts[0];
        BonfireData db = BonfireData.getInstance(ctx);

        // fetch messages pending a retransmission
        ArrayList<Message> messages = db.getPendingMessages();

        // resend them
        for (Message message: messages) {
            // not already acked?
            if (!message.hasFlag(Message.FLAG_ACKNOWLEDGED)) {
                Log.d(TAG, "resending pending message with uuid: " + message.uuid);
                ConnectionManager.retrySendMessage(ctx, message);
                db.updateMessage(message);
            }
        }

        return null;
    }
}
