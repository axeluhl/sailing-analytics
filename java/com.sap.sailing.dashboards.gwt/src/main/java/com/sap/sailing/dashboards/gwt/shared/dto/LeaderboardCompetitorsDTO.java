package com.sap.sailing.dashboards.gwt.shared.dto;

import java.util.List;

import com.sap.sailing.domain.common.dto.CompetitorWithoutBoatDTO;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class LeaderboardCompetitorsDTO implements Result {

    private List<CompetitorWithoutBoatDTO> competitors;

    public LeaderboardCompetitorsDTO() {}

    public List<CompetitorWithoutBoatDTO> getCompetitors() {
        return competitors;
    }

    public void setCompetitors(List<CompetitorWithoutBoatDTO> competitors) {
        this.competitors = competitors;
    }
}
