package com.sap.sailing.xcelsiusadapter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.server.gateway.SailingServerHttpServlet;

public class XcelsiusServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = -6849138354942569249L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final String action = req.getParameter("action");

        try {
            if ((action != null) && !"".equals(action)) {
                if ("leaderboardData".equalsIgnoreCase(action)) {
                    final LeaderboardData leaderboardData = new LeaderboardData(req, res, getService());
                    leaderboardData.perform();
                    return;
                }
                throw new ServletException("Unknown action " + action);
                return;
            }
            throw new ServletException("Please use the action= parameter to specify an action.", res);
            return;
        } catch (Exception e) {
            throw (new ServletException(e));
        }
    }
}
