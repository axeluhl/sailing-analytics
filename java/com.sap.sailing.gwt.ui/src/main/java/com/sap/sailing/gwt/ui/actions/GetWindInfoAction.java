package com.sap.sailing.gwt.ui.actions;

import java.util.Collection;
import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sse.gwt.client.async.AsyncAction;

public class GetWindInfoAction implements AsyncAction<WindInfoForRaceDTO> {
    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private Date from;
    private long millisecondsStepWidth;
    private int numberOfFixes;
    private Collection<String> windSourceTypeNames;
    
    private long resolutionInMilliseconds;
    private Date fromDate;
    private Date toDate;
    
    private enum CallVariants { Variant1, Variant2 };
    private final CallVariants callVariant;

    public GetWindInfoAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier, Date from,
            long millisecondsStepWidth, int numberOfFixes, Collection<String> windSourceTypeNames) {
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.from = from;
        this.millisecondsStepWidth = millisecondsStepWidth;
        this.numberOfFixes = numberOfFixes;
        this.windSourceTypeNames = windSourceTypeNames;
        callVariant = CallVariants.Variant1;
    }

    public GetWindInfoAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier, Date fromDate,
            Date toDate, long resolutionInMilliseconds, Collection<String> windSourceTypeNames) {
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.resolutionInMilliseconds = resolutionInMilliseconds;
        this.windSourceTypeNames = windSourceTypeNames;
        callVariant = CallVariants.Variant2;
    }

    @Override
    public void execute(AsyncCallback<WindInfoForRaceDTO> callback) {
        switch (callVariant) {
        case Variant1:
            sailingService.getAveragedWindInfo(raceIdentifier, from, millisecondsStepWidth, numberOfFixes, windSourceTypeNames, callback);
            break;
        case Variant2:
            sailingService.getAveragedWindInfo(raceIdentifier, fromDate, toDate, resolutionInMilliseconds, windSourceTypeNames, callback);
            break;
        }
    }
}