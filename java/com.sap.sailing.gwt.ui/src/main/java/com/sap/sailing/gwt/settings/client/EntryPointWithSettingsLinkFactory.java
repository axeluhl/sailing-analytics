package com.sap.sailing.gwt.settings.client;

import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaOverviewBaseSettings;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaRaceStatesSettings;
import com.sap.sse.gwt.settings.AbstractEntryPointWithSettingsLinkFactory;

public class EntryPointWithSettingsLinkFactory extends AbstractEntryPointWithSettingsLinkFactory {

    public static String createRegattaOverviewLink(RegattaOverviewBaseSettings regattaOverviewSettings) {
        return createRegattaOverviewLink(regattaOverviewSettings, new RegattaRaceStatesSettings());
    }

    public static String createRegattaOverviewLink(RegattaOverviewBaseSettings regattaOverviewSettings,
            RegattaRaceStatesSettings regattaRaceStatesSettings) {
        return createEntryPointLink("/gwt/RegattaOverview.html", regattaOverviewSettings, regattaRaceStatesSettings);
    }

}
