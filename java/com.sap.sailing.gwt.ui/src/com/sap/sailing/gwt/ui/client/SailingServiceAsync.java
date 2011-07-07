package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDAO;

/**
 * The async counterpart of {@link SailingService}
 */
public interface SailingServiceAsync {
    void listEvents(AsyncCallback<List<EventDAO>> callback);

    void listRacesInEvent(String eventJsonURL, AsyncCallback<List<RaceRecordDAO>> callback);

    void track(RaceRecordDAO rr, String liveURI, String storedURI, boolean trackWind, boolean correctWindByDeclination,
            AsyncCallback<Void> callback);

    void getPreviousConfigurations(AsyncCallback<List<TracTracConfigurationDAO>> callback);

    void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI,
            AsyncCallback<Void> callback);

    void stopTrackingEvent(String eventName, AsyncCallback<Void> callback);

    void stopTrackingRace(String eventName, String raceName, AsyncCallback<Void> asyncCallback);

    void getWindInfo(String eventName, String raceName, long fromAsMilliseconds, long toAsMilliseconds,
            AsyncCallback<WindInfoForRaceDAO> callback);

    void setWind(String eventName, String raceName, WindDAO wind, AsyncCallback<Void> callback);

}
