package com.sap.sailing.server.gateway.test.support;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CreateSessionServlet extends HttpServlet {
    private static final long serialVersionUID = -880795218153271730L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
