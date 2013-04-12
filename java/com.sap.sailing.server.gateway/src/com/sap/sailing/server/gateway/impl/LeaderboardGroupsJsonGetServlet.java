package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;

public class LeaderboardGroupsJsonGetServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = -2193421590275280102L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONArray jsonLeaderboardGroups = new JSONArray();
        Map<String, LeaderboardGroup> leaderboardGroups = getService().getLeaderboardGroups();
        for (String leaderboardGroupName : leaderboardGroups.keySet()) {
            jsonLeaderboardGroups.add(leaderboardGroupName);
        }
        setJsonResponseHeader(resp);
        jsonLeaderboardGroups.writeJSONString(resp.getWriter());
    }

}
