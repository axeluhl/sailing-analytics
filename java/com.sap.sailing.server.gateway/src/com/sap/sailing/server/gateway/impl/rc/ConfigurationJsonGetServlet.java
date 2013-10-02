package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;

public class ConfigurationJsonGetServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 7704668926551060433L;

    public static final String PARAMS_CLIENT_ID = "client";
    
    private final static Logger logger = Logger.getLogger(ConfigurationJsonGetServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String clientId = request.getParameter(PARAMS_CLIENT_ID);
        if (clientId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("Missing parameter '%s'.", PARAMS_CLIENT_ID));
            return;
        }
        
        logger.fine(String.format("Configuration requested by client %s.", clientId));

        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

}
