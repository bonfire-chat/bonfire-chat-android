package de.tudarmstadt.informatik.bp.bonfirechat.routing;

/**
 * Created by johannes on 29.08.15.
 */
public class TracerouteNodeSegment implements TracerouteSegment {

    private final String nickname;
    private final String image;

    public TracerouteNodeSegment(String nickname) {
        this.nickname = nickname;
        image = "";
    }

    public TracerouteNodeSegment(String nickname, String image) {
        this.nickname = nickname;
        this.image = image;
    }

    public String getNickname() {
        return nickname;
    }

    public String getImage() {
        return image;
    }

}
