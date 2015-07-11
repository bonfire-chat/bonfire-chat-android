package de.tudarmstadt.informatik.bp.bonfirechat.models.test;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.StreamHelper;

/**
 * Created by mw on 12.07.15.
 */
public class StreamHelperTest {

    @Test
    public void testStreamToString() {
        assertEquals(StreamHelper.convertStreamToString(null), "");
        ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[]{});
        assertEquals(StreamHelper.convertStreamToString(emptyStream), "");
        ByteArrayInputStream testStream = new ByteArrayInputStream(new byte[]{0x54,0x65,0x73,0x74});
        assertEquals(StreamHelper.convertStreamToString(testStream), "Test");
    }

    @Test
    public void testByteArrayToHexString() {
        assertEquals("", StreamHelper.byteArrayToHexString(new byte[]{}));
        assertEquals("0001feff", StreamHelper.byteArrayToHexString(new byte[]{0,1,(byte)0xfe,(byte)0xff}));
    }
}
