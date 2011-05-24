package com.sap.sailing.server;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AdminApp extends HttpServlet {
    private static final long serialVersionUID = -6849138354941569249L;
    
    private static final String PARAM_ACTION = "action";
    
    private static final String ACTION_NAME_LIST_EVENTS = "listevents";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        @SuppressWarnings("unchecked")
        Map<String, String[]> parameters = req.getParameterMap();
        String[] actions = parameters.get(PARAM_ACTION);
        if (actions != null) {
            for (String action : actions) {
                if (ACTION_NAME_LIST_EVENTS.equals(action)) {
                    
                }
            }
        } else {
            resp.getWriter().println("Hello admin!");
        }
    }
}
