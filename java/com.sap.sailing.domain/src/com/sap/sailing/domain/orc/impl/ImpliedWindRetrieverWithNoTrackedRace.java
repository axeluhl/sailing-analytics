package com.sap.sailing.domain.orc.impl;

import com.sap.sailing.domain.abstractlog.orc.RaceLogORCImpliedWindSourceFinder;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.impl.SimpleRaceLogIdentifierImpl;
import com.sap.sailing.domain.common.orc.FixedSpeedImpliedWind;
import com.sap.sailing.domain.common.orc.ImpliedWindSource;
import com.sap.sailing.domain.common.orc.ImpliedWindSourceVisitor;
import com.sap.sailing.domain.common.orc.OtherRaceAsImpliedWindSource;
import com.sap.sailing.domain.common.orc.OwnMaxImpliedWind;
import com.sap.sailing.domain.racelog.RaceLogAndTrackedRaceResolver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

/**
 * Retrieves "reference implied wind" data based on an {@link ImpliedWindSource} by visiting the implied wind source
 * object and either finding a fixed value for the implied wind, or a reference to another race slot which is then
 * followed. If that race slot has a {@link TrackedRace} attached (see
 * {@link RaceLogAndTrackedRaceResolver#resolveTrackedRace(SimpleRaceLogIdentifier)}), the tracked race is asked for its
 * {@link TrackedRace#getReferenceImpliedWind(TimePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache) reference
 * implied wind}. Otherwise, the {@link RaceLog} for the slot is resolved and searched for a hint regarding implied wind
 * using another instance of this retriever type again.
 * <p>
 * 
 * If the {@link ImpliedWindSource} requests a maximum implied wind to be calculated from an
 * {@link ORCPerformanceCurveByImpliedWindRankingMetric} or anything more specific, {@code null} is returned because we
 * don't have a {@link TrackedRace} and therefore no {@link RankingMetric} to ask.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ImpliedWindRetrieverWithNoTrackedRace implements ImpliedWindSourceVisitor<Speed> {
    private final TimePoint timePoint;
    private final WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache;
    private final RaceLogAndTrackedRaceResolver raceLogResolver;
    
    public ImpliedWindRetrieverWithNoTrackedRace(TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache,
            RaceLogAndTrackedRaceResolver raceLogResolver) {
        super();
        this.timePoint = timePoint;
        this.cache = cache;
        this.raceLogResolver = raceLogResolver;
    }

    /**
     * The fixed wind speed from the implied wind source event is returned unchanged.
     */
    @Override
    public Speed visit(FixedSpeedImpliedWind impliedWindSource) {
        return impliedWindSource.getFixedImpliedWindSpeed();
    }

    /**
     * The implied wind to use comes from another race, identified by the triple of
     * leaderboard name, race column name and fleet name. This uniquely identifies
     * a {@link RaceLog}, and there may be a {@link TrackedRace} attached to the slot
     * identified this way. In case a {@link TrackedRace} is found in that slot, it is
     * asked for its {@link TrackedRace#getReferenceImpliedWind reference implied wind} which
     * it is expected to delegate to its ranking metric. If no {@link TrackedRace} is found
     * in the slot, still that other {@link RaceLog} may contain a definition of an
     * implied wind source for its race which can be evaluated, unless it requests using
     * the own maximum implied wind which without a tracked race cannot be determined in which
     * case {@code null} will be returned.
     */
    @Override
    public Speed visit(OtherRaceAsImpliedWindSource impliedWindSource) {
        final SimpleRaceLogIdentifier raceLogIdentifier = new SimpleRaceLogIdentifierImpl(
                impliedWindSource.getLeaderboardAndRaceColumnAndFleetOfDefiningRace().getA(),
                impliedWindSource.getLeaderboardAndRaceColumnAndFleetOfDefiningRace().getB(),
                impliedWindSource.getLeaderboardAndRaceColumnAndFleetOfDefiningRace().getC());
        final TrackedRace otherTrackedRace = raceLogResolver.resolveTrackedRace(raceLogIdentifier);
        final Speed result;
        if (otherTrackedRace != null) {
            result = otherTrackedRace.getReferenceImpliedWind(timePoint, cache);
        } else {
            // check race log:
            final RaceLog raceLog = raceLogResolver.resolve(raceLogIdentifier);
            if (raceLog != null) {
                final ImpliedWindSource otherRaceImpliedWindSource = new RaceLogORCImpliedWindSourceFinder(raceLog).analyze();
                if (otherRaceImpliedWindSource != null) {
                    result = otherRaceImpliedWindSource.accept(new ImpliedWindRetrieverWithNoTrackedRace(timePoint, cache, raceLogResolver));
                } else {
                    result = null;
                }
            } else {
                result = null;
            }
        }
        return result;
    }

    /**
     * The default strategy: use the maximum implied wind obtained for all race competitors' progress at
     * {@code timePoint}.
     */
    @Override
    public Speed visit(OwnMaxImpliedWind impliedWindSource) {
        return null;
    }
}
