package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.async.AsyncAction;

public class GetTargetTimeAction implements AsyncAction<Duration> {

    private MillisecondsTimePoint millisecondsTimePoint;
    private RegattaAndRaceIdentifier raceIdentifier;
    private SailingServiceAsync sailingService;

    public GetTargetTimeAction(SailingServiceAsync sailingService, MillisecondsTimePoint millisecondsTimePoint, RegattaAndRaceIdentifier raceIdentifier) {
        this.millisecondsTimePoint = millisecondsTimePoint;
        this.raceIdentifier = raceIdentifier;
        this.sailingService = sailingService;
    }

    @Override
    public void execute(AsyncCallback<Duration> callback) {
        sailingService.getEstimatedTargetTime(millisecondsTimePoint,raceIdentifier,new AsyncCallback<Duration>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(Duration result) {
                callback.onSuccess(result);
            }
        });
    }

}
