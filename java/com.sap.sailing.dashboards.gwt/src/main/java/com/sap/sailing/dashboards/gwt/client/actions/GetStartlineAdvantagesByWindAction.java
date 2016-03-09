package com.sap.sailing.dashboards.gwt.client.actions;

/**
 * @author Alexander Ries (D062114)
 *
 */

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.dashboards.gwt.server.util.actions.startlineadvantage.StartlineAdvantagesByWindCalculator;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dispatch.RequiresLiveRaceAndCachesMovingAverageAction;
import com.sap.sailing.dashboards.gwt.shared.dto.StartlineAdvantagesWithMaxAndAverageDTO;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

public class GetStartlineAdvantagesByWindAction extends RequiresLiveRaceAndCachesMovingAverageAction<StartlineAdvantagesWithMaxAndAverageDTO> {

    public GetStartlineAdvantagesByWindAction() {}
    
    public GetStartlineAdvantagesByWindAction(String leaderboardName) {
        super(leaderboardName);
    }
    
    private static final String MOVING_AVERAGE_CACHE_KEY = UUID.randomUUID().toString();
    
    @Override
    @GwtIncompatible
    public StartlineAdvantagesWithMaxAndAverageDTO execute(DashboardDispatchContext dashboardDispatchContext) throws DispatchException {
        StartlineAdvantagesWithMaxAndAverageDTO result = new StartlineAdvantagesWithMaxAndAverageDTO();
        TrackedRace liveRace = super.getLiveRace(dashboardDispatchContext);
        if (liveRace != null) {
            final DomainFactory domainFactory = dashboardDispatchContext.getRacingEventService().getBaseDomainFactory();
            StartlineAdvantagesByWindCalculator startlineAdvantagesByWindCalculator = new StartlineAdvantagesByWindCalculator(dashboardDispatchContext, domainFactory);
            result = startlineAdvantagesByWindCalculator.getStartLineAdvantagesAccrossLineFromTrackedRaceAtTimePoint(liveRace, MillisecondsTimePoint.now());
        }
        if(result != null && result.maximum != null) {
            super.addValueToMovingAverage(result.maximum, dashboardDispatchContext.getMovingAveragesCache());
            result.average = dashboardDispatchContext.getMovingAveragesCache().getValueForKey(uniqueMovingAverageCacheKey());
        }
        return result;
    }
    
    @Override
    protected String uniqueMovingAverageCacheKey() {
        return MOVING_AVERAGE_CACHE_KEY;
    }
}