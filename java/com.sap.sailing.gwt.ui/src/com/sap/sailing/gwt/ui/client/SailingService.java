package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDAO;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("sailing")
public interface SailingService extends RemoteService {
    List<TracTracConfigurationDAO> getPreviousConfigurations() throws Exception;
    
    List<EventDAO> listEvents();

    List<RaceRecordDAO> listRacesInEvent(String eventJsonURL) throws Exception;

    void track(RaceRecordDAO rr, String liveURI, String storedURI, boolean trackWind, boolean correctWindByDeclination) throws Exception;

    void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI) throws Exception;

    void stopTrackingEvent(String eventName) throws Exception;

    void stopTrackingRace(String eventName, String raceName) throws Exception;

    WindInfoForRaceDAO getWindInfo(String eventName, String raceName, long fromAsMilliseconds, long toAsMilliseconds);

    void setWind(String eventName, String raceName, WindDAO wind);
}
