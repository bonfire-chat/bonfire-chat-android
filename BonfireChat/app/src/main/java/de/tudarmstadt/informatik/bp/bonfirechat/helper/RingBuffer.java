package de.tudarmstadt.informatik.bp.bonfirechat.helper;

/**
 * Created by mw on 15.06.15.
 */
public class RingBuffer<T extends Object> {

    Object[] content;
    int length;
    int insertPosition;

    public RingBuffer(int length) {
        content = new Object[length];
        insertPosition = 0;
    }
    public void enqueue(T element) {
        content[insertPosition++] = element;
        insertPosition = insertPosition % length;
    }
    public boolean contains(T element) {
        for(int i = 0; i < length; i++) {
            if (element.equals(content[i])) return true;
        }
        return false;
    }

}
