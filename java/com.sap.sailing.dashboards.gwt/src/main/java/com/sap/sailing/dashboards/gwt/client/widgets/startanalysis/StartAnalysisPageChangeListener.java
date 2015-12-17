package com.sap.sailing.dashboards.gwt.client.widgets.startanalysis;

import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;

public interface StartAnalysisPageChangeListener {

    public void startAnalysisComponentPageChangedToIndexAndStartAnalysis(int newPageIndex, StartAnalysisDTO startAnalysisDTO);
    
}
