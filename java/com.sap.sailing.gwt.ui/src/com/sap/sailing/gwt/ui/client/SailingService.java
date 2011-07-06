package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDAO;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("sailing")
public interface SailingService extends RemoteService {
    List<TracTracConfigurationDAO> getPreviousConfigurations() throws Exception;
    
    List<EventDAO> listEvents();

    List<RaceRecordDAO> listRacesInEvent(String eventJsonURL) throws Exception;

    void track(RaceRecordDAO rr, String liveURI, String storedURI) throws Exception;

    void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI) throws Exception;

    void stopTrackingEvent(String eventName) throws Exception;

    void stopTrackingRace(String eventName, String raceName) throws Exception;
}
