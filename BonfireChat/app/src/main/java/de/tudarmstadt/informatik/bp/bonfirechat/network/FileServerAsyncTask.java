package de.tudarmstadt.informatik.bp.bonfirechat.network;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Simon on 22.05.2015.
 */
public class FileServerAsyncTask extends AsyncTask {

    private Context context;
    private TextView statusText;

    public FileServerAsyncTask(Context context, View statusText) {
        this.context = context;
        this.statusText = (TextView) statusText;
    }



    @Override
    protected Object doInBackground(Object[] params) {
        try {
            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            ServerSocket serverSocket = new ServerSocket(4242);
            Socket client = serverSocket.accept();

            InputStream inputstream = client.getInputStream();
            inputstream.read();

        } catch (IOException e) {
        }
        return null;
    }
}
