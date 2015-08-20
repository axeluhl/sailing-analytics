package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.IgtimiWindListener;
import com.sap.sse.common.TimePoint;

/**
 * Shows the state of wind receivers regardless of them being attached to a race. 
 * Currently Igtimi is supported.
 * 
 * @author Simon Marcel Pamies
 *
 */
public class WindStatusJsonServlet extends WindStatusServlet implements IgtimiWindListener, BulkFixReceiver {

    private static final long serialVersionUID = 6091476602985063675L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String reinitializeWindReceiverParameter = req.getParameter(PARAM_RELOAD_WIND_RECEIVER);
        initializeWindReceiver(reinitializeWindReceiverParameter != null && reinitializeWindReceiverParameter.equalsIgnoreCase("true"));
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
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
                    windInformation.put("lastIgtimiWindFixInMillisecondsAgo", lastFixDiffInMs);
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
                    windFix.put("windSpeedInKilometersPerHour", message.getWind().getKilometersPerHour());
                    windFix.put("windSpeedInKnots", message.getWind().getKnots());
                    windFix.put("measurementTimePointAsMillis", message.getWind().getTimePoint().asMillis());
                    windFixesIgtimi.add(windFix);
                }
            }
        } 
        windInformation.put("windFixes", windFixesIgtimi);
        out.print(windInformation.toJSONString());
        out.close();
    }

}
