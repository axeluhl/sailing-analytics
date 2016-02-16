package com.sap.sailing.dashboards.gwt.shared.dto;

import java.util.List;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class LeaderboardCompetitorsDTO implements Result {

    private List<CompetitorDTO> competitors;

    public LeaderboardCompetitorsDTO() {}

    public List<CompetitorDTO> getCompetitors() {
        return competitors;
    }

    public void setCompetitors(List<CompetitorDTO> competitors) {
        this.competitors = competitors;
    }
}
