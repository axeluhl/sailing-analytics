package com.sap.sailing.gwt.ui.actions;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.gwt.client.async.AsyncAction;

public class GetRaceTimesInfoAction implements AsyncAction<List<RaceTimesInfoDTO>> {
    private final SailingServiceAsync sailingService;
    private final Collection<RegattaAndRaceIdentifier> raceIdentifiers;

    public GetRaceTimesInfoAction(SailingServiceAsync sailingService, Collection<RegattaAndRaceIdentifier> raceIdentifiers) {
        this.sailingService = sailingService;
        this.raceIdentifiers = raceIdentifiers;
    }

    @Override
    public void execute(AsyncCallback<List<RaceTimesInfoDTO>> callback) {
        sailingService.getRaceTimesInfos(raceIdentifiers, callback);
    }
}