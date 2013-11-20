package com.sap.sailing.domain.igtimiadapter;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

/**
 * A connection to the Igtimi system for one {@link Client} and one {@link Account}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface IgtimiConnection {

    Iterable<User> getUsers() throws IllegalStateException, ClientProtocolException, IOException, ParseException;

}
