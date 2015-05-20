package de.tudarmstadt.informatik.bp.bonfirechat.network;


import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.io.IOException;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by mw on 13.05.15.
 */
public class ClientServerProtocol implements IProtocol {
    XMPPTCPConnection connection;
    Identity identity;

    public ClientServerProtocol(Identity i) {


    }

    public void connectToServer() {


        connection = new XMPPTCPConnection(identity.username, identity.password, identity.server);
        try {
            connection.connect();

            AccountManager a = AccountManager.getInstance(connection);
            //a.createAccount();

            connection.login();
            //connection.

        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sendMessage(String target, Message message) {

    }

    @Override
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {

    }
}
