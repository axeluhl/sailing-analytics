/**
 * 
 */
package com.sap.sailing.dashboards.gwt.client.dataretriever;

import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;

/**
 * 
 * @author Alexander Ries
 *
 */
public interface RibDashboardDataRetrieverListener {

    public void updateUIWithNewLiveRaceInfo(RibDashboardRaceInfoDTO liveRaceInfoDTO);
}
