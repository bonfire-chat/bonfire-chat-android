package de.tudarmstadt.informatik.bp.bonfirechat.routing;

/**
 * Created by johannes on 29.08.15.
 */
public class TracerouteNodeSegment implements TracerouteSegment {

    private final String nickname;

    public TracerouteNodeSegment(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

}
