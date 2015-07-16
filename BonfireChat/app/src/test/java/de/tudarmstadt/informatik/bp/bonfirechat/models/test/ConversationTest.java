package de.tudarmstadt.informatik.bp.bonfirechat.models.test;

import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.models.MyPublicKey;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by MAX
 */
@RunWith(MockitoJUnitRunner.class)
public class ConversationTest {

    Conversation conversation;
    Conversation conversation2;
    Contact contact;

    @Before
    public void initTests(){
        conversation = new Conversation(null, "Title", 42);

        MyPublicKey myPublicKey = Mockito.mock(MyPublicKey.class);
        contact = new Contact("","","","",myPublicKey,null,null,null,9876);
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new Message("Hello, world.", contact, new Date(2014,01,01), Message.FLAG_PROTO_CLOUD, new UUID(0,0)));
        conversation2 = new Conversation(contact, "Title2", 23, messages);
    }

    @Test
    public void testGetLastMessage() {
        assertEquals(conversation.getLastMessage(), "");
        assertEquals(conversation2.getLastMessage(), "Hello, world.");
    }

    @Test
    public void testAddMessages() {
        ArrayList<Message> newmessages = new ArrayList<>();
        newmessages.add(new Message("Test1", contact, new Date(2014, 01, 01), Message.FLAG_PROTO_CLOUD, new UUID(0,0)));
        newmessages.add(new Message("Test2", contact, new Date(2014, 01, 01), Message.FLAG_PROTO_CLOUD, new UUID(0,0)));
        conversation2.addMessages(newmessages);
        assertEquals(conversation2.getLastMessage(), "Test2");
    }

    /*
    @Test
    public void testGetContentValues() {
        ContentValues cv = conversation.getContentValues();
        assertFalse(cv.containsKey("peer"));
        assertEquals(cv.get("title"), "Title");
    }
    @Test
    public void testGetContentValuesWithPeer() {
        ContentValues cv = conversation.getContentValues();
        assertEquals(cv.get("peer"), 9876);
        assertEquals(cv.get("title"), "Title2");
    }
    */

    @Test
    public void testFromCursor() {
        Cursor cursor = Mockito.mock(Cursor.class);
        when(cursor.getColumnIndex("title")).thenReturn(1);
        when(cursor.getString(1)).thenReturn("theTitle");
        when(cursor.getColumnIndex("rowid")).thenReturn(2);
        when(cursor.getInt(2)).thenReturn(42);
        when(cursor.getColumnIndex("conversationType")).thenReturn(3);
        when(cursor.getInt(3)).thenReturn(0);
        Conversation conv = Conversation.fromCursor(null, cursor);
        assertEquals(conv.title, "theTitle");
        assertEquals(conv.rowid, 42);
        assertEquals(conv.conversationType, Conversation.ConversationType.Single);
    }

}
