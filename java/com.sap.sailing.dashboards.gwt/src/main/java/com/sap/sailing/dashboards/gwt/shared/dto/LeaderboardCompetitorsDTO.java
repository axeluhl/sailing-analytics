package com.sap.sailing.dashboards.gwt.shared.dto;

import java.util.List;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class LeaderboardCompetitorsDTO implements Result {

    private List<CompetitorWithBoatDTO> competitors;

    public LeaderboardCompetitorsDTO() {}

    public List<CompetitorWithBoatDTO> getCompetitors() {
        return competitors;
    }

    public void setCompetitors(List<CompetitorWithBoatDTO> competitors) {
        this.competitors = competitors;
    }
}
