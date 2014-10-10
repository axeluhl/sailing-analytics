package com.sap.sailing.racecommittee.app.services.sending;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;

import com.sap.sailing.android.shared.services.sending.MessageSendingService;
import com.sap.sailing.android.shared.services.sending.ServerReplyCallback;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class EventSendingServiceUtil {
    /**
     * a UUID that identifies this client session; can be used, e.g., to let the server identify subsequent requests coming from the same client
     */
    public final static UUID uuid = UUID.randomUUID();
    
    public static String getRaceLogEventSendAndReceiveUrl(Context context, final String raceGroupName,
            final String raceName, final String fleetName) {
        String url = String.format("%s/sailingserver/rc/racelog?"+
                RaceLogServletConstants.PARAMS_LEADERBOARD_NAME+"=%s&"+
                RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME+"=%s&"+
                RaceLogServletConstants.PARAMS_RACE_FLEET_NAME+"=%s&"+
                RaceLogServletConstants.PARAMS_CLIENT_UUID+"=%s",
                AppPreferences.on(context).getServerBaseURL(), URLEncoder.encode(raceGroupName),
                URLEncoder.encode(raceName), URLEncoder.encode(fleetName), uuid);
        return url;
    }

    /**
     * Creates an intent that contains the event to be sent and the race id which shall be sent to the back end. See
     * constants in <code>AddEntryToRaceLogJsonPostServlet</code> for URL construction rules.
     * 
     * @param context
     *            the context of the app
     * @param race
     *            the race for which the event was created
     * @param serializedEventAsUrlEncodedJson
     *            the event serialized to JSON
     * @param callbackClass
     *            the class of the callback which should process the server reply
     * @return the intent that shall be sent to the EventSendingService
     */
    public static Intent createEventIntent(Context context, ManagedRace race, Serializable eventId, String serializedEventAsJson,
            Class<? extends ServerReplyCallback> callbackClass) {
        String url = getRaceLogEventSendAndReceiveUrl(context, 
                race.getRaceGroup().getName(), race.getName(), race.getFleet().getName());
        return MessageSendingService.createMessageIntent(context, url, race.getId(), eventId, serializedEventAsJson, callbackClass);
    }
}
