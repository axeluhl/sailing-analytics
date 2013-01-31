package com.sap.sailing.server.gateway.impl;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;

public class SimpleJspForwardServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = -1017961881555515288L;

    private static final String PARAM_NAME_LEADERBOARDNAME = "leaderboardName";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RacingEventService racingEventService = getService();
        if(racingEventService != null) {
            String query = request.getPathInfo();
            String jspPath = "/WEB-INF/jsp";
            String jspFile = "error.jsp";
            if(query.equals("/leaderboard")) {
                String leaderboardName = request.getParameter(PARAM_NAME_LEADERBOARDNAME);
                if (leaderboardName != null) {
                    Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
                    request.setAttribute("leaderboard", leaderboard);           
                    jspFile = "leaderboard.jsp";
                }
            }
            request.setAttribute("racingEventService", racingEventService);           
            ServletContext sc = getServletContext();
            RequestDispatcher requestDispatcher = sc.getRequestDispatcher(jspPath + "/" + jspFile);
            requestDispatcher.forward(request, response);
        }
    }

}
