package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import com.google.android.gms.maps.model.LatLng;

import org.abstractj.kalium.crypto.Box;
import org.abstractj.kalium.keys.PublicKey;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;

/**
 * Created by johannes on 30.08.15.
 *
 * packet to share your location with friends on a regular schedule
 * these don't generate ACK packets and therefore are a distinct packet type
 */
public class LocationUdpPacket extends Packet {

    public final Date sentTime;
    public byte[] encryptedBody;
    public byte[] nonce;


    public LocationUdpPacket(Identity sender, byte[] recipientPublicKey, LatLng location) {
        super(sender.getPublicKey().asByteArray(), recipientPublicKey, UUID.randomUUID());
        this.type = PacketType.LocationUdp;
        this.sentTime = new Date();

        // create location payload
        String body = location.latitude + ":" + location.longitude;

        // encrypt payload
        Box crypto = new Box(new PublicKey(recipientPublicKey), sender.privateKey);
        nonce = CryptoHelper.generateNonce();
        encryptedBody = crypto.encrypt(nonce, body.getBytes(Charset.forName("UTF-8")));
    }
}
