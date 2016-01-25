package com.sap.sailing.dashboards.gwt.client.widgets.startanalysis;

import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.ui.client.shared.racemap.CombinedWindPanelStyle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapResources;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface DashboardRaceMapResources extends RaceMapResources {

    @Source("com/sap/sailing/dashboards/gwt/client/images/windarrow.png")
    ImageResource combinedWindIcon();
    
    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "StartAnalysisCombinedWindPanel.gss"})
    CombinedWindPanelStyle combinedWindPanelStyle();
}
