package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceConfigurationJsonSerializer;

public class ConfigurationJsonGetServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 7704668926551060433L;

    public static final String PARAMS_CLIENT_ID = "client";
    public static final String PARAMS_CLIENT_UUID = "uuid";
    
    private final static Logger logger = Logger.getLogger(ConfigurationJsonGetServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String configurationName = request.getParameter(PARAMS_CLIENT_ID);
        final String configurationUuidAsString = request.getParameter(PARAMS_CLIENT_UUID);
        if (configurationName == null && configurationUuidAsString == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("Missing parameter: one of '%s' and '%s' must be provided.", PARAMS_CLIENT_ID, PARAMS_CLIENT_UUID));
        } else {
            final DeviceConfiguration configuration;
            if (configurationName != null) {
                logger.fine(String.format("Configuration requested by client %s.", configurationName));
                configuration = getService().getDeviceConfigurationByName(configurationName);
            } else {
                logger.fine(String.format("Configuration requested by id %s.", configurationUuidAsString));
                configuration = getService().getDeviceConfigurationById(UUID.fromString(configurationUuidAsString));
            }
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
}
