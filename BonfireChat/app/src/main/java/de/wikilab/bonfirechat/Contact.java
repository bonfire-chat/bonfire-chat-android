package de.wikilab.bonfirechat;

/**
 * Created by johannes on 05.05.15.
 */
public class Contact {
    private String name;

    public Contact(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
