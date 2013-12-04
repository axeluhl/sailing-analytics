package com.sap.sailing.domain.igtimiadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Group;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.Session;
import com.sap.sailing.domain.igtimiadapter.User;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWA;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWS;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.igtimiadapter.impl.Activator;

public class BasicIgtimiAdapterTest {
    @Test
    public void testGetUsers() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        IgtimiConnection connection = connectionFactory.connect(account);
        Iterable<User> users = connection.getUsers();
        assertEquals(1, Util.size(users));
        assertEquals(account.getUser().getId(), users.iterator().next().getId());
    }

    @Test
    public void testGetGroups() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        IgtimiConnection connection = connectionFactory.connect(account);
        Iterable<Group> groups = connection.getGroups();
        assertTrue(Util.size(groups) > 1);
    }

    @Test
    public void testGetDataAccessWindows() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        IgtimiConnection connection = connectionFactory.connect(account);
        Iterable<DataAccessWindow> daws = connection.getDataAccessWindows(Permission.read, /* startTime */ null,
                /* endTime */ null, /* deviceSerialNumbers */ Collections.singleton("DD-EE-AAGA"));
        assertFalse(Util.isEmpty(daws));
        for (DataAccessWindow daw : daws) {
            assertEquals("DD-EE-AAGA", daw.getDeviceSerialNumber());
            assertTrue(daw.getRecipient() instanceof Group);
        }
    }
    
    @Test
    public void testGetAllDataAccessWindows() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        IgtimiConnection connection = connectionFactory.connect(account);
        Iterable<DataAccessWindow> daws = connection.getDataAccessWindows(Permission.read, /* startTime */ null,
                /* endTime */ null, /* deviceSerialNumbers */ null);
        assertFalse(Util.isEmpty(daws));
    }
    
    @Test
    public void testGetResources() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        IgtimiConnection connection = connectionFactory.connect(account);
        Iterable<Resource> resources = connection.getResources(Permission.read, /* start time */ null, /* end time */ null,
                /* serial numbers */ Collections.singleton("GA-EN-AAEJ"), /* stream IDs */ null);
        assertTrue(resources.iterator().hasNext());
    }
    
    @Test
    public void testGetSessions() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        IgtimiConnection connection = connectionFactory.connect(account);
        Iterable<Session> sessions = connection.getSessions(Collections.singleton(4571l), /* isPublic */ null, /* limit */ 1, /* includeIncomplete */ null);
        assertTrue(sessions.iterator().hasNext());
        final Session session = sessions.iterator().next();
        assertEquals("Auto Session - 01:14:02 10 Nov", session.getName());
        User sessionOwher = connection.getUser(session.getOwnerId());
        assertEquals("Uhl", sessionOwher.getSurname());
    }

    @Test
    public void testGetResourceData() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        IgtimiConnection connection = connectionFactory.connect(account);
        Map<Type, Double> typesAndCompression = new HashMap<>();
        typesAndCompression.put(Type.gps_latlong, 0.0);
        Iterable<Fix> data = connection.getResourceData(new MillisecondsTimePoint(1384420883000l),
                new MillisecondsTimePoint(1384421639000l), Collections.singleton("GA-EN-AAEJ"), typesAndCompression);
        assertTrue(data.iterator().hasNext());
    }
    
    @Test
    public void testDataAccessWindowForGivenTimeFrame() throws java.text.ParseException, IllegalStateException, ClientProtocolException, IOException, ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.GERMAN);
        TimePoint start = new MillisecondsTimePoint(dateFormat.parse("2013-11-07T08:00:00Z"));
        TimePoint end   = new MillisecondsTimePoint(dateFormat.parse("2013-11-09T18:00:00Z"));
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        IgtimiConnection connection = connectionFactory.connect(account);
        // URL is https://www.igtimi.com/api/v1/devices/data_access_windows?type=read&start_time=1383811200000&end_time=1383933600000&access_token=3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9
        Iterable<DataAccessWindow> daws = connection.getDataAccessWindows(Permission.read, start, end, /* deviceSerialNumbers; get all devices available for that time */ null);
        assertFalse(Util.isEmpty(daws));
        for (DataAccessWindow daw : daws) {
            assertTrue((daw.getStartTime().compareTo(start)<=0 && daw.getEndTime().compareTo(start)>0)  // spans start
                    || (daw.getStartTime().compareTo(end)<=0 && daw.getEndTime().compareTo(end)>0)      // spans end
                    || (daw.getStartTime().compareTo(start)>=0 && daw.getEndTime().compareTo(end)<=0)); // lies within
        }
    }
    
    @Test
    public void testResourceDataForGivenTimeFrame() throws java.text.ParseException, IllegalStateException, ClientProtocolException, IOException, ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.GERMAN);
        TimePoint start = new MillisecondsTimePoint(dateFormat.parse("2013-11-09T07:00:00Z"));
        TimePoint end   = new MillisecondsTimePoint(dateFormat.parse("2013-11-09T07:10:00Z"));
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        IgtimiConnection connection = connectionFactory.connect(account);
        // URL is https://www.igtimi.com/api/v1/devices/data_access_windows?type=read&start_time=1383811200000&end_time=1383933600000&access_token=3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9
        Iterable<DataAccessWindow> daws = connection.getDataAccessWindows(Permission.read, start, end, /* deviceSerialNumbers; get all devices available for that time */ null);
        Set<String> deviceSerialNumbers = new HashSet<>();
        for (DataAccessWindow daw : daws) {
            deviceSerialNumbers.add(daw.getDeviceSerialNumber());
        }
        Iterable<Fix> windData = connection.getResourceData(start, end, deviceSerialNumbers, Type.gps_latlong, Type.AWA, Type.AWS, Type.HDG);
        assertFalse(Util.isEmpty(windData));
        boolean foundWind = false;
        for (Fix fix : windData) {
            foundWind = foundWind || fix instanceof AWA || fix instanceof AWS;
            assertTrue(fix.getTimePoint().compareTo(start) >= 0 && fix.getTimePoint().compareTo(end) <= 0);
        }
    }
    
}
