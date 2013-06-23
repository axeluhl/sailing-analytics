package com.sap.sailing.server.operationaltransformation;

import org.json.simple.JSONObject;

public class CreationCount {
    
    private int leaderboardCount = 0;
    private int leaderboardGroupCount = 0;
    private int eventCount = 0;
    private int regattaCount = 0;

    public void addOneLeaderboard() {
        leaderboardCount++;
    }
    
    public void addOneLeaderboardGroup() {
        leaderboardGroupCount++;
    }
    
    public void addOneEvent() {
        eventCount++;
    }

    public void addOneRegatta() {
        regattaCount++;
        
    }
    public JSONObject toJson() {
        JSONObject countJson = new JSONObject();
        countJson.put("leaderboards", leaderboardCount);
        countJson.put("leaderboardGroups", leaderboardGroupCount);
        countJson.put("events", eventCount);
        countJson.put("regattas", regattaCount);
        return countJson;
    }


   
}
