package com.sap.sailing.gwt.ui.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;

public class GetPolarSheetDataByAngleAction extends DefaultAsyncAction<PolarSheetsHistogramData> {
    
    private final SailingServiceAsync sailingService;
    private final String polarSheetId;
    private final int angle;

    

    public GetPolarSheetDataByAngleAction(SailingServiceAsync sailingService,
            String polarSheetId, int angle, AsyncCallback<PolarSheetsHistogramData> callback) {
        super(callback);
        this.sailingService = sailingService;
        this.polarSheetId = polarSheetId;
        this.angle = angle;
    }



    @Override
    public void execute(AsyncActionsExecutor asyncActionsExecutor) {
        sailingService.getPolarSheetData(polarSheetId, angle, (AsyncCallback<PolarSheetsHistogramData>) getWrapperCallback(asyncActionsExecutor));
    }

}
