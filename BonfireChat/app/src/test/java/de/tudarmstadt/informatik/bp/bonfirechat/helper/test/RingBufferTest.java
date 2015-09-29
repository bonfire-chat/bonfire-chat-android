package de.tudarmstadt.informatik.bp.bonfirechat.helper.test;

import org.junit.Test;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.RingBuffer;

import static org.junit.Assert.*;

/**
 * Created by mw on 11.07.15.
 */
public class RingBufferTest {

    /**
     * tests, that contains() does not crash when an instance of RingBuffer is empty
     */
    @Test
    public void testEmpty() {
        RingBuffer<String> rb = new RingBuffer<String>(0);
        assertFalse(rb.contains("hello"));

    }

    /**
     * tests correct behaviour of contains() method
     * <br><br>
     * If Object o is in the RingBuffer, contains(o) should return true
     * Otherwise it should return false
     */
    @Test
    public void testContains() {
        RingBuffer<String> rb = new RingBuffer<String>(5);
        assertFalse(rb.contains("hello"));
        rb.enqueue("hello");
        assertTrue(rb.contains("hello"));
        assertFalse(rb.contains("world"));
    }

    /**
     * Tests correct behaviour of RingBuffer after it exceeds its size for the first time
     * <br><br>
     * The first object that overflows the buffer should be written to the first position
     */
    @Test
    public void testOverflow() {
        RingBuffer<String> rb = new RingBuffer<String>(5);
        assertFalse(rb.contains("hello"));
        rb.enqueue("hello");
        rb.enqueue("1");
        assertTrue(rb.contains("hello"));
        rb.enqueue("2");
        rb.enqueue("3");
        rb.enqueue("4");
        assertTrue(rb.contains("hello"));
        rb.enqueue("5");
        assertFalse(rb.contains("hello"));
    }

}
