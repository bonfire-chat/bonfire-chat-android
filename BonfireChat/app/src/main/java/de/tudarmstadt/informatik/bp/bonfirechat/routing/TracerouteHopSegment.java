package de.tudarmstadt.informatik.bp.bonfirechat.routing;

import java.util.Date;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;

/**
 * Created by johannes on 16.08.15.
 */
public class TracerouteHopSegment implements TracerouteSegment {
    public enum ProtocolType {
        BLUETOOTH,
        WIFI,
        GCM
    }

    private ProtocolType protocol;
    private Date sentTime;
    private Date receivedTime;

    public TracerouteHopSegment(ProtocolType protocol, Date sentTime, Date receivedTime) {
        this.protocol = protocol;
        this.sentTime = sentTime;
        this.receivedTime = receivedTime;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }
    public String getTimeDelta() {
        return DateHelper.formatTimeDelta(sentTime, receivedTime);
    }
}
