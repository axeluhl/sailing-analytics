package com.sap.sailing.domain.anniversary;

import java.io.Serializable;
import java.net.URL;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sse.common.TimePoint;

/**
 * Used to capture information about a race for use in an "anniversary" feature that allows us to sort a set of races by
 * their start time. Races are identified by a {@link RegattaAndRaceIdentifier}. Their start time is recorded in the
 * {@link #startOfRace} field which must not be {@code null}, and the {@link URL} of the remote server reference is
 * stored in {@link #remoteUrl}. Using clients shall make sure that the same {@link URL} object is used for larger sets
 * of objects of this type in order not to waste memory on a massive replication of those equal {@link URL} objects
 * identifying the same remote server.
 */
public class SimpleRaceInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final RegattaAndRaceIdentifier identifier;
    private final TimePoint startOfRace;
    private final URL remoteUrl;

    /**
     * @param remoteUrl
     *            use {@code null} to mean "local"; a local server does not necessarily know under which URL it is being
     *            reached and therefore cannot provide this
     */
    public SimpleRaceInfo(RegattaAndRaceIdentifier identifier, TimePoint startOfRace, URL remoteUrl) {
        if (identifier == null || startOfRace == null) {
            throw new IllegalStateException("SimpleRaceInfo Data is not allowed to contain any null values!");
        }
        this.identifier = identifier;
        this.startOfRace = startOfRace;
        this.remoteUrl = remoteUrl;
    }

    public RegattaAndRaceIdentifier getIdentifier() {
        return identifier;
    }

    /**
     * The URL used in a remote server reference through which the record was obtained; or {@code null} in case the race
     * identified by this object resides on the local server responding to a request.
     */
    public URL getRemoteUrl() {
        return remoteUrl;
    }

    public TimePoint getStartOfRace() {
        return startOfRace;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((startOfRace == null) ? 0 : startOfRace.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleRaceInfo other = (SimpleRaceInfo) obj;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        if (startOfRace == null) {
            if (other.startOfRace != null)
                return false;
        } else if (!startOfRace.equals(other.startOfRace))
            return false;
        return true;
    }

}