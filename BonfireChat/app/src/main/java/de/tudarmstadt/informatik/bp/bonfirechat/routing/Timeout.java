package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import java.util.NoSuchElementException;
import java.util.Queue;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.RingBuffer;

/**
 * Created by jonas on 01.08.15.
 */
public class Timeout extends Thread{

    private Queue<Packet> queue;
    //timeout in milliseconds
    private int timeout;

    public Timeout(Queue<Packet> queue){
        this.queue = queue;
        //randomly chosen value
        this.timeout = 30000;
    }

    public Timeout(Queue<Packet> queue, int timeout){
        this.queue = queue;
        this.timeout = timeout;
    }

    public void run(){
        while (true) {
            try {
                Packet currentPacket = queue.element();
                sleep(System.currentTimeMillis() - (currentPacket.getTimeSent() + timeout));
                if (currentPacket.equals(queue.element())){
                    //TODO: Hier Retransmission durchführen
                    queue.remove();
                }

            } catch (InterruptedException e) {
                continue;
            }
            catch (NoSuchElementException e){
                break;
            }
        }
        //TODO: find better solution
        stop();
    }

}
