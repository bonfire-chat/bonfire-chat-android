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
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;

/**
 * Created by jonas on 09.07.15.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConversationTest {

    Conversation conversation;
    Contact contact;

    @Before
    public void initTests(){
       conversation = new Conversation(contact, "Title", 42);
    }

    
}
