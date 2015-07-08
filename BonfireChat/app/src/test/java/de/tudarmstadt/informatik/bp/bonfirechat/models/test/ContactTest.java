package de.tudarmstadt.informatik.bp.bonfirechat.models.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.MyPublicKey;
import org.abstractj.kalium.keys.KeyPair;

/**
 * Created by jonas on 30.06.15.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContactTest {

    public org.abstractj.kalium.keys.PublicKey publicKeyMock;
    Contact contact;

    @Before
    public void initTests(){
        KeyPair keyPairMock = Mockito.mock(KeyPair.class);
        org.abstractj.kalium.keys.PublicKey publicKeyMock = Mockito.mock(org.abstractj.kalium.keys.PublicKey.class);
        when(keyPairMock.getPublicKey()).thenReturn(publicKeyMock);
        contact = new Contact("nickname", "", "", "", new MyPublicKey(publicKeyMock), "", "", "", 0);
    }

    @Test
    public void testToString(){
        assertEquals("nickname", contact.toString());
    }

}
