package com.sap.sailing.domain.tracking.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.tracking.StartTimeChangedListener;
import com.sap.sse.common.TimePoint;

public class StartTimeResetHandler extends UpdateHandler implements StartTimeChangedListener {
    
    private static final String ACTION = "resetStartTime";
    
    private final static Logger logger = Logger.getLogger(StartTimeResetHandler.class.getName());
    
    public StartTimeResetHandler(URI updateURI, String username, String password, Serializable eventId, Serializable raceId) {
        super(updateURI, ACTION, username, password, eventId, raceId);
    }

    @Override
    public void startTimeChanged(TimePoint newStartTime) throws MalformedURLException, IOException {
        if (!isActive() || newStartTime != null) {
            return;
        }
        URL startTimeUpdateURL = buildUpdateURL();
        logger.info("Using " + startTimeUpdateURL.toString() + " for the start reset!");
        HttpURLConnection connection =  (HttpURLConnection) startTimeUpdateURL.openConnection();
        try {
            connection = setConnectionProperties(connection);
            try {
                checkAndLogUpdateResponse(connection);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            } else {
                logger.severe("Connection to TracTrac start time reset URL " + startTimeUpdateURL.toString() + " could not be established");
            }
        }
    }
}
