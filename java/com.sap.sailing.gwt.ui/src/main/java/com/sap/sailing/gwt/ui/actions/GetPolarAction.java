package com.sap.sailing.gwt.ui.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sse.gwt.client.async.AsyncAction;

/**
 * An asynchronous action to determine whether a suitable polar diagram is available for the race shown on {@link RaceMap}
 * 
 * @author Christopher Ronnewinkel (D036654)
 * 
 */
public class GetPolarAction implements AsyncAction<Boolean> {
    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    
    public GetPolarAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier) {
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
    }

    @Override
    public void execute(AsyncCallback<Boolean> callback) {
        sailingService.getPolarResults(raceIdentifier, callback);
    }
}