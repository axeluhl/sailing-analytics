package com.sap.sailing.domain.leaderboard;

import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sse.common.Util.Pair;

/**
 * Describes the result of
 * {@link Leaderboard#mapRegattaScoreCorrections(com.sap.sailing.domain.common.RegattaScoreCorrections, java.util.Map, java.util.Map, boolean)
 * mapping} a {@link RegattaScoreCorrections} object to an existing {@link Leaderboard}. For each
 * {@link RegattaScoreCorrections#getScoreCorrectionsForRaces() race} for which there are score corrections, the mapping of the
 * {@link ScoreCorrectionsForRace#getRaceNameOrNumber() race name or number} to a {@link RaceColumn} in the leaderboard is captured.
 * If no matching race column was assigned, that race name/number is mapped to {@code null} instead. Likewise, for all sail IDs/numbers
 * (that may alternatively be interpreted as competitor short names if the boat assignments can change per race in the leaderboard)
 * the matching {@link Competitor} is recorded where a {@code null} value again means that for the key sail ID/number/shortname no
 * matching competitor was identified.<p>
 * 
 * All {@link ScoreCorrectionForCompetitorInRace} from the original {@link RegattaScoreCorrections} object that were mapped
 * successfully to a {@link RaceColumn} and a {@link Competitor} can be obtained using the {@link #getScoreCorrections} method.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ScoreCorrectionMapping {
    Map<String, RaceColumn> getRaceMappings();

    Map<String, Competitor> getCompetitorMappings();

    Map<RaceColumn, Map<Competitor, Pair<Double, MaxPointsReason>>> getScoreCorrections();
}
