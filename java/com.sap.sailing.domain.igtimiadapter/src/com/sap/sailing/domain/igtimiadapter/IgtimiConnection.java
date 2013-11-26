package com.sap.sailing.domain.igtimiadapter;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.TimePoint;

/**
 * A connection to the Igtimi system for one {@link Client} and one {@link Account}.
 * 
 * @author Axel Uhl (d043530)
 */
public interface IgtimiConnection {

    Iterable<User> getUsers() throws IllegalStateException, ClientProtocolException, IOException, ParseException;

    /**
     * @param startTime optional; may be <code>null</code>
     * @param endTime optional; may be <code>null</code>
     * @param serialNumbers optional; may be <code>null</code>
     * @param streamIds optional; may be <code>null</code>
     */
    Iterable<Resource> getResources(Permission permission, TimePoint startTime, TimePoint endTime,
            Iterable<String> serialNumbers, Iterable<String> streamIds) throws IllegalStateException,
            ClientProtocolException, IOException, ParseException;

}
