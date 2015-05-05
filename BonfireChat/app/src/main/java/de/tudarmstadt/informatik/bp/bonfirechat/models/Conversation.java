package de.tudarmstadt.informatik.bp.bonfirechat.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johannes on 05.05.15.
 */
public class Conversation {
    private Contact peer;
    private ArrayList<Message> messages;

    public Conversation(Contact peer, List<Message> messages) {
        this.peer = peer;
        this.messages = new ArrayList<>(messages);
    }

    public String getLastMessage() {
        return messages.get(messages.size() - 1).toString();
    }
    public String getName() {
        return peer.toString();
    }
}
