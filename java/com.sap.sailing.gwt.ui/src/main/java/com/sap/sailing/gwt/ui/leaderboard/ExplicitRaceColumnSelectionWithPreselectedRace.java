package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;

/**
 * A variant of {@link ExplicitRaceColumnSelection} which has a pre-selected race. As opposed to the base class,
 * a new leaderboard column is added implicitly only if it is linked to the pre-selected race.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ExplicitRaceColumnSelectionWithPreselectedRace extends ExplicitRaceColumnSelection {
    private final RaceIdentifier preSelectedRace;
    
    /**
     * @param preSelectedRace must be non-<code>null</code>. Otherwise, you may use the superclass instead.
     */
    public ExplicitRaceColumnSelectionWithPreselectedRace(RaceIdentifier preSelectedRace) {
        super();
        assert preSelectedRace != null;
        this.preSelectedRace = preSelectedRace;
    }

    @Override
    protected List<RaceColumnDTO> getRaceColumnsToAddImplicitly(LeaderboardDTO newLeaderboard,
            LeaderboardDTO oldLeaderboard) {
        List<RaceColumnDTO> columnsToAddImplicitly = super.getRaceColumnsToAddImplicitly(newLeaderboard, oldLeaderboard);
        if (preSelectedRace != null) {
            for (Iterator<RaceColumnDTO> i = columnsToAddImplicitly.iterator(); i.hasNext();) {
                RaceColumnDTO next = i.next();
                if (!next.containsRace(preSelectedRace)) {
                    i.remove();
                }
            }
        }
        return columnsToAddImplicitly;
    }
}
