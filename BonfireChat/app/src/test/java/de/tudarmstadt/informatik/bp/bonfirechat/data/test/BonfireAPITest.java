package de.tudarmstadt.informatik.bp.bonfirechat.data.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireAPI;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.CryptoHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.MyPublicKey;

/**
 * Created by jonas on 30.07.15.
 */
@RunWith(MockitoJUnitRunner.class)
public class BonfireAPITest {

    /**
     * tests valid encoding of a String
     * <br><br>
     * String will be encoded and the resulting byte array should match the pre-computed
     * correct byte array.
     */
    @Test
    public void testEncode(){
        byte []result = {97, 98, 99, 100, 101, 102, 103};
        assertArrayEquals(result, BonfireAPI.encode("abcdefg"));
    }


    /**
     * tests, that a new contact is correctly created if a new phone contact is found in
     * the address book
     */
//    @Test
//    public void testOnNewPhoneContact() {
//        String randomKey = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
//
//        BonfireData mock = Mockito.mock(BonfireData.class);
//        when(mock.getContactByPublicKey("PUBKEY")).thenReturn(null);
//        BonfireAPI.onNewPhoneContact(mock, "PHONE-NUMBER", randomKey, "NICKNAME");
//        ArgumentCaptor<Contact> argumentCaptor = ArgumentCaptor.forClass(Contact.class);
//        verify(mock).createContact(argumentCaptor.capture());
//        assertEquals(argumentCaptor.getValue().getNickname(), "NICKNAME");
//        assertEquals(argumentCaptor.getValue().getPhoneNumber(), "PHONE-NUMBER");
//        assertEquals(argumentCaptor.getValue().getPublicKey(), randomKey);
//    }
}
