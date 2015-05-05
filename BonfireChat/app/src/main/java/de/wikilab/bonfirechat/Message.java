package de.wikilab.bonfirechat;

/**
 * Created by johannes on 05.05.15.
 */
public class Message {
    enum MessageDirection {
        Unknown,
        Sent,
        Received
    }

    public String body;
    public MessageDirection direction = MessageDirection.Unknown;

    public Message(String body, MessageDirection dir) {
        this.body = body; this.direction = dir;
    }

    @Override
    public String toString() {
        return body;
    }
}
