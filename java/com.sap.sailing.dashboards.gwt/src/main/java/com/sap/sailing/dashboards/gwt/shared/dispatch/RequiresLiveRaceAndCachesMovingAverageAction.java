package com.sap.sailing.dashboards.gwt.shared.dispatch;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.dashboards.gwt.shared.MovingAveragesCache;
import com.sap.sailing.gwt.dispatch.client.Result;

/**
 * @author Alexander Ries (D062114)
 *
 */
public abstract class RequiresLiveRaceAndCachesMovingAverageAction<R extends Result> extends RequiresLiveRaceAction<R>{

    private static final int MOVING_AVERAGE_CACHE_SIZE = (60/5)*15;/*15 minutes in seconds divided by request intervall*/
    
    public RequiresLiveRaceAndCachesMovingAverageAction() {}
    
    public RequiresLiveRaceAndCachesMovingAverageAction(String leaderboardName) {
       super(leaderboardName);
    }
    
    @GwtIncompatible
    protected void addValueToMovingAverage(double value, MovingAveragesCache movingAveragesCache) {
        if(!movingAveragesCache.containesCacheWithKey(getKeyForMovingAverage())) {
            movingAveragesCache.createMovingAverageWithKeyAndSize(getKeyForMovingAverage(), MOVING_AVERAGE_CACHE_SIZE);
        }
        movingAveragesCache.addValueToAverageWithKey(getKeyForMovingAverage(), value);
    }
    
    protected abstract String getKeyForMovingAverage();
}
