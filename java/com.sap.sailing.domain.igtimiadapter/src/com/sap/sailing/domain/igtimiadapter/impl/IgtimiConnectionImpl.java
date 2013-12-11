package com.sap.sailing.domain.igtimiadapter.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util;
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
import com.sap.sailing.domain.igtimiadapter.websocket.WebSocketConnectionManager;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.DynamicTrackImpl;

public class IgtimiConnectionImpl implements IgtimiConnection {
    private static final Logger logger = Logger.getLogger(IgtimiConnectionImpl.class.getName());
    private final Account account;
    private final IgtimiConnectionFactoryImpl connectionFactory;
    
    public IgtimiConnectionImpl(IgtimiConnectionFactoryImpl connectionFactory, Account account) {
        this.connectionFactory = connectionFactory;
        this.account = account;
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
    public Iterable<Fix> getResourceData(TimePoint startTime, TimePoint endTime,
            Iterable<String> deviceSerialNumbers, Map<Type, Double> typeAndCompression) throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        HttpClient client = connectionFactory.getHttpClient();
        HttpGet getResourceData = new HttpGet(connectionFactory.getResourceDataUrl(startTime, endTime, deviceSerialNumbers, typeAndCompression, account));
        JSONObject resourceDataJson = ConnectivityUtils.getJsonFromResponse(client.execute(getResourceData));
        return new FixFactory().createFixes(resourceDataJson);
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
    public LiveDataConnection createLiveConnection(Iterable<String> deviceSerialNumbers) throws Exception {
        return new WebSocketConnectionManager(connectionFactory, deviceSerialNumbers, getAccount());
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
    public void importWindIntoRace(Iterable<DynamicTrackedRace> trackedRaces) throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        TimePoint startOfWindow = new MillisecondsTimePoint(Long.MAX_VALUE);
        TimePoint endOfWindow = new MillisecondsTimePoint(0);
        for (DynamicTrackedRace trackedRace : trackedRaces) {
            startOfWindow = Collections.min(Arrays.asList(startOfWindow, IgtimiWindTracker.getReceivingStartTime(trackedRace)));
            endOfWindow = Collections.max(Arrays.asList(endOfWindow, IgtimiWindTracker.getReceivingEndTime(trackedRace)));
        }
        Iterable<DataAccessWindow> daws = getDataAccessWindows(Permission.read, startOfWindow, endOfWindow, /* find all deviceSerialNumbers for window */ null);
        List<String> deviceSerialNumbers = new ArrayList<>();
        for (DataAccessWindow daw : daws) {
            String deviceSerialNumber = daw.getDeviceSerialNumber();
            // now filter for the wind-providing devices
            Iterable<Resource> resources = getResources(Permission.read, startOfWindow, endOfWindow,
                    Collections.singleton(deviceSerialNumber), /* streamIds */ null);
            if (hasWind(resources)) {
                deviceSerialNumbers.add(deviceSerialNumber);
            }
        }
        if (!deviceSerialNumbers.isEmpty()) {
            IgtimiWindReceiver windReceiver = new IgtimiWindReceiver(deviceSerialNumbers);
            windReceiver.addListener(new WindListenerSendingToTrackedRace(trackedRaces, Activator.getInstance()
                    .getWindTrackerFactory()));
            getAndNotifyResourceData(startOfWindow, endOfWindow, deviceSerialNumbers, windReceiver,
                    windReceiver.getFixTypes());
        } else {
            logger.info("No Igtimi devices found for time window "+startOfWindow+".."+endOfWindow);
        }
    }

    private boolean hasWind(Iterable<Resource> resources) {
        for (Resource resource : resources) {
            if (Util.contains(resource.getDataTypes(), Type.AWS)) {
                return true;
            }
        }
        return false;
    }
}