package com.sap.sailing.server.gateway.impl;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;

public class SimpleJspForwardServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = -1017961881555515288L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RacingEventService racingEventService = getService();
        if(racingEventService != null) {
            request.setAttribute("racingEventService", racingEventService);           
            ServletContext sc = getServletContext();
            RequestDispatcher requestDispatcher = sc.getRequestDispatcher("/WEB-INF/jsp/listEvents.jsp");
            requestDispatcher.forward(request, response);
        }
    }

}
