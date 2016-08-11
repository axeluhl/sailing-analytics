package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationIdentifierImpl;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceConfigurationJsonSerializer;

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
        
        DeviceConfigurationIdentifier identifier = new DeviceConfigurationIdentifierImpl(clientId);
        
        logger.fine(String.format("Configuration requested by client %s.", identifier));
        
        DeviceConfiguration configuration = getService().getDeviceConfiguration(identifier);
        if (configuration != null) {
            JsonSerializer<DeviceConfiguration> serializer = DeviceConfigurationJsonSerializer.create();
            JSONObject json = serializer.serialize(configuration);
            response.setCharacterEncoding("UTF-8");
            json.writeJSONString(response.getWriter());
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No configuration for given identifier.");
        }
    }

}
