package de.tudarmstadt.informatik.bp.bonfirechat.models;

/**
 * Created by mw on 15.06.15.
 */
public interface IPublicIdentity {
    MyPublicKey getPublicKey();
    String getNickname();
    String getPhoneNumber();
    String getImage();
}
