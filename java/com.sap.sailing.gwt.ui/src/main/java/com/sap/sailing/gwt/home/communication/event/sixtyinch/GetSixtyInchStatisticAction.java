package com.sap.sailing.gwt.home.communication.event.sixtyinch;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.TargetTimeInfo;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sse.common.Duration;
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
public class GetSixtyInchStatisticAction implements SailingAction<GetSixtyInchStatisticDTO> {
    // transfer as string, to avoid transfering incompatible types
    private String racename;
    private String regattaname;

    @SuppressWarnings("unused")
    private GetSixtyInchStatisticAction() {
    }

    public GetSixtyInchStatisticAction(String racename, String regattaname) {
        this.racename = racename;
        this.regattaname = regattaname;
    }

    @Override
    @GwtIncompatible
    public GetSixtyInchStatisticDTO execute(SailingDispatchContext context) {
        int competitors = 0;
        int legs = 0;
        RegattaNameAndRaceName identifier = new RegattaNameAndRaceName(regattaname, racename);
        RaceDefinition race = context.getRacingEventService().getRace(identifier);
        for (Competitor c : race.getCompetitors()) {
            competitors++;
        }
        legs = race.getCourse().getLegs().size();
        DynamicTrackedRace trace = context.getRacingEventService().getTrackedRace(identifier);


        Duration duration = null;
        try {
            TargetTimeInfo timeToComplete = trace.getEstimatedTimeToComplete(MillisecondsTimePoint.now());
            duration = timeToComplete.getExpectedDuration();

            trace.getEstimatedDistanceToComplete(MillisecondsTimePoint.now());
        } catch (NotEnoughDataHasBeenAddedException e) {
            e.printStackTrace();
        } catch (NoWindException e) {
            e.printStackTrace();
        }

        Distance distance = null;
        try {
            TargetTimeInfo timeToComplete = trace.getEstimatedTimeToComplete(MillisecondsTimePoint.now());
            distance = timeToComplete.getExpectedDistance();

            trace.getEstimatedDistanceToComplete(MillisecondsTimePoint.now());
        } catch (NotEnoughDataHasBeenAddedException e) {
            e.printStackTrace();
        } catch (NoWindException e) {
            e.printStackTrace();
        }

        return new GetSixtyInchStatisticDTO(competitors, legs, duration, distance);
    }
}
