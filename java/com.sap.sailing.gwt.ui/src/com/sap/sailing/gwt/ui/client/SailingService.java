package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceRecordDAO;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("sailing")
public interface SailingService extends RemoteService {
    List<EventDAO> listEvents();

    List<RaceRecordDAO> listRacesInEvent(String eventJsonURL) throws Exception;
}
