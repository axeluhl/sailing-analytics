package com.sap.sailing.racecommittee.app.services.sending;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import com.sap.sailing.android.shared.services.sending.MessageSendingService;
import com.sap.sailing.android.shared.services.sending.ServerReplyCallback;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

import android.content.Context;
import android.content.Intent;

public class EventSendingServiceUtil {

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
    public static Intent createEventIntent(Context context, ManagedRace race, Serializable eventId,
            String serializedEventAsJson, Class<? extends ServerReplyCallback> callbackClass)
            throws UnsupportedEncodingException {
        String url = MessageSendingService.getRaceLogEventSendAndReceiveUrl(context, race.getRaceGroup().getName(),
                race.getName(), race.getFleet().getName());
        return MessageSendingService.createMessageIntent(context, url, race.getId(), eventId, serializedEventAsJson,
                callbackClass);
    }
}
