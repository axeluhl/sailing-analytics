package com.sap.sailing.gwt.settings.client;

import java.util.Collections;

import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardContextDefinition;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaOverviewContextDefinition;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaRaceStatesSettings;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.LinkWithSettingsGenerator;
import com.sap.sse.gwt.client.shared.perspective.IgnoreLocalSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.settings.AbstractEntryPointWithSettingsLinkFactory;

/**
 * Creates a link to a Entrypoint, supports passing Context and Settings in a type save manner, and is responsible to
 * create the url correctly.
 *
 */
public class EntryPointWithSettingsLinkFactory extends AbstractEntryPointWithSettingsLinkFactory {

    public static String createRegattaOverviewLink(RegattaOverviewContextDefinition regattaOverviewSettings) {
        return createRegattaOverviewLink(regattaOverviewSettings, new RegattaRaceStatesSettings());
    }

    public static String createRegattaOverviewLink(RegattaOverviewContextDefinition regattaOverviewSettings,
            RegattaRaceStatesSettings regattaRaceStatesSettings) {
        return new LinkWithSettingsGenerator<>("/gwt/RegattaOverview.html", regattaOverviewSettings,
                new IgnoreLocalSettings(true)).createUrl(regattaRaceStatesSettings);
    }
    
    public static String createLeaderboardLink(LeaderboardContextDefinition contextDefinition, LeaderboardPerspectiveOwnSettings perspectiveOwnSettings) {
        final LinkWithSettingsGenerator<Settings> linkWithSettingsGenerator = new LinkWithSettingsGenerator<>(
                EntryPointLinkFactory.LEADERBOARD_PATH,
                contextDefinition);
        final PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> settings = new PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>(
                perspectiveOwnSettings, Collections.emptyMap());
        return linkWithSettingsGenerator.createUrl(settings);
    }

    public static String createRaceBoardLink(RaceboardContextDefinition ctx,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings) {
        return new LinkWithSettingsGenerator<>("/gwt/RaceBoard.html", ctx).createUrl(settings);
    }

}
