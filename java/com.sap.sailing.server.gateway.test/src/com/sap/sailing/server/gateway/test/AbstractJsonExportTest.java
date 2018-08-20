package com.sap.sailing.server.gateway.test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sse.common.Color;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;

public abstract class AbstractJsonExportTest {
    protected RacingEventService racingEventService;
    protected MongoDBService service;    
    
    protected static SimpleDateFormat TIMEPOINT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public void setUp() {
        service = MongoDBConfiguration.getDefaultTestConfiguration().getService();
        service.getDB().dropDatabase();

        racingEventService = new RacingEventServiceImpl();
    }

    protected String callJsonHttpServlet(AbstractJsonHttpServlet jsonServlet, String GetOrPostMethod, Map<String, String> parameters) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);       
        HttpServletResponse response = mock(HttpServletResponse.class);    
        when(request.getMethod()).thenReturn(GetOrPostMethod);          

        if(parameters != null) {
            for(Map.Entry<String, String> entry: parameters.entrySet()) {
                when(request.getParameter(entry.getKey())).thenReturn(entry.getValue());
            }
        }

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        AbstractJsonHttpServlet spyJsonExportServlet = spy(jsonServlet);
        doReturn(racingEventService).when(spyJsonExportServlet).getService();
        
        spyJsonExportServlet.service(request, response);

        // the writer may not have been flushed yet...
        writer.flush(); 
        
        return stringWriter.toString();
    }

    protected TimePoint parseTimepointFromJsonString(String timePointAsJsonString) throws ParseException {
        TimePoint result = null;
        if(timePointAsJsonString != null && !timePointAsJsonString.isEmpty()) {
            Date date = TIMEPOINT_FORMATTER.parse(timePointAsJsonString);
            result = new MillisecondsTimePoint(date);
        }
        return result;
    }
     
    protected List<Competitor> createCompetitors(int numberOfCompetitorsToCreate) {
        List<Competitor> result = new ArrayList<Competitor>();
        for (int i = 1; i <= numberOfCompetitorsToCreate; i++) {
            String competitorName = "C" + i;
            Competitor competitor = new CompetitorImpl(UUID.randomUUID(), competitorName, "KYC", Color.RED, null, null, new TeamImpl("STG", Collections.singleton(
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
