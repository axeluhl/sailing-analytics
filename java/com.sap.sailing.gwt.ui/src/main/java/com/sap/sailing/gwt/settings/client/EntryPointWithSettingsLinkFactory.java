package com.sap.sailing.gwt.settings.client;

import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextSettings;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaOverviewBaseSettings;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaRaceStatesSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sse.gwt.client.shared.components.LinkWithSettingsGenerator;
import com.sap.sse.gwt.client.shared.perspective.IgnoreLocalSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.settings.AbstractEntryPointWithSettingsLinkFactory;

public class EntryPointWithSettingsLinkFactory extends AbstractEntryPointWithSettingsLinkFactory {

    public static String createRegattaOverviewLink(RegattaOverviewBaseSettings regattaOverviewSettings) {
        return createRegattaOverviewLink(regattaOverviewSettings, new RegattaRaceStatesSettings());
    }

    public static String createRegattaOverviewLink(RegattaOverviewBaseSettings regattaOverviewSettings,
            RegattaRaceStatesSettings regattaRaceStatesSettings) {
        return new LinkWithSettingsGenerator<>("/gwt/RegattaOverview.html", regattaOverviewSettings,
                new IgnoreLocalSettings(true)).createUrl(regattaRaceStatesSettings);
    }

    public static String createRaceBoardLink(RaceboardContextSettings ctx,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings) {
        return new LinkWithSettingsGenerator<>("/gwt/RaceBoard.html", ctx).createUrl(settings);
    }

}
