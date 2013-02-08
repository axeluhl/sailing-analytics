package com.sap.sailing.gwt.ui.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;

public class GetPolarSheetDataByAngleAction extends DefaultAsyncAction<PolarSheetsHistogramData> {
    
    private final SailingServiceAsync sailingService;
    private final String polarSheetId;
    private final int angle;
    private final int windSpeed;

    

    public GetPolarSheetDataByAngleAction(SailingServiceAsync sailingService,
            String polarSheetId, int angle, int windSpeed, AsyncCallback<PolarSheetsHistogramData> callback) {
        super(callback);
        this.sailingService = sailingService;
        this.polarSheetId = polarSheetId;
        this.angle = angle;
        this.windSpeed = windSpeed;
    }



    @Override
    public void execute(AsyncActionsExecutor asyncActionsExecutor) {
        sailingService.getPolarSheetData(polarSheetId, angle, windSpeed, (AsyncCallback<PolarSheetsHistogramData>) getWrapperCallback(asyncActionsExecutor));
    }

}
