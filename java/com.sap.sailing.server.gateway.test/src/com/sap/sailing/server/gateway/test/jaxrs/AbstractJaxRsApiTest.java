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

import javax.ws.rs.core.Response;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockSmartphoneUuidServiceFinderFactory;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sse.common.Color;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;

public abstract class AbstractJaxRsApiTest {
    protected RacingEventService racingEventService;
    protected MongoDBService service;    
    
    protected static SimpleDateFormat TIMEPOINT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public void setUp() throws Exception {
        service = MongoDBConfiguration.getDefaultTestConfiguration().getService();
        service.getDB().drop();
        racingEventService = new RacingEventServiceImpl(/* clearPersistentCompetitorStore */ true,
                new MockSmartphoneUuidServiceFinderFactory(), /* restoreTrackedRaces */ false);
    }

    protected <T extends AbstractSailingServerResource> T spyResource(T resource) {
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
