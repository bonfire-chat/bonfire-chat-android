package de.tudarmstadt.informatik.bp.bonfirechat.models.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.abstractj.kalium.keys.KeyPair;
import org.abstractj.kalium.keys.PublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import de.tudarmstadt.informatik.bp.bonfirechat.models.MyPublicKey;

/**
 * Created by jonas on 16.07.15.
 */
@RunWith(MockitoJUnitRunner.class)
public class MyPublicKeyTest {

    KeyPair keyPairMock;
    MyPublicKey myPublicKey;
    PublicKey publicKeyMock;

    @Before
    public void initTests(){
        keyPairMock = Mockito.mock(KeyPair.class);
        publicKeyMock = Mockito.mock(org.abstractj.kalium.keys.PublicKey.class);
        when(keyPairMock.getPublicKey()).thenReturn(publicKeyMock);
        myPublicKey = new MyPublicKey(publicKeyMock);
    }

    /**
     * Tests correct behaviour of get()
     * <br><br>
     * get() should return the public key myPublicKey was initialized with
     */
    @Test
    public void testGet(){
        assertEquals(publicKeyMock, myPublicKey.get());
    }

    /**
     * Tests the correct conversion of the public key to a byte array
     */
    @Test
    public void testAsByteArray(){
        assertEquals(publicKeyMock.toBytes(), myPublicKey.asByteArray());
    }
}
