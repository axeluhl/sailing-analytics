package com.sap.sailing.server.gateway.ess40;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.meta.LeaderboardGroupMetaLeaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;

public class ESS40JspForwardServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = -1017961881555515288L;

    private static final String PARAM_NAME_LEADERBOARDNAME = "leaderboardName";
    private static final String PARAM_NAME_SHOWDETAILS = "showDetails";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RacingEventService racingEventService = getService();
        String pathInfo = request.getPathInfo();
        String jspPath = "/WEB-INF/jsp";
        String leaderboardNameParam = request.getParameter(PARAM_NAME_LEADERBOARDNAME);
        String showDetailsParam = request.getParameter(PARAM_NAME_SHOWDETAILS);
        Boolean showRaceDetails = showDetailsParam != null && showDetailsParam.equalsIgnoreCase("true");
        
        if (leaderboardNameParam == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "You need to specify a leaderboard name using the "+
                    PARAM_NAME_LEADERBOARDNAME+" parameter");
            return;
        }

        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardNameParam);

        if(pathInfo.equals("/leaderboard") && racingEventService != null && leaderboard != null) {
            String jspFile = null;
            if(leaderboard instanceof LeaderboardGroupMetaLeaderboard) {
                jspFile = "overallLeaderboard.jsp";
            } else {
                jspFile = "leaderboard.jsp";
            }
            request.setAttribute("showRaceDetails", showRaceDetails);
            request.setAttribute("leaderboard", leaderboard);           
            request.setAttribute("racingEventService", racingEventService);           

            ServletContext sc = getServletContext();
            RequestDispatcher requestDispatcher = sc.getRequestDispatcher(jspPath + "/" + jspFile);
            requestDispatcher.forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during leaderboard export.");
        }            
    }

}
