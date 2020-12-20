package com.sap.sailing.gwt.ui.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.BearingWithConfidenceDTO;
import com.sap.sse.common.Speed;
import com.sap.sse.gwt.client.async.AsyncAction;

public class GetManeuverAngleAction implements AsyncAction<BearingWithConfidenceDTO> {
    private final SailingServiceAsync sailingService;
    private final BoatClass boatClass;
    private final ManeuverType maneuverType;
    private final Speed windSpeed;

    public GetManeuverAngleAction(SailingServiceAsync sailingService, BoatClass boatClass, ManeuverType maneuverType,
            Speed windSpeed) {
        this.sailingService = sailingService;
        this.boatClass = boatClass;
        this.maneuverType = maneuverType;
        this.windSpeed = windSpeed;
    }

    @Override
    public void execute(AsyncCallback<BearingWithConfidenceDTO> callback) {
        //sailingService.getManeuverAngle(boatClass, maneuverType, windSpeed, callback);
    }
}
