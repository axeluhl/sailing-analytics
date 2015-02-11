package com.sap.sailing.dashboards.gwt.client.startanalysis;

import java.util.List;

import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;

/**
 * classes implementing this interface get usually notified, if there is a new {@link StartAnalysisDTO} 
 * in {@link RibDashboardRaceInfoDTO} variable <code>startAnalysisDTOList</code>.
 * 
 * @author Alexander Ries (D062114)
 *
 */
public interface NewStartAnalysisListener {

    public void addNewStartAnalysisCardForCompetitor(List<StartAnalysisDTO> startAnalysisDTOs, String selectedCompetitor);
}
