package com.sap.sailing.dashboards.gwt.client.widgets;
import com.sap.sailing.dashboards.gwt.client.DashboardClientFactory;
import com.sap.sse.gwt.client.player.TimeListener;


/**
 * @author Alexander Ries (D062114)
 *
 */
public interface PollsLiveDataEvery5Seconds extends TimeListener {

    public void registerForDashboardFiveSecondsTimer(DashboardClientFactory dashboardClientFactory);
}
