package com.sap.sailing.dashboards.gwt.shared.dto;

import java.util.List;

import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.gwt.dispatch.client.Result;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class StartAnalysesDTO implements Result {

    private List<StartAnalysisDTO> startAnalyses;

    public StartAnalysesDTO() {}

    public List<StartAnalysisDTO> getStartAnalyses() {
        return startAnalyses;
    }

    public void setStartAnalyses(List<StartAnalysisDTO> startAnalyses) {
        this.startAnalyses = startAnalyses;
    }
}
