package com.sap.sailing.gwt.ui.actions;

import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;

public class GetCoursePositionsAction extends DefaultAsyncAction<CoursePositionsDTO> {
    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final Date date;

    public GetCoursePositionsAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier, Date date,
            AsyncCallback<CoursePositionsDTO> callback) {
        super(callback);
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.date = date;
    }
    
    @Override
    public void execute(AsyncActionsExecutor asyncActionsExecutor) {
        sailingService.getCoursePositions(raceIdentifier, date, (AsyncCallback<CoursePositionsDTO>) getWrapperCallback(asyncActionsExecutor));
    }
}