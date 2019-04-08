package com.sap.sailing.gwt.home.communication.event.sixtyinch;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.TargetTimeInfo;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown in compact leaderboard (e.g. on the series overview
 * page) for the {@link #GetMiniOverallLeaderbordAction(UUID) given series-id}, where the amount of loaded entries can
 * optionally be {@link #GetMiniOverallLeaderbordAction(UUID, int) limited}.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live is <i>1 minute</i> for currently running events, <i>2 minutes</i>
 * otherwise.
 * </p>
 */
public class GetSixtyInchStatisticAction implements SailingAction<GetSixtyInchStatisticDTO>, ProvidesLeaderboardRouting {
    private static final Logger LOGGER = Logger.getLogger(GetSixtyInchStatisticAction.class.getName());
    // transfer as string, to avoid transfering incompatible types
    private String racename;
    private String regattaname;

    public GetSixtyInchStatisticAction() {
    }

    public GetSixtyInchStatisticAction(String racename, String regattaname) {
        this.racename = racename;
        this.regattaname = regattaname;
    }

    @Override
    @GwtIncompatible
    public GetSixtyInchStatisticDTO execute(SailingDispatchContext context) {
        int legs = 0;
        RegattaNameAndRaceName identifier = new RegattaNameAndRaceName(regattaname, racename);
        RaceDefinition race = context.getRacingEventService().getRace(identifier);
        int competitors = com.sap.sse.common.Util.size(race.getCompetitors());
        legs = race.getCourse().getLegs().size();
        DynamicTrackedRace trace = context.getRacingEventService().getTrackedRace(identifier);

        Duration duration = null;
        Distance distance = null;
        TimePoint timePoint;
        if(trace.getEndOfRace() == null){
            timePoint = MillisecondsTimePoint.now();
            duration = estimateDuration(trace, duration,timePoint);
            distance = estimateDistance(trace, distance,timePoint);
        }else{
            List<Competitor> competitorOrder = trace.getCompetitorsFromBestToWorst(trace.getEndOfRace());
            if(!competitorOrder.isEmpty()){
                distance = trace.getDistanceTraveled(competitorOrder.get(0), trace.getEndOfRace());
                duration = trace.getStartOfRace().until(trace.getEndOfRace());
            }
        }

        return new GetSixtyInchStatisticDTO(competitors, legs, duration, distance);
    }

    @GwtIncompatible
    private Distance estimateDistance(DynamicTrackedRace trace, Distance distance,TimePoint timePoint) {
        try {
            TargetTimeInfo timeToComplete = trace.getEstimatedTimeToComplete(timePoint);
            distance = timeToComplete.getExpectedDistance();
        } catch (NoWindException | NotEnoughDataHasBeenAddedException e) {
            LOGGER.log(Level.WARNING, "Could not estimate Distance" , e);
        }
        return distance;
    }

    @GwtIncompatible
    private Duration estimateDuration(DynamicTrackedRace trace, Duration duration, TimePoint timePoint) {
        try {
            TargetTimeInfo timeToComplete = trace.getEstimatedTimeToComplete(timePoint);
            duration = timeToComplete.getExpectedDuration();
        } catch (NotEnoughDataHasBeenAddedException | NoWindException e) {
            LOGGER.log(Level.WARNING, "Could not estimate duration" , e);
        }
        return duration;
    }

    @Override
    public String getLeaderboardName() {
        return regattaname;
    }
}
