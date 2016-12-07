package com.sap.sailing.dashboards.gwt.client;

import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchSystem;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.client.player.Timer;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface DashboardClientFactory{
    
    DashboardDispatchSystem getDispatch();
    
    SailingServiceAsync getSailingService();
    
    Timer getDashboardFiveSecondsTimer();
}
