package com.sap.sailing.domain.igtimiadapter;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;

/**
 * A connection to the Igtimi system for one {@link Client} and one {@link Account}.
 * 
 * @author Axel Uhl (d043530)
 */
public interface IgtimiConnection {

    Iterable<User> getUsers() throws IllegalStateException, ClientProtocolException, IOException, ParseException;

    /**
     * @param startTime
     *            optional; may be <code>null</code>
     * @param endTime
     *            optional; may be <code>null</code>
     * @param deviceIds
     *            optional (may be <code>null</code>) if <code>permission</code> is {@link Permission#modify}; lists the
     *            devices for which to look for resources
     * @param streamIds
     *            optional; may be <code>null</code>
     */
    Iterable<Resource> getResources(Permission permission, TimePoint startTime, TimePoint endTime,
            Iterable<String> deviceIds, Iterable<String> streamIds) throws IllegalStateException,
            ClientProtocolException, IOException, ParseException;

    /**
     * All arguments are mandatory.
     */
    Iterable<Fix> getResourceData(TimePoint startTime, TimePoint endTime, Iterable<String> serialNumbers,
            Map<Type, Double> typeAndCompression) throws IllegalStateException, ClientProtocolException, IOException,
            ParseException;

    /**
     * @param sessionIds
     *            the optional IDs of the sessions for which to obtain the metadata; if <code>null</code>, all available
     *            sessions will be returned
     * @param isPublic
     *            optional; if <code>null</code> or <code>true</code>, only public sessions will be included in the
     *            result
     * @param limit
     *            optional; if not <code>null</code>, no more than this many session objects will be returned
     * @param includeIncomplete
     *            optional; if not <code>false</code>, only completed sessions that have a start and an end time will be
     *            returned
     */
    Iterable<Session> getSessions(Iterable<Long> sessionIds, Boolean isPublic, Integer limit, Boolean includeIncomplete)
            throws IllegalStateException, ClientProtocolException, IOException, ParseException;

}
