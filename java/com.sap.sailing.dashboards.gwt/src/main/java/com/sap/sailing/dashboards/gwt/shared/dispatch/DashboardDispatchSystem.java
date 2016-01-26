package com.sap.sailing.dashboards.gwt.shared.dispatch;

import com.sap.sse.gwt.dispatch.client.system.DispatchSystemAsync;
import com.sap.sse.gwt.dispatch.client.system.ProvidesServerTime;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface DashboardDispatchSystem extends DispatchSystemAsync<DashboardDispatchContext>, ProvidesServerTime {

}
