package com.sap.sailing.domain.igtimiadapter;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;

/**
 * A connection to the Igtimi system for one {@link Client} and one {@link Account}.
 * 
 * @author Axel Uhl (d043530)
 */
public interface IgtimiConnection {

    Iterable<User> getUsers() throws IllegalStateException, ClientProtocolException, IOException, ParseException;

    User getUser(long id) throws IllegalStateException, ClientProtocolException, IOException, ParseException;

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
     * 
     * @param deviceSerialNumbers
     *            the serial numbers of the devices for which to return data; these numbers can be obtained, e.g., from
     *            {@link #getOwnedDevices()}.{@link Device#getSerialNumber() getSerialNumber()} or from
     *            {@link #getDataAccessWindows(Permission, TimePoint, TimePoint, Iterable)}.
     *            {@link DataAccessWindow#getDeviceSerialNumber() getDeviceSerialNumber()}.
     * @param typeAndCompression
     *            for each data type to be obtained, tells the compression level; <code>0.0</code> is a good default,
     *            meaning "no compression". Compression is currently only supported for type {@link Type#gps_latlong} where
     *            the number provided represents a maximum error in degrees of latitude and longitude.
     */
    Iterable<Fix> getResourceData(TimePoint startTime, TimePoint endTime, Iterable<String> deviceSerialNumbers,
            Map<Type, Double> typeAndCompression) throws IllegalStateException, ClientProtocolException, IOException,
            ParseException;
    
    /**
     * Shorthand for {@link #getResourceData(TimePoint, TimePoint, Iterable, Map)} where no compression is requested for
     * any type.
     */
    Iterable<Fix> getResourceData(TimePoint startTime, TimePoint endTime, Iterable<String> deviceSerialNumbers,
            Type... types) throws IllegalStateException, ClientProtocolException, IOException,
            ParseException;

    /**
     * Shorthand for {@link #getResourceData(TimePoint, TimePoint, Iterable, Map)} where no compression is requested for
     * any type. The fixes received are forwarded to the {@link BulkFixReceiver} <code>bulkFixReceiver</code> in one call.
     */
    Iterable<Fix> getAndNotifyResourceData(TimePoint startTime, TimePoint endTime,
            Iterable<String> deviceSerialNumbers, BulkFixReceiver bulkFixReceiver, Type... types)
            throws IllegalStateException, ClientProtocolException, IOException, ParseException;


    /**
     * Same as {@link #getResourceData(TimePoint, TimePoint, Iterable, Type...)}, but the resulting {@link Fix}es are
     * grouped into {@link Track}s per fix type and per device.
     * 
     * @return a map whose keys are the devices' serial numbers and whose values are the fixes produced by the device
     *         identified by the key, grouped in a map with the fix {@link Type} as its key. Note that if a device
     *         didn't produce any fixes at all under the requested parameters, its serial number may not appear as a key
     *         in the map. Note further that should a device not have produced fixes of a given type, that type won't
     *         appear as a key in the map for that device.
     */
    Map<String, Map<Type, DynamicTrack<Fix>>> getResourceDataAsTracks(TimePoint startTime, TimePoint endTime,
            Iterable<String> deviceSerialNumbers, Type... types) throws IllegalStateException, ClientProtocolException,
            IOException, ParseException;

    /**
     * For the devices specified by <code>deviceSerialNumbers</code>, creates a live data connection. The <code>account</code>
     * needs to be authorized to access the devices' data for the current time window. Fixes received through this connection
     * are forwarded in the batches in which they are received to the listeners that can be added to the live connection
     * using {@link LiveDataConnection#addListener(BulkFixReceiver)}.
     * 
     * @return a connection that the caller can use to stop the live feed by calling {@link LiveDataConnection#stop()}.
     */
    LiveDataConnection getOrCreateLiveConnection(Iterable<String> deviceSerialNumbers) throws Exception;
    
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

    Session getSession(long id) throws IllegalStateException, ClientProtocolException, IOException, ParseException;
    
    /**
     * Returns the devices owned by the user to which the application client represented by this connection belongs.
     */
    Iterable<Device> getOwnedDevices() throws IllegalStateException, ClientProtocolException, IOException, ParseException;
    
    /**
     * Returns all devices that this connection has access to with the requested <code>permission</code>. Note that
     * these don't necessarily need to be devices owned by the user to which this connection belongs. The user only
     * needs to have been authorized by the owner of the data to access the respective window of data.
     * 
     * @param startTime
     *            optional; may be <code>null</code>. If provided, only data access windows whose time frame has a
     *            non-empty range after this time will be returned.
     * @param endTime
     *            optional; may be <code>null</code>. If provided, only data access windows whose time frame has a
     *            non-empty range before this time will be returned.
     * @param deviceSerialNumbers
     *            optional; if not <code>null</code> and not empty, only data access windows for the devices identified
     *            by these serial numbers will be returned
     */
    Iterable<DataAccessWindow> getDataAccessWindows(Permission permission, TimePoint startTime, TimePoint endTime,
            Iterable<String> deviceSerialNumbers) throws IllegalStateException, ClientProtocolException, IOException,
            ParseException;

    Iterable<Group> getGroups() throws IllegalStateException, ClientProtocolException, IOException, ParseException;

    Account getAccount();

    /**
     * Finds all data access windows that have wind data for the time span around the race, loads their wind data and
     * {@link DynamicTrackedRace#recordWind(com.sap.sailing.domain.tracking.Wind, com.sap.sailing.domain.common.WindSource)
     * records it} in the tracked races.
     * 
     * @return the number of wind fixes imported per tracked race; contains an entry for all elements in
     *         <code>trackedRaces</code>
     */
    Map<TrackedRace, Integer> importWindIntoRace(Iterable<DynamicTrackedRace> trackedRaces, boolean correctByDeclination) throws IllegalStateException,
            ClientProtocolException, IOException, ParseException;
    
    /**
     * Find all the devices from which we may read and which have logged GPS positions and apparent wind speed (AWS) or that
     * have never logged GPS nor wind (probably new sensors)
     */
    Iterable<String> getWindDevices() throws IllegalStateException, IOException, ParseException;

    /**
     * Returns the latest datum for the specified devices that contains a fix of the <code>type</code> requested. The
     * result contains entries only for those devices that have actually produced a fix of the <code>type</code> requested
     * that is readable by the {@link #getAccount()} used by this connection.
     */
    Iterable<Fix> getLatestFixes(Iterable<String> deviceSerialNumbers, Type type) throws IllegalStateException, ClientProtocolException, IOException, ParseException;
}
