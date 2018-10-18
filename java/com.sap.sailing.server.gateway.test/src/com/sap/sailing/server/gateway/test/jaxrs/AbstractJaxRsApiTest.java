package com.sap.sailing.server.gateway.test.jaxrs;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneUuidServiceFinderFactory;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sse.common.Color;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.ActionWithResult;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;
import com.sap.sse.security.shared.impl.UserGroupImpl;

public abstract class AbstractJaxRsApiTest {
    protected RacingEventService racingEventService;
    protected MongoDBService service;
    protected SecurityService securityService;
    protected DummyEventsRessource eventsResource;
    protected DummyRegattasResource regattasResource;
    protected DummyLeaderboardGroupsResource leaderboardGroupsResource;
    protected DummyLeaderboardsResource leaderboardsResource;
    protected DummyBoatsResource boatsResource;
    protected DummyCompetitorsResource competitorsResource;
    
    protected static SimpleDateFormat TIMEPOINT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        service = MongoDBConfiguration.getDefaultTestConfiguration().getService();
        service.getDB().dropDatabase();
        racingEventService = Mockito.spy(new RacingEventServiceImpl(/* clearPersistentCompetitorStore */ true,
                new MockSmartphoneUuidServiceFinderFactory(), /* restoreTrackedRaces */ false));

        UserGroupImpl defaultTenant = new UserGroupImpl(new UUID(0, 1), "defaultTenant");

        securityService = Mockito.mock(SecurityService.class);
        SecurityManager securityManager = Mockito.mock(org.apache.shiro.mgt.SecurityManager.class);
        Subject fakeSubject = Mockito.mock(Subject.class);

        SecurityUtils.setSecurityManager(securityManager);
        Mockito.doReturn(fakeSubject).when(securityManager).createSubject(Mockito.any());

        Mockito.doReturn(defaultTenant).when(securityService).getDefaultTenant();

        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgumentAt(4, ActionWithResult.class).run();
            }
        }).when(securityService).setOwnershipCheckPermissionForObjectCreationAndRevertOnError(Mockito.anyString(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgumentAt(4, ActionWithResult.class).run();
            }
        }).when(securityService).setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                Mockito.any(UserGroup.class), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(ActionWithResult.class));

        Mockito.doReturn(true).when(securityService)
                .hasCurrentUserReadPermission(Mockito.any(WithQualifiedObjectIdentifier.class));

        Mockito.doNothing().when(securityService).checkCurrentUserReadPermission(Mockito.any());

        Mockito.doReturn(true).when(fakeSubject).isAuthenticated();

        eventsResource = spyResource(new DummyEventsRessource());
        doReturn(getSecurityService()).when(eventsResource).getSecurityService();

        regattasResource = spyResource(new DummyRegattasResource());
        doReturn(getSecurityService()).when(regattasResource).getSecurityService();
        leaderboardGroupsResource = spyResource(new DummyLeaderboardGroupsResource());
        doReturn(getSecurityService()).when(leaderboardGroupsResource).getSecurityService();
        leaderboardsResource = spyResource(new DummyLeaderboardsResource());
        doReturn(getSecurityService()).when(leaderboardsResource).getSecurityService();
        boatsResource = spyResource(new DummyBoatsResource());
        doReturn(getSecurityService()).when(boatsResource).getSecurityService();
        competitorsResource = spyResource(new DummyCompetitorsResource());
        doReturn(getSecurityService()).when(competitorsResource).getSecurityService();
        
        doReturn(RaceLogTrackingAdapterFactory.INSTANCE.getAdapter(racingEventService.getBaseDomainFactory()))
                .when(leaderboardsResource).getRaceLogTrackingAdapter();
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    private <T extends AbstractSailingServerResource> T spyResource(T resource) {
        T spyResource = spy(resource);
        doReturn(racingEventService).when(spyResource).getService();
        return spyResource;
    }    
    
    protected String decodeResponseFromByteArray(Response response) throws UnsupportedEncodingException {
        byte[] entity = (byte[]) response.getEntity();
        return new String(entity, "UTF-8");
    }

    protected TimePoint parseTimepointFromJsonNumber(Long timePointAsJsonNumber) throws ParseException {
        TimePoint result = null;
        if (timePointAsJsonNumber != null) {
            result = new MillisecondsTimePoint(timePointAsJsonNumber);
        }
        return result;
    }

    protected TimePoint parseTimepointFromJsonString(String timePointAsJsonString) throws ParseException {
        TimePoint result = null;
        if (timePointAsJsonString != null && !timePointAsJsonString.isEmpty()) {
            Date date = TIMEPOINT_FORMATTER.parse(timePointAsJsonString);
            result = new MillisecondsTimePoint(date);
        }
        return result;
    }
     
    protected List<Competitor> createCompetitors(int numberOfCompetitorsToCreate) {
        List<Competitor> result = new ArrayList<Competitor>();
        for (int i = 1; i <= numberOfCompetitorsToCreate; i++) {
            String competitorName = "C" + i;
            Competitor competitor = new CompetitorImpl(new Integer(i), competitorName, "KYC", Color.RED, null, null, new TeamImpl("STG", Collections.singleton(
                                    new PersonImpl(competitorName, new NationalityImpl("GER"),
                                            /* dateOfBirth */ null, "This is famous "+competitorName)),
                                            new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                                            /* dateOfBirth */null, "This is Rigo, the coach")), 
                                    /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null); 
            result.add(competitor);
        }
        return result;
    }
}
