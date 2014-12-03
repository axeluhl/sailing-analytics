package com.sap.sailing.dashboards.gwt.server;

import java.util.List;
import java.util.Map;

import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;


public interface StartAnalysisRacesStoreListener {

    public void startAnalyisisRacesChanged(Map<String , List<StartAnalysisDTO>> startAnalysisDTOs);
}
