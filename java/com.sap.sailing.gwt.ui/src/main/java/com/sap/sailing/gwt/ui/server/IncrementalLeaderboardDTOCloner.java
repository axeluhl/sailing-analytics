package com.sap.sailing.gwt.ui.server;

import com.sap.sailing.domain.common.dto.IncrementalLeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sse.util.ClonerImpl;

/**
 * Uses reflection to clone all properties of a {@link LeaderboardDTO} into a new instance of type {@link IncrementalLeaderboardDTO}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class IncrementalLeaderboardDTOCloner {
    public IncrementalLeaderboardDTO clone(LeaderboardDTO leaderboardDTO) {
        IncrementalLeaderboardDTO result = new IncrementalLeaderboardDTO(leaderboardDTO.getId(), new ClonerImpl());
        new ClonerImpl().clone(leaderboardDTO, result);
        return result;
    }
}
