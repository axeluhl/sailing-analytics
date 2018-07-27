package com.sap.sailing.domain.tracking.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.StartTimeChangedListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.util.LaxRedirectStrategyForAllRedirectResponseCodes;

public class StartTimeUpdateHandler extends UpdateHandler implements StartTimeChangedListener {
    private final static Logger logger = Logger.getLogger(StartTimeUpdateHandler.class.getName());

    private final static String ACTION = "update_race_start_time";
    private final static String ACTION_START_TRACKING = "start_tracking";
    private final static String FIELD_RACE_START_TIME = "race_start_time";
    private final static String FIELD_TRACKING_START_TIME = "tracking_start_time";

    /**
     * The regatta is required in order to query {@link Regatta#isControlTrackingFromStartAndFinishTimes()} when
     * a new start time is received.
     */
    private final Regatta regatta;
    
    public StartTimeUpdateHandler(URI updateURI, String tracTracUsername, String tracTracPassword,
            Serializable tracTracEventId, Serializable raceId, Regatta regatta) {
        super(updateURI, ACTION, tracTracUsername, tracTracPassword, tracTracEventId, raceId);
        this.regatta = regatta;
    }

    @Override
    public void startTimeChanged(TimePoint newStartTime) throws MalformedURLException, IOException, URISyntaxException {
        if (isActive()) {
            if (newStartTime == null) {
                /*
                 * Do not reset start time based on request by Jorge from TracTrac:
                 * 
                 * """
                 * We have detected that when you want to update the race start time your
                 * system sends before a message to reset the race start time. When your
                 * system invokes the first service our system changes the race start time
                 * to null and this value causes some secondary effects in our side.
                 * 
                 * Can you just send the update_race_start_time without the
                 * reset_race_start_time? The reset_race_start_time service has to be used
                 * if you want to set the value to null.
                 * """
                 */
            } else {
                HashMap<String, String> additionalParameters = new HashMap<String, String>();
                additionalParameters.put(FIELD_RACE_START_TIME, String.valueOf(newStartTime.asMillis()));
                URL startTimeUpdateURL = buildUpdateURL(additionalParameters);
                
                logger.info("Using " + startTimeUpdateURL.toString() + " for the start time update!");
                HttpURLConnection connection = (HttpURLConnection) startTimeUpdateURL.openConnection();
                try {
                    connection = setConnectionProperties(connection);
                    try {
                        checkAndLogUpdateResponse(connection);
                    } catch (ParseException e) {
                        logger.log(Level.INFO, "Error parsing TracTrac response for start time update", e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    } else {
                        logger.severe("Connection to TracTrac Course Update URL " + startTimeUpdateURL.toString() + " could not be established");
                    }
                }
                if (regatta.isControlTrackingFromStartAndFinishTimes()) {
                    // make sure tracking is started on TracTrac's side and the start of tracking time is set
                    // to five minutes before start:
                    final URI startTrackingURI = getActionURI(ACTION_START_TRACKING);
                    final HttpPost request = new HttpPost(startTrackingURI);
                    final List<BasicNameValuePair> params = getDefaultParametersAsNewList();
                    params.add(new BasicNameValuePair(FIELD_TRACKING_START_TIME, String.valueOf(newStartTime.minus(
                            TrackedRace.START_TRACKING_THIS_MUCH_BEFORE_RACE_START).asMillis())));
                    request.setEntity(new UrlEncodedFormEntity(params));
                    final AbstractHttpClient client = new SystemDefaultHttpClient();
                    client.setRedirectStrategy(new LaxRedirectStrategyForAllRedirectResponseCodes());
                    logger.info("Using " + startTrackingURI.toString() + " to start tracking");
                    final HttpResponse response = client.execute(request);
                    try {
                        parseAndLogResponse(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
                    } catch (ParseException e) {
                        logger.log(Level.INFO, "Error parsing TracTrac response for start tracking", e);
                    }
                }
            }
        }
    }
}
