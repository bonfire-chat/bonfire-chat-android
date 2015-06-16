package de.tudarmstadt.informatik.bp.bonfirechat.models;

import android.content.Context;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * Created by johannes on 15.06.15.
 *
 * An Envelope represents a message on its way. Envelopes are for being sent and recieved and
 * should describe a message uniquely, including network parameters.
 */
public class Envelope implements Serializable {

    public UUID uuid;
    public int hopCount;
    public Date sentTime;
    public ArrayList<byte[]> recipientsPublicKeys;
    //TODO eventuell rauswerfen
    public String senderNickname;
    public byte[] senderPublicKey;
    public byte[] encryptedBody;
    public int flags;

    public static final int FLAG_ENCRYPTED = 4;
    public static final int FLAG_TRACEROUTE = 8;


    public Envelope(UUID uuid, int hopCount, Date sentTime, ArrayList<byte[]> recipientsPublicKeys, String senderNickname, byte[] senderPublicKey, byte[] encryptedBody) {
        this.uuid = uuid;
        this.hopCount = hopCount;
        this.sentTime = sentTime;
        this.recipientsPublicKeys = recipientsPublicKeys;
        this.senderNickname = senderNickname;
        this.senderPublicKey = senderPublicKey;
        this.encryptedBody = encryptedBody;
    }


    public static Envelope fromMessage(Message message) {
        ArrayList<byte[]> publicKeys = new ArrayList<>();
        for(Contact recipient: message.recipients) {
            publicKeys.add(recipient.getPublicKey().asByteArray());
        }
        Envelope envelope = new Envelope(
                message.uuid,
                0,
                new Date(),
                publicKeys,
                message.sender.getNickname(),
                message.sender.getPublicKey().asByteArray(),
                message.body.getBytes(Charset.forName("UTF-8")));
        return envelope;
    }

    public Message toMessage(Context ctx) {
        return new Message(
                new String(encryptedBody, Charset.forName("UTF-8")),
                Contact.findOrCreate(ctx, senderPublicKey, senderNickname),
                sentTime,
                uuid);
    }


    public boolean containsRecipient(Identity id) {
        for (byte[] publicKey: recipientsPublicKeys) {
            if (Arrays.equals(publicKey, id.publicKey.asByteArray())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFlag(int flag) {
        return (flag & flags) == flag;
    }
}
