package com.sap.sailing.domain.leaderboard.impl;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Supplier;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sse.common.Util;

/**
 * A special low-point scoring scheme that uses one or more medal series to eliminate some competitors
 * during each such medal series. Competitors may be promoted to later series based on their opening
 * series results. For example, the scheme may define that the winner of the opening series automatically
 * advances to the final medal series, and the second and third ranking competitors advance to the last-but-one
 * medal series already; all others have to race a medal race/series to qualify for the next series, and
 * some of those will be eliminated on the way.<p>
 * 
 * The number of competitors from the opening series that qualify to later medal series is provided as
 * an array to the {@link #LowPointWithEliminatingMedalSeriesWithPromotions(int[]) constructor}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class LowPointWithEliminatingMedalSeriesWithPromotions extends LowPoint {
    private static final long serialVersionUID = 7759999270911627798L;
    
    /**
     * The last index for this array refers to the last medal race; e.g., the "Grand Final" race
     * in an iQFOil regatta with the medal series consisting of a "Quarter Final," a "Semi Final"
     * and a "Grand Final". The last-but-one index in the example would then refer to the Semi Final,
     * and so on. The array may be empty, meaning that no competitor is promoted into any medal
     * race. The field always has to refer to a valid array.
     */
    private final int[] numberOfPromotedCompetitorsIntoLastMedalRaces;

    /**
     * 
     * @param numberOfPromotedCompetitorsIntoLastMedalRaces
     *            The last index for this array refers to the last medal series; e.g., the "Grand Final" race in an iQFOil
     *            regatta with three medal series ("Quarter Final," "Semi Final" and "Grand Final"). The
     *            last-but-one index in the example would then refer to the Semi Final series, and so on. The array may be
     *            empty, meaning that no competitor is promoted into any medal race. The field always has to refer to a
     *            valid array.
     */
    public LowPointWithEliminatingMedalSeriesWithPromotions(int[] numberOfPromotedCompetitorsIntoLastMedalRaces) {
        super();
        if (numberOfPromotedCompetitorsIntoLastMedalRaces == null) {
            throw new NullPointerException("array specifying number of promoted competitors must not be null");
        }
        this.numberOfPromotedCompetitorsIntoLastMedalRaces = numberOfPromotedCompetitorsIntoLastMedalRaces;
    }
    
    @Override
    public ScoringSchemeType getType() {
        return super.getType();
    }

    /**
     * Counts the number of competitors in a medal series that, without sailing in it, will be ranked better
     * than those that do sail in it. This is because these many competitors have already advanced to later
     * medal series based on their opening series rank.
     */
    public int getNumberOfCompetitorsBetterThanThoseSailingInSeries(Series medalSeries) {
        final List<? extends Series> allMedalSeries = Util.asList(Util.filter(medalSeries.getRegatta().getSeries(), series->series.isMedal()));
        int result = 0;
        int indexInNumberOfPromotedCompetitorsIntoLastMedalRaces = numberOfPromotedCompetitorsIntoLastMedalRaces.length-1;
        if (allMedalSeries.size() > 1) {
            for (final ListIterator<? extends Series> i=allMedalSeries.listIterator(allMedalSeries.size()); i.hasPrevious() && i.previous() != medalSeries; ) {
                result += numberOfPromotedCompetitorsIntoLastMedalRaces[indexInNumberOfPromotedCompetitorsIntoLastMedalRaces--];
            }
        }
        return result;
    }
    
    public int getNumberOfCompetitorsAdvancingFromOpeningSeriesToOrThroughSeries(Series medalSeries) {
        final List<? extends Series> allMedalSeries = Util.asList(Util.filter(medalSeries.getRegatta().getSeries(), series->series.isMedal()));
        int result = 0;
        if (numberOfPromotedCompetitorsIntoLastMedalRaces.length > 0) {
            int indexInNumberOfPromotedCompetitorsIntoLastMedalRaces = numberOfPromotedCompetitorsIntoLastMedalRaces.length;
            if (!allMedalSeries.isEmpty()) {
                result = numberOfPromotedCompetitorsIntoLastMedalRaces[--indexInNumberOfPromotedCompetitorsIntoLastMedalRaces];
                for (final ListIterator<? extends Series> i=allMedalSeries.listIterator(allMedalSeries.size());
                     indexInNumberOfPromotedCompetitorsIntoLastMedalRaces > 0 && i.hasPrevious() && i.previous() != medalSeries; ) {
                    result += numberOfPromotedCompetitorsIntoLastMedalRaces[--indexInNumberOfPromotedCompetitorsIntoLastMedalRaces];
                }
            }
        }
        return result;
    }
    
    /**
     * In addition to the default implementation (assumed to check for a non-{@code null} medal race score}, this
     * specialized implementation is aware of the promotion scheme and defines competitors as "participants" of the
     * medal race represented by {@code medalRaceColumn} if their opening series rank was
     * {@link #getNumberOfCompetitorsBetterThanThoseSailingInSeries(RaceColumnInSeries) good enough} to have been promoted
     * to {@code medalRaceColumn}'s or a later medal series already. In this case it is not necessary for the competitor
     * to have <em>scored</em> in {@code medalRaceColumn} yet.<p>
     * 
     * Note: Since the promotion decision is made based on the opening series rank, promotion is considered even if
     * more opening series races are to come. This, however, will not change the results because the promotion scheme
     * will keep the rank ordering among those promoted.
     */
    @Override
    public boolean isParticipatingInMedalRace(Competitor competitor, Double competitorMedalRaceScore,
            RaceColumnInSeries medalRaceColumn, Supplier<Map<Competitor, Integer>> competitorsRankedByOpeningSeries) {
        final Integer openingSeriesRank;
        return super.isParticipatingInMedalRace(competitor, competitorMedalRaceScore, medalRaceColumn, competitorsRankedByOpeningSeries) ||
                ((openingSeriesRank = competitorsRankedByOpeningSeries.get().get(competitor)) != null &&
                 openingSeriesRank <= getNumberOfCompetitorsAdvancingFromOpeningSeriesToOrThroughSeries(medalRaceColumn.getSeries()));
    }
}
