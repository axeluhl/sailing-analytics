/**
 * 
 */
package com.sap.sailing.dashboards.gwt.shared.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sap.sailing.dashboards.gwt.shared.ResponseMessage;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

/**
 * @author Alexander Ries
 *
 */
public class RibDashboardRaceInfoDTO implements Serializable{

    private static final long serialVersionUID = -1325150193180234561L;
    
    public Map<String, WindBotComponentDTO> windBotDTOForID;
    public StartLineAdvantageDTO startLineAdvantageDTO;
    public List<StartAnalysisDTO> startAnalysisDTOList;
    public List<String> competitorNamesFromLastTrackedRace;
    public RegattaAndRaceIdentifier idOfLastTrackedRace;
    public ResponseMessage responseMessage;
    
    public RibDashboardRaceInfoDTO() {}
  
}
