package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;

public class LeaderboardsJsonExportServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = -2193421590275280102L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // to allow access to the json document directly from a client side javascript
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JSONArray jsonLeaderboards = new JSONArray();
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        for (String leaderboardName : leaderboards.keySet()) {
            jsonLeaderboards.add(leaderboardName);
        }
        jsonLeaderboards.writeJSONString(resp.getWriter());
    }

}
