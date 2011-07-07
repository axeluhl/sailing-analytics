package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDAO;

/**
 * The async counterpart of {@link SailingService}
 */
public interface SailingServiceAsync {
    void listEvents(AsyncCallback<List<EventDAO>> callback);

    void listRacesInEvent(String eventJsonURL, AsyncCallback<List<RaceRecordDAO>> callback);

    void track(RaceRecordDAO rr, String liveURI, String storedURI, AsyncCallback<Void> callback);

    void getPreviousConfigurations(AsyncCallback<List<TracTracConfigurationDAO>> callback);

    void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI,
            AsyncCallback<Void> callback);

    void stopTrackingEvent(String eventName, AsyncCallback<Void> callback);

    void stopTrackingRace(String eventName, String raceName, AsyncCallback<Void> asyncCallback);

    void startTrackingWind(String eventName, String raceName, int port, boolean correctDeclination,
            AsyncCallback<Void> callback);
}
