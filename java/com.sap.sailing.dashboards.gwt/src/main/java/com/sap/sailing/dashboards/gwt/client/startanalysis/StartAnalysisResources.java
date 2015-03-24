package com.sap.sailing.dashboards.gwt.client.startanalysis;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.ui.client.shared.racemap.CombinedWindPanelStyle;

interface StartAnalysisResources extends ClientBundle {

    @Source("com/sap/sailing/dashboards/gwt/client/images/windarrow.png")
    ImageResource combinedWindIcon();
    
    @Source("StartAnalysisCombinedWindPanel.css")
    CombinedWindPanelStyle combinedWindPanelStyle();
}