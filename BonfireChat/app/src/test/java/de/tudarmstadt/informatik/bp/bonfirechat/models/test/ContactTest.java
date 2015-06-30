package de.tudarmstadt.informatik.bp.bonfirechat.models.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;

/**
 * Created by jonas on 30.06.15.
 */
public class ContactTest {

    @Test
    public void testToString(){
        Contact contact = new Contact("nickname", "", "", "", "", "", "", "", 0);
        assertEquals("nickname", contact.toString());
    }

}
