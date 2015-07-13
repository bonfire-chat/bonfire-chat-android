package de.tudarmstadt.informatik.bp.bonfirechat.helper.test;

import org.junit.Test;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.RingBuffer;

import static org.junit.Assert.*;

/**
 * Created by mw on 11.07.15.
 */
public class RingBufferTest {

    @Test
    public void testEmpty() {
        RingBuffer<String> rb = new RingBuffer<String>(0);
        assertFalse(rb.contains("hello"));

    }

    @Test
    public void testContains() {
        RingBuffer<String> rb = new RingBuffer<String>(5);
        assertFalse(rb.contains("hello"));
        rb.enqueue("hello");
        assertTrue(rb.contains("hello"));
        assertFalse(rb.contains("world"));
    }

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
