package com.sap.sailing.domain.igtimiadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

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
}
