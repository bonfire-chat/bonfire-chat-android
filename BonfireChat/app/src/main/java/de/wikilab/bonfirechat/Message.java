package de.wikilab.bonfirechat;

/**
 * Created by johannes on 05.05.15.
 */
public class Message {
    public String body;

    public Message(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return body;
    }
}
