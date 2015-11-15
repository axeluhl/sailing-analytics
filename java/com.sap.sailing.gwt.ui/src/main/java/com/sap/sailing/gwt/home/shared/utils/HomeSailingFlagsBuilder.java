package com.sap.sailing.gwt.home.shared.utils;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.sap.sailing.gwt.home.communication.race.FlagStateDTO;
import com.sap.sailing.gwt.regattaoverview.client.SailingFlagsBuilder;

public class HomeSailingFlagsBuilder {
    
    public static SafeHtml render(FlagStateDTO flagState, double scale, String tooltip) {
        return SailingFlagsBuilder.render(flagState.getLastUpperFlag(), flagState.getLastLowerFlag(), 
                flagState.isLastFlagsAreDisplayed(), flagState.isLastFlagsDisplayedStateChanged(), scale, tooltip);
    }

}
