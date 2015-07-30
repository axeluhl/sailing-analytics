package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;

public class LeaderboardsJsonGetServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = -2193421590275280102L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONArray jsonLeaderboards = new JSONArray();
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        for (String leaderboardName : leaderboards.keySet()) {
            jsonLeaderboards.add(leaderboardName);
        }
        setJsonResponseHeader(resp);
        jsonLeaderboards.writeJSONString(resp.getWriter());
    }

}
