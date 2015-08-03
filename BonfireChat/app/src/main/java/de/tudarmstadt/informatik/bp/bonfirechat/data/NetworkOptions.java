package de.tudarmstadt.informatik.bp.bonfirechat.data;

/**
 * Created by mw on 03.08.15.
 */
public class NetworkOptions {

    public static final int MAX_RETRANSMISSIONS = 5;

    // maximum hops for a message until it will be discarded
    public static final int MAX_HOPS = 10;

    public static final int RETRANSMISSION_TIMEOUT = 20000;


}
