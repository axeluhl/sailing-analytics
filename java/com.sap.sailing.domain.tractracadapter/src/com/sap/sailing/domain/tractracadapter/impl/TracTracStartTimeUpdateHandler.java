package com.sap.sailing.domain.tractracadapter.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.tracking.StartTimeChangedListener;
import com.sap.sse.common.TimePoint;

public class TracTracStartTimeUpdateHandler extends UpdateHandler implements StartTimeChangedListener {
    
    private static final String ACTION = "update_race_start_time";
    private final TracTracStartTimeResetHandler resetHandler;
    
    private final static Logger logger = Logger.getLogger(TracTracStartTimeUpdateHandler.class.getName());
    private final static String FIELD_RACE_START_TIME = "race_start_time";
    
    public TracTracStartTimeUpdateHandler(URI updateURI, String tracTracUsername, String tracTracPassword, Serializable tracTracEventId, Serializable raceId) {
        super(updateURI, ACTION, tracTracUsername, tracTracPassword, tracTracEventId, raceId);
        resetHandler = new TracTracStartTimeResetHandler(updateURI, tracTracUsername, tracTracPassword, tracTracEventId, raceId);
    }

    @Override
    public void startTimeChanged(TimePoint newStartTime) throws MalformedURLException, IOException {
        if (!isActive()) {
            return;
        }
        
        if (newStartTime == null) {
            // reset start time with TracTrac
            resetHandler.startTimeChanged(null);
        } else {
            HashMap<String, String> additionalParameters = new HashMap<String, String>();
            additionalParameters.put(FIELD_RACE_START_TIME, String.valueOf(newStartTime.asMillis()));
            URL startTimeUpdateURL = buildUpdateURL(additionalParameters);
            
            logger.info("Using " + startTimeUpdateURL.toString() + " for the start time update!");
            HttpURLConnection connection = (HttpURLConnection) startTimeUpdateURL.openConnection();
            try {
                setConnectionProperties(connection);
                try {
                    checkAndLogUpdateResponse(connection);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                } else {
                    logger.severe("Connection to TracTrac Course Update URL " + startTimeUpdateURL.toString() + " could not be established");
                }
            }
        }
    }
    
}
