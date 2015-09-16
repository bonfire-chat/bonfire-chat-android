package de.tudarmstadt.informatik.bp.bonfirechat.helper;

/**
 * Created by mw on 15.06.15.
 */
public class RingBuffer<T extends Object> {

    private final Object[] content;
    private final int length;
    private int insertPosition;

    public RingBuffer(int length) {
        this.length = length;
        content = new Object[length];
        insertPosition = 0;
    }
    public void enqueue(T element) {
        content[insertPosition++] = element;
        insertPosition = insertPosition % length;
    }
    public boolean contains(T element) {
        for(int i = 0; i < length; i++) {
            if (content[i] != null && content[i].equals(element)) return true;
        }
        return false;
    }

}
