package com.sap.sailing.server.gateway.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.impl.RegattasJsonExportServlet;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class JsonExportTests {

    private RacingEventService racingEventService;

    @Before
    public void setUp() {
        racingEventService = new RacingEventServiceImpl();
    }

    @Test
    public void exportRegattasAsJsonTest() throws Exception {       
        String boatClassName = "49er";
        String regattaName = "Testregatta";
        Serializable id = new RegattaName(regattaName);
        racingEventService.getOrCreateRegatta(regattaName, boatClassName, id);
    
        HttpServletRequest request = mock(HttpServletRequest.class);       
        HttpServletResponse response = mock(HttpServletResponse.class);    
        when(request.getMethod()).thenReturn("GET");          
            
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        RegattasJsonExportServlet spyRegattasJsonExportServlet = spy(new RegattasJsonExportServlet());
        doReturn(racingEventService).when(spyRegattasJsonExportServlet).getService();
        
        spyRegattasJsonExportServlet.service(request, response);

        // the writer may not have been flushed yet...
        writer.flush(); 
        String jsonString = stringWriter.toString();
        
        Object obj= JSONValue.parse(jsonString);
        JSONArray array= (JSONArray) obj;

        assertTrue(array.size() == 1);

        JSONObject firstElement = (JSONObject) array.get(0);  
        String jsonName = (String) firstElement.get("name");
        String jsonBoatClass = (String) firstElement.get("boatclass");
        
        assertTrue(RegattaImpl.getFullName(regattaName, boatClassName).equals(jsonName));
        assertTrue(boatClassName.equals(jsonBoatClass));
    }
}
