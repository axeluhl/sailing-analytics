package com.sap.sailing.gwt.ui.actions;

import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;
import com.sap.sse.gwt.client.async.AsyncAction;

public class GetCoursePositionsAction implements AsyncAction<CoursePositionsDTO> {
    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final Date date;

    public GetCoursePositionsAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier, Date date) {
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.date = date;
    }
    
    @Override
    public void execute(AsyncCallback<CoursePositionsDTO> callback) {
        sailingService.getCoursePositions(raceIdentifier, date, callback);
    }
}