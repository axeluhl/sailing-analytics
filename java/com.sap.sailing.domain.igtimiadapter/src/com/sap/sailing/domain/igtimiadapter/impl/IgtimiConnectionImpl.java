package com.sap.sailing.domain.igtimiadapter.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Group;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.LiveDataConnection;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.Session;
import com.sap.sailing.domain.igtimiadapter.User;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.igtimiadapter.shared.IgtimiWindReceiver;
import com.sap.sailing.domain.igtimiadapter.websocket.LiveDataConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.websocket.LiveDataConnectionFactoryImpl;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.DynamicTrackImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class IgtimiConnectionImpl implements IgtimiConnection {
    private static final Logger logger = Logger.getLogger(IgtimiConnectionImpl.class.getName());

    /**
     * name of the JSON property indicating an error reason in a response document
     */
    private static final String REASON = "reason";

    /**
     * name of the JSON property indicating an error condition in a response document
     */
    private static final String ERROR = "error";

    private final Account account;
    private final IgtimiConnectionFactoryImpl connectionFactory;
    private final LiveDataConnectionFactory liveDataConnectionFactory;
    
    public IgtimiConnectionImpl(IgtimiConnectionFactoryImpl connectionFactory, Account account) {
        this.connectionFactory = connectionFactory;
        this.account = account;
        liveDataConnectionFactory = new LiveDataConnectionFactoryImpl(connectionFactory, account);
    }
    
    @Override
    public Account getAccount() {
        return account;
    }
    
    @Override
    public Iterable<User> getUsers() throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        HttpClient client = connectionFactory.getHttpClient();
        HttpGet getUsers = new HttpGet(connectionFactory.getUsersUrl(account));
        JSONObject usersJson = ConnectivityUtils.getJsonFromResponse(client.execute(getUsers));
        final List<User> result = new ArrayList<>();
        for (Object userJson : (JSONArray) usersJson.get("users")) {
            User user = new UserDeserializer().createUserFromJson((JSONObject) ((JSONObject) userJson).get("user"));
            result.add(user);
        }
        return result;
    }

    @Override
    public User getUser(long id) throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        HttpClient client = connectionFactory.getHttpClient();
        HttpGet getUsers = new HttpGet(connectionFactory.getUserUrl(id, account));
        JSONObject userJson = ConnectivityUtils.getJsonFromResponse(client.execute(getUsers));
        User result = new UserDeserializer().createUserFromJson((JSONObject) ((JSONObject) userJson).get("user"));
        return result;
    }
    
    @Override
    public Iterable<Group> getGroups() throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        HttpClient client = connectionFactory.getHttpClient();
        HttpGet getGroups = new HttpGet(connectionFactory.getGroupsUrl(account));
        JSONObject groupsJson = ConnectivityUtils.getJsonFromResponse(client.execute(getGroups));
        final List<Group> result = new ArrayList<>();
        for (Object groupJson : (JSONArray) groupsJson.get("groups")) {
            Group group = new GroupDeserializer().createGroupFromJson((JSONObject) ((JSONObject) groupJson).get("group"));
            result.add(group);
        }
        return result;
    }

    @Override
    public Iterable<Resource> getResources(Permission permission, TimePoint startTime, TimePoint endTime,
            Iterable<String> deviceIds, Iterable<String> streamIds) throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        HttpClient client = connectionFactory.getHttpClient();
        HttpGet getResources = new HttpGet(connectionFactory.getResourcesUrl(permission, startTime, endTime, deviceIds, streamIds, account));
        JSONObject resourcesJson = ConnectivityUtils.getJsonFromResponse(client.execute(getResources));
        final List<Resource> result = new ArrayList<>();
        for (Object resourceJson : (JSONArray) resourcesJson.get("resources")) {
            Resource resource = new ResourceDeserializer().createResourceFromJson((JSONObject) ((JSONObject) resourceJson).get("resource"), this);
            result.add(resource);
        }
        return result;
    }
    
    @Override
    public Iterable<Fix> getLatestFixes(Iterable<String> deviceSerialNumbers, Type type) throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        HttpClient client = connectionFactory.getHttpClient();
        HttpGet getLatestData = new HttpGet(connectionFactory.getLatestDatumUrl(deviceSerialNumbers, type, account));
        JSONObject latestDataJson = ConnectivityUtils.getJsonFromResponse(client.execute(getLatestData));
        return new FixFactory().createFixes(latestDataJson);
    }
    
    @Override
    public Iterable<Session> getSessions(Iterable<Long> sessionIds, Boolean isPublic, Integer limit,
            Boolean includeIncomplete) throws IllegalStateException, ClientProtocolException, IOException,
            ParseException {
        HttpClient client = connectionFactory.getHttpClient();
        HttpGet getResources = new HttpGet(connectionFactory.getSessionsUrl(sessionIds, isPublic, limit, includeIncomplete, account));
        JSONObject sessionsJson = ConnectivityUtils.getJsonFromResponse(client.execute(getResources));
        final List<Session> result = new ArrayList<>();
        for (Object sessionJson : (JSONArray) sessionsJson.get("sessions")) {
            Session session = new SessionDeserializer().createResourceFromJson((JSONObject) ((JSONObject) sessionJson).get("session"), this);
            result.add(session);
        }
        return result;
    }

    @Override
    public Session getSession(long id) throws IllegalStateException, ClientProtocolException, IOException,
            ParseException {
        HttpClient client = connectionFactory.getHttpClient();
        HttpGet getSessions = new HttpGet(connectionFactory.getSessionUrl(id, account));
        JSONObject sessionJson = ConnectivityUtils.getJsonFromResponse(client.execute(getSessions));
        Session result = new SessionDeserializer().createResourceFromJson((JSONObject) ((JSONObject) sessionJson).get("session"), this);
        return result;
    }

    @Override
    public Iterable<Fix> getResourceData(final TimePoint startTime, final TimePoint endTime,
            Iterable<String> deviceSerialNumbers, Map<Type, Double> typeAndCompression) throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        logger.info("Requested resource data from "+startTime+" to "+endTime+" for devices "+deviceSerialNumbers+" for types "+typeAndCompression);
        List<Fix> result = new ArrayList<>(); 
        // Cut interval into slices that are at most one week long. See also the discussion at
        // http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=2002 that talks about a one-month limitation
        // imposed by the Igtimi API
        TimePoint windowStartTime = startTime;
        while (!windowStartTime.after(endTime)) {
            TimePoint windowEndTime = windowStartTime.plus(Duration.ONE_WEEK);
            if (windowEndTime.after(endTime)) {
                windowEndTime = endTime;
            }
            logger.info("Obtaining resource data from "+windowStartTime+" to "+windowEndTime+" for devices "+deviceSerialNumbers+" for types "+typeAndCompression);
            HttpClient client = connectionFactory.getHttpClient();
            HttpGet getResourceData = new HttpGet(connectionFactory.getResourceDataUrl(windowStartTime, windowEndTime,
                    deviceSerialNumbers, typeAndCompression, account));
            JSONObject resourceDataJson = ConnectivityUtils.getJsonFromResponse(client.execute(getResourceData));
            String error = (String) resourceDataJson.get(ERROR);
            if (error != null) {
                String reason = (String) resourceDataJson.get(REASON);
                throw new ClientProtocolException("Error trying to obtain Igtimi resource data from " + windowStartTime
                        + " to " + windowEndTime + " from devices " + deviceSerialNumbers + ": " + error
                        + (reason == null ? "" : ". Reason: " + reason));
            }
            Util.addAll(new FixFactory().createFixes(resourceDataJson), result);
            windowStartTime = windowEndTime.plus(1);
        }
        return result;
    }

    @Override
    public Iterable<Fix> getResourceData(TimePoint startTime, TimePoint endTime, Iterable<String> deviceSerialNumbers,
            Type... types) throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        Map<Type, Double> typeAndCompression = new HashMap<>();
        for (Type type : types) {
            typeAndCompression.put(type, 0.0);
        }
        return getResourceData(startTime, endTime, deviceSerialNumbers, typeAndCompression);
    }

    @Override
    public Iterable<Fix> getAndNotifyResourceData(TimePoint startTime, TimePoint endTime,
            Iterable<String> deviceSerialNumbers, BulkFixReceiver bulkFixReceiver, Type... types)
            throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        Iterable<Fix> result = getResourceData(startTime, endTime, deviceSerialNumbers, types);
        logger.info("Received "+Util.size(result)+" fixes; will pass them to BulkFixReceiver for further processing now.");
        bulkFixReceiver.received(result);
        return result;
    }

    @Override
    public Map<String, Map<Type, DynamicTrack<Fix>>> getResourceDataAsTracks(TimePoint startTime, TimePoint endTime, Iterable<String> deviceSerialNumbers,
            Type... types) throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        Iterable<Fix> fixes = getResourceData(startTime, endTime, deviceSerialNumbers, types);
        Map<String, Map<Type, DynamicTrack<Fix>>> result = new HashMap<>();
        for (Fix fix : fixes) {
            String deviceSerialNumber = fix.getSensor().getDeviceSerialNumber();
            Type type = fix.getType();
            DynamicTrack<Fix> track = (DynamicTrack<Fix>) getOrCreateTrack(result, deviceSerialNumber, type);
            track.add(fix);
        }
        return result;
    }
    

    @Override
    public LiveDataConnection getOrCreateLiveConnection(Iterable<String> deviceSerialNumbers) throws Exception {
        return liveDataConnectionFactory.getOrCreateLiveDataConnection(deviceSerialNumbers);
    }

    private DynamicTrack<Fix> getOrCreateTrack(Map<String, Map<Type, DynamicTrack<Fix>>> result,
            String deviceSerialNumber, Type type) {
        Map<Type, DynamicTrack<Fix>> mapForDevice = result.get(deviceSerialNumber);
        if (mapForDevice == null) {
            mapForDevice = new HashMap<>();
            result.put(deviceSerialNumber, mapForDevice);
        }
        DynamicTrack<Fix> track = mapForDevice.get(type);
        if (track == null) {
            track = new DynamicTrackImpl<Fix>("Track for Igtimi fixes of type "+type.name()+" for "+this);
            mapForDevice.put(type, track);
        }
        return track;
    }

    @Override
    public Iterable<Device> getOwnedDevices() throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        HttpClient client = connectionFactory.getHttpClient();
        HttpGet getResources = new HttpGet(connectionFactory.getOwnedDevicesUrl(account));
        JSONObject devicesJson = ConnectivityUtils.getJsonFromResponse(client.execute(getResources));
        final List<Device> result = new ArrayList<>();
        for (Object deviceJson : (JSONArray) devicesJson.get("devices")) {
            Device device = new DeviceDeserializer().createResourceFromJson((JSONObject) ((JSONObject) deviceJson).get("device"), this);
            result.add(device);
        }
        return result;
    }

    @Override
    public Iterable<DataAccessWindow> getDataAccessWindows(Permission permission, TimePoint startTime,
            TimePoint endTime, Iterable<String> deviceSerialNumbers) throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        HttpClient client = connectionFactory.getHttpClient();
        HttpGet getDataAccessWindows = new HttpGet(connectionFactory.getDataAccessWindowsUrl(permission, startTime, endTime, deviceSerialNumbers, account));
        JSONObject dataAccessWindowsJson = ConnectivityUtils.getJsonFromResponse(client.execute(getDataAccessWindows));
        final List<DataAccessWindow> result = new ArrayList<>();
        for (Object dataAccessWindowJson : (JSONArray) dataAccessWindowsJson.get("data_access_windows")) {
            DataAccessWindow dataAccessWindow = new DataAccessWindowDeserializer().createDataAccessWindowFromJson(
                    (JSONObject) ((JSONObject) dataAccessWindowJson).get("data_access_window"), this);
            result.add(dataAccessWindow);
        }
        return result;
    }

    @Override
    public Map<TrackedRace, Integer> importWindIntoRace(Iterable<DynamicTrackedRace> trackedRaces,
            boolean correctByDeclination) throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        logger.info("Importing Igtimi wind for tracked races " + trackedRaces);
        TimePoint startOfWindow = new MillisecondsTimePoint(Long.MAX_VALUE);
        TimePoint endOfWindow = new MillisecondsTimePoint(0);
        for (DynamicTrackedRace trackedRace : trackedRaces) {
            startOfWindow = Collections.min(Arrays.asList(startOfWindow, IgtimiWindTracker.getReceivingStartTime(trackedRace)));
            final TimePoint receivingEndTime = IgtimiWindTracker.getReceivingEndTime(trackedRace);
            endOfWindow = Collections.max(Arrays.asList(endOfWindow, receivingEndTime==null?MillisecondsTimePoint.now():receivingEndTime));
        }
        Iterable<DataAccessWindow> daws = getDataAccessWindows(Permission.read, startOfWindow, endOfWindow, /* find all deviceSerialNumbers for window */ null);
        logger.info("Found "+Util.size(daws)+" data access windows. Analyzing which ones contain wind data...");
        Set<String> deviceSerialNumbers = new HashSet<>();
        for (DataAccessWindow daw : daws) {
            String deviceSerialNumber = daw.getDeviceSerialNumber();
            // now filter for the wind-providing devices
            Iterable<Resource> resources = getResources(Permission.read, startOfWindow, endOfWindow,
                    Collections.singleton(deviceSerialNumber), /* streamIds */ null);
            if (hasWind(resources)) {
                StringBuilder resourceIDs = new StringBuilder();
                boolean first = true;
                for (Resource resource : resources) {
                    if (!first) {
                        resourceIDs.append(", ");
                    } else {
                        first = false;
                    }
                    resourceIDs.append(resource.getId());
                }
                logger.info("  Resources ["+resourceIDs+"] for device "+deviceSerialNumber+" contain wind data");
                deviceSerialNumbers.add(deviceSerialNumber);
            }
        }
        final Map<TrackedRace, Integer> result;
        if (!deviceSerialNumbers.isEmpty()) {
            IgtimiWindReceiver windReceiver = new IgtimiWindReceiver(correctByDeclination ? DeclinationService.INSTANCE : null);
            final WindListenerSendingToTrackedRace windListener = new WindListenerSendingToTrackedRace(
                    trackedRaces, Activator.getInstance().getWindTrackerFactory());
            windReceiver.addListener(windListener);
            getAndNotifyResourceData(startOfWindow, endOfWindow, deviceSerialNumbers, windReceiver,
                    windReceiver.getFixTypes());
            result = windListener.getFixesAppliedPerTrackedRace();
            logger.info("Imported the following number of wind fixes for the following list of races: "+result);
        } else {
            logger.info("No Igtimi devices that measure wind found for time window "+startOfWindow+".."+endOfWindow);
            result = new HashMap<>();
        }
        return result;
    }

    private boolean hasWind(Iterable<Resource> resources) {
        for (Resource resource : resources) {
            if (Util.contains(resource.getDataTypes(), Type.AWS)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterable<String> getWindDevices() throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        // find all the devices from which we may read
        Iterable<DataAccessWindow> dataAccessWindows = getDataAccessWindows(
                Permission.read, /* start time */ null, /* end time */ null,
                /* get data for all available deviceSerialNumbers */null);
        Set<String> deviceSerialNumbersWeCanRead = new HashSet<>();
        for (DataAccessWindow daw : dataAccessWindows) {
            deviceSerialNumbersWeCanRead.add(daw.getDeviceSerialNumber());
        }
        // find all that haven't even sent GPS; those may never have sent ever, so we need to listen to them for new stuff; they could be wind sensors
        Iterable<Fix> gpsFixes = getLatestFixes(deviceSerialNumbersWeCanRead, Type.gps_latlong);
        Set<String> devicesWithGps = getDeviceSerialNumbers(gpsFixes);
        Iterable<Fix> awsFixes = getLatestFixes(deviceSerialNumbersWeCanRead, Type.AWS); // look for latest fixes with apparent wind speed in the fix
        Set<String> devicesWithWind = getDeviceSerialNumbers(awsFixes);
        Set<String> devicesThatHaveNeverSentGpsNorWind = new HashSet<>(deviceSerialNumbersWeCanRead);
        devicesThatHaveNeverSentGpsNorWind.removeAll(devicesWithGps);
        devicesThatHaveNeverSentGpsNorWind.removeAll(devicesWithWind);
        Set<String> devicesWeShouldListenTo = new HashSet<>();
        devicesWeShouldListenTo.addAll(devicesWithWind);
        devicesWeShouldListenTo.addAll(devicesThatHaveNeverSentGpsNorWind);
        logger.info("Wind devices identified: "
                + devicesWeShouldListenTo
                + " because from all devices "+deviceSerialNumbersWeCanRead+" for "
                + devicesThatHaveNeverSentGpsNorWind
                + " we don't know what they are as they never sent anything we can access, and for "
                + devicesWithWind + " we know they sent wind");
        return devicesWeShouldListenTo;
    }
    
    private Set<String> getDeviceSerialNumbers(Iterable<Fix> fixes) {
        Set<String> deviceSerialNumbers = new HashSet<>();
        for (Fix fix : fixes) {
            deviceSerialNumbers.add(fix.getSensor().getDeviceSerialNumber());
        }
        return deviceSerialNumbers;
    }

}