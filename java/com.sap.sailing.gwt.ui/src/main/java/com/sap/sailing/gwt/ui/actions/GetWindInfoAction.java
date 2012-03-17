package com.sap.sailing.gwt.ui.actions;

import java.util.Collection;
import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;

public class GetWindInfoAction extends DefaultAsyncAction<WindInfoForRaceDTO>
{
    private final SailingServiceAsync sailingService;
    private final RaceIdentifier raceIdentifier;
    private final Date from;
    private long millisecondsStepWidth;
    private int numberOfFixes;
    private Collection<String> windSourceTypeNames;
    
    private WindInfoForRaceDTO result;
    
    private AsyncCallback<WindInfoForRaceDTO> callback;

    public GetWindInfoAction(SailingServiceAsync sailingService, RaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, Collection<String> windSourceTypeNames) {
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.from = from;
        this.millisecondsStepWidth = millisecondsStepWidth;
        this.numberOfFixes = numberOfFixes;
        this.windSourceTypeNames = windSourceTypeNames;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        sailingService.getWindInfo(raceIdentifier, from, millisecondsStepWidth, numberOfFixes, windSourceTypeNames, (AsyncCallback<WindInfoForRaceDTO>) wrapperCallback);
    }

    @Override
    public WindInfoForRaceDTO getResult() {
        return result;
    }

    @Override
    public AsyncCallback<WindInfoForRaceDTO> getCallback() {
        return callback;
    }

    @Override
    public void setCallback(AsyncCallback<WindInfoForRaceDTO> callback) {
        this.callback = callback;
    }
    
    @Override
    public String getName() {
        return GetWindInfoAction.class.getName();
    }
}