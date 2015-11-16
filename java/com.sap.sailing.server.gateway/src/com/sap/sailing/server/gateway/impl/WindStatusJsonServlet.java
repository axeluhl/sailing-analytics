package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.IgtimiWindListener;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Shows the state of wind receivers regardless of them being attached to a race. 
 * Currently Igtimi is supported.
 * 
 * @author Simon Marcel Pamies
 *
 */
public class WindStatusJsonServlet extends WindStatusServlet implements IgtimiWindListener, BulkFixReceiver {

    private static final long serialVersionUID = 6091476602985063675L;
    private static final String PARAM_SHOW_DUMMY_DATA = "showDummyData";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String reinitializeWindReceiverParameter = req.getParameter(PARAM_RELOAD_WIND_RECEIVER);
        String showDummyDataAsString = req.getParameter(PARAM_SHOW_DUMMY_DATA);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        boolean showDummyData = showDummyDataAsString != null && showDummyDataAsString.equalsIgnoreCase("true");
        if (showDummyData) {
            out.print(generateDummyData().toJSONString());
        } else {
            initializeWindReceiver(reinitializeWindReceiverParameter != null && reinitializeWindReceiverParameter.equalsIgnoreCase("true"));
            JSONObject windInformation = new JSONObject();
            windInformation.put("windFixesReceivedSoFar", getIgtimiMessagesRawCount()+getLastExpeditionMessages().size());
            JSONArray windFixesIgtimi = new JSONArray();
            if (getLastIgtimiMessages() != null && !getLastIgtimiMessages().isEmpty()) {
                for(Entry<String, Deque<IgtimiMessageInfo>> deviceAndMessagesList: getLastIgtimiMessages().entrySet()) {
                    final Deque<IgtimiMessageInfo> copyOfLastIgtimiMessages;
                    synchronized (deviceAndMessagesList.getValue()) {
                        copyOfLastIgtimiMessages = new ArrayDeque<>(deviceAndMessagesList.getValue());
                    }
                    if(copyOfLastIgtimiMessages.size() > 0) {
                        TimePoint latestTimePoint = copyOfLastIgtimiMessages.peek().getWind().getTimePoint(); 
                        long lastFixDiffInMs = System.currentTimeMillis() - latestTimePoint.asMillis();
                        windInformation.put("lastWindFixMillisecondsAgo", lastFixDiffInMs);
                    }
                    Iterator<IgtimiMessageInfo> messageIt = copyOfLastIgtimiMessages.iterator();
                    while (messageIt.hasNext()){
                        JSONObject windFix = new JSONObject();
                        windFix.put("sensorId", deviceAndMessagesList.getKey());
                        IgtimiMessageInfo message = messageIt.next();
                        windFix.put("sensorPositionLatitude", message.getWind().getPosition().getLatDeg());
                        windFix.put("sensorPositionLongitude", message.getWind().getPosition().getLngDeg());
                        windFix.put("windFromInDegrees", message.getWind().getFrom().getDegrees());
                        windFix.put("windToInDegrees", message.getWind().getFrom().reverse().getDegrees());
                        windFix.put("windSpeedInMetersPerSecond", message.getWind().getMetersPerSecond());
                        windFix.put("windSpeedInStatuteMilesPerHour", message.getWind().getStatuteMilesPerHour());
                        windFix.put("windSpeedInKilometersPerHour", message.getWind().getKilometersPerHour());
                        windFix.put("windSpeedInKnots", message.getWind().getKnots());
                        windFix.put("measurementTimePointAsMillis", message.getWind().getTimePoint().asMillis());
                        windFixesIgtimi.add(windFix);
                    }
                }
            } 
            windInformation.put("windFixes", windFixesIgtimi);
            out.print(windInformation.toJSONString());
        }
        out.close();
    }
    
    @SuppressWarnings("serial")
    private JSONObject generateDummyData() {
        List<Map<String, Object>> dummyWindFixes = new ArrayList<Map<String,Object>>();
        dummyWindFixes.add(new HashMap<String, Object>() {{
            put("sensorId", "DD-EE-HG");
            put("sensorPositionLatitude", 49.2511599999);
            put("sensorPositionLongitude", 8.624829999);
            put("windFromInDegrees", 90.0);
            put("windToInDegrees", 270.0);
            put("windSpeedInMetersPerSecond", 6.944444);
            put("windSpeedInKilometersPerHour", 25.0);
            put("windSpeedInStatuteMilesPerHour", 15.43);
            put("windSpeedInKnots", 13.498920086);
            put("measurementTimePointAsMillis", MillisecondsTimePoint.now().minus(Duration.ONE_SECOND.times(4)).asMillis());
        }});
        dummyWindFixes.add(new HashMap<String, Object>() {{
            put("sensorId", "DD-AA-EF");
            put("sensorPositionLatitude", 49.244489999);
            put("sensorPositionLongitude", 8.6253399999);
            put("windFromInDegrees", 100.0);
            put("windToInDegrees", 280.0);
            put("windSpeedInMetersPerSecond", 6.944444);
            put("windSpeedInKilometersPerHour", 25.0);
            put("windSpeedInStatuteMilesPerHour", 15.43);
            put("windSpeedInKnots", 13.498920086);
            put("measurementTimePointAsMillis", MillisecondsTimePoint.now().minus(Duration.ONE_SECOND.times(5)).asMillis());
        }});
        dummyWindFixes.add(new HashMap<String, Object>() {{
            put("sensorId", "DD-UU-KL");
            put("sensorPositionLatitude", 49.243539999);
            put("sensorPositionLongitude", 8.6146099999);
            put("windFromInDegrees", 85.0);
            put("windToInDegrees", 265.0);
            put("windSpeedInMetersPerSecond", 6.944444);
            put("windSpeedInKilometersPerHour", 25.0);
            put("windSpeedInStatuteMilesPerHour", 15.43);
            put("windSpeedInKnots", 13.498920086);
            put("measurementTimePointAsMillis", MillisecondsTimePoint.now().minus(Duration.ONE_SECOND.times(6)).asMillis());
        }});
        JSONObject windInformation = new JSONObject();
        windInformation.put("windFixesReceivedSoFar", 3);
        JSONArray windFixesIgtimi = new JSONArray();
        windInformation.put("lastWindFixMillisecondsAgo", 1239876);
        for (Map<String, Object> windDummyFix : dummyWindFixes) {
            JSONObject windFix = new JSONObject();
            windFix.putAll(windDummyFix);
            windFixesIgtimi.add(windFix);
        }
        windInformation.put("windFixes", windFixesIgtimi);
        return windInformation; 
    }

}
