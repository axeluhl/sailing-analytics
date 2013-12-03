package com.sap.sailing.domain.igtimiadapter;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

/**
 * Sessions are a container that gives meaning and structure to a group of {@link Device devices} that are brought
 * together at a particular time for a race etc. It has a few direct properties such as {@link #getName() name},
 * start+end time. Then it has the session logs which hold all the detail about what’s in the session, such as yachts,
 * buoys, devices, names and colors. Sessions are shared by adding a {@link User user} into the session's {@link Group}
 * identified by the {@link #getSessionGroupId() session group id}.
 * <p>
 * 
 * Sessions handle all the magic to ensure that {@link #getPermissions() permissions} to access device data (
 * {@link DataAccessWindow)s} are handled when group membership changes, and as devices are added or removed to the
 * session via the logs. So once you are familiar with using them it takes away all the load of managing users,
 * permissions, and access to data. Without sessions, to give you access to an event's data would have meant manually
 * making a list of devices used in the session and creating a bunch of {@link DataAccessWindow}s for them.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Session extends HasId, HasPermissions, HasStartAndEndTime {
    boolean isBlob();

    long getAdminSessionGroupId();

    long getSessionGroupId();

    long getOwnerId();

    String getName();

    User getOwner() throws IllegalStateException, ClientProtocolException, IOException, ParseException;
}
