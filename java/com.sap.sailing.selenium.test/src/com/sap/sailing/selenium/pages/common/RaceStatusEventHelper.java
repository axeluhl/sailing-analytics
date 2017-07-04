package com.sap.sailing.selenium.pages.common;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sse.common.Base64Utils;

public class RaceStatusEventHelper {
    
    private static final MessageFormat RACE_LOG_URL = new MessageFormat("{0}sailingserver/rc/racelog?"
            + RaceLogServletConstants.PARAMS_LEADERBOARD_NAME + "={1}&" + RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME
            + "={2}&" + RaceLogServletConstants.PARAMS_RACE_FLEET_NAME + "={3}"); 
    
    public static RaceStatusEventHelper get(String contextRoot, String leaderboard, String raceColumn, String fleet) {
            return new RaceStatusEventHelper(contextRoot, leaderboard, raceColumn, fleet);
    }
    
    private final String raceLogUrl;
    
    private RaceStatusEventHelper(Object... parameters) {
        this.raceLogUrl = RACE_LOG_URL.format(parameters);
    }
    
    public void finishRace(Date finishDate) {
        sendRaceLogRaceStatusEvent(finishDate, finishDate, "FINISHED");
    }
    
    private void sendRaceLogRaceStatusEvent(Date timestamp, Date createdAt, String nextStatus) {
        Map<String, Object> jsonParams = new HashMap<>();
        jsonParams.put("@class", "RaceLogRaceStatusEvent");
        jsonParams.put("timestamp", timestamp.getTime());
        jsonParams.put("createdAt", createdAt.getTime());
        jsonParams.put("nextStatus", nextStatus);
        jsonParams.put("competitors", new JSONArray());
        jsonParams.put("passId", 0);
        jsonParams.put("id", UUID.randomUUID());
        sendRaceLogEvent(jsonParams);
    }
    
    private void sendRaceLogEvent(Map<String, Object> jsonParams) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(raceLogUrl).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + Base64Utils.toBase64("admin:admin".getBytes()));
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            connection.connect();
            connection.getOutputStream().write(new JSONObject(jsonParams).toString().getBytes());
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) throw new RuntimeException(responseCode + " " + connection.getResponseMessage());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
}
