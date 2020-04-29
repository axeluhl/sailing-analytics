package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.Map;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sailing.gwt.ui.shared.RaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;

/**
 * The {@link QuickRankDTO} objects stored in the {@link RaceMap}'s {@code quickRanks} field may be obtained in
 * different ways. This interface abstracts the different strategies for how this may happen. If a {@link RaceMap} is
 * used in the context of a {@link RaceBoardPanel} which is a {@link LeaderboardUpdateListener}, {@link LeaderboardDTO}
 * objects can provide the up-to-date information for all aspects of the {@link QuickRankDTO} objects required.
 * Otherwise, the {@link QuickRankDTO} objects as received in the {@link RaceMapDataDTO} objects can be used. This may
 * not always be consistent and in sync with the leaderboard being shown for a particular time point because their
 * calculation in the back-end happens in a lower-priority thread, hence may be a bit delayed in a server under high
 * load. See also bug 4175.
 * The speedInKnots field values of {@link SpeedWithBearingDTO} objects are used to provide speed delivered from
 * server.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface QuickFlagDataProvider {
    public interface QuickFlagDataListener {
        void rankChanged(String competitorIdAsString, QuickRankDTO oldQuickRank, QuickRankDTO quickRanks);

        void speedChanged(CompetitorDTO competitor, Double quickSpeed);
    }

    void addQuickFlagDataListener(QuickFlagDataListener listener);

    void removeQuickFlagDataListener(QuickFlagDataListener listener);

    /**
     * When switching the underlying provider, this method allows to move current listeners to the new provider.
     * 
     * @param newProvider
     */
    public void moveListernersTo(QuickFlagDataProvider newProvider);

    /**
     * The strategy may or may not use this quick ranks information provided directly from the server. This information
     * may have been provided with some delay and may be slightly inconsistent with a {@link LeaderboardDTO leaderboard}
     * that was delivered through a different channel. Therefore, a strategy that has access to a current
     * {@link LeaderboardDTO leaderboard} should ignore this call if it already has quick ranks information available
     * from a leaderboard.
     * 
     * @param quickRanksFromServer
     *            keys are the competitor IDs as strings
     */
    void quickRanksReceivedFromServer(Map<String, QuickRankDTO> quickRanksFromServer);

    /**
     * The strategy may or may not use this quick speeds information provided directly from the server. This information
     * may have been provided with some delay and may be slightly inconsistent with a {@link LeaderboardDTO leaderboard}
     * that was delivered through a different channel. Therefore, a strategy that has access to a current
     * {@link LeaderboardDTO leaderboard} should ignore this call if it already has quick ranks information available
     * from a leaderboard.
     * 
     * @param quickSpeedsFromServer
     *            keys are the competitors, values are current speeds of competitors
     */
    void quickSpeedsReceivedFromServer(Map<CompetitorDTO, Double> quickSpeedsFromServer);

    /**
     * @return keys are the {@link CompetitorWithBoatDTO#getIdAsString() competitor IDs are string}, values are the
     *         quick ranks pertaining to the competitors whose IDs are provided as keys
     */
    Map<String, QuickRankDTO> getQuickRanks();

    /**
     * @return keys are the {@link CompetitorWithBoatDTO}, values are the quick speeds pertaining to the competitors
     */
    Map<CompetitorDTO, Double> getQuickSpeeds();
}
