package de.tudarmstadt.informatik.bp.bonfirechat.models.test;

import org.abstractj.kalium.keys.KeyPair;
import org.junit.Before;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.models.MyPublicKey;
import de.tudarmstadt.informatik.bp.bonfirechat.network.BluetoothProtocol;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

/**
 * Created by jonas on 16.07.15.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageTest {

    public Message message;
    public Contact contact;

    @Before
    public void initTests(){
        KeyPair keyPairMock = Mockito.mock(KeyPair.class);
        org.abstractj.kalium.keys.PublicKey publicKeyMock = Mockito.mock(org.abstractj.kalium.keys.PublicKey.class);
        when(keyPairMock.getPublicKey()).thenReturn(publicKeyMock);

        contact = new Contact("nickname", "Nick", "Name", "", new MyPublicKey(publicKeyMock), "", "", "", 0);
        message = new Message("body", contact, new Date(42), Message.FLAG_ENCRYPTED | Message.FLAG_PROTO_BT | Message.FLAG_PROTO_CLOUD | Message.FLAG_PROTO_WIFI, contact);
    }

    @Test
    public void testDirection(){
        assertEquals(Message.MessageDirection.Received, message.direction());
    }

    @Test
    public void testToString(){
        assertEquals("body", message.toString());
    }

    @Test
    public void testSetTransferProtocol(){
        message.setTransferProtocol(BluetoothProtocol.class);
        assertEquals(Message.FLAG_ENCRYPTED | Message.FLAG_PROTO_BT & ~(Message.FLAG_PROTO_CLOUD | Message.FLAG_PROTO_WIFI), message.flags);
    }

    @Test
    public void testHasFlag(){
        message.setTransferProtocol(BluetoothProtocol.class);
        assertTrue(message.hasFlag(Message.FLAG_PROTO_BT));
        assertFalse(message.hasFlag(Message.FLAG_PROTO_CLOUD));
    }
}
