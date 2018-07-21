package com.sap.sailing.gwt.settings.client;

import java.util.Collections;
import java.util.UUID;

import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardContextDefinition;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.leaderboardedit.LeaderboardEditContextDefinition;
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

    private static final String LEADERBOARD_EDITING_PATH = "/gwt/LeaderboardEditing.html";
    private static final String RACE_BOARD_PATH = "/gwt/RaceBoard.html";
    private static final String REGATTA_OVERVIEW_PATH = "/gwt/RegattaOverview.html";

    public static String createRegattaOverviewLink(RegattaOverviewContextDefinition regattaOverviewSettings) {
        return createRegattaOverviewLink(regattaOverviewSettings, new RegattaRaceStatesSettings(), true);
    }

    public static String createRegattaOverviewLink(RegattaOverviewContextDefinition regattaOverviewSettings,
            RegattaRaceStatesSettings regattaRaceStatesSettings, boolean ignoreLocalSettings) {
        return new LinkWithSettingsGenerator<>(REGATTA_OVERVIEW_PATH, regattaOverviewSettings,
                new IgnoreLocalSettings(ignoreLocalSettings)).createUrl(regattaRaceStatesSettings);
    }
    
    public static String createLeaderboardLink(LeaderboardContextDefinition contextDefinition, LeaderboardPerspectiveOwnSettings perspectiveOwnSettings) {
        final LinkWithSettingsGenerator<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> linkWithSettingsGenerator = new LinkWithSettingsGenerator<>(
                EntryPointLinkFactory.LEADERBOARD_PATH,
                contextDefinition);
        final PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> settings = new PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>(
                perspectiveOwnSettings, Collections.emptyMap());
        return linkWithSettingsGenerator.createUrl(settings);
    }
    
    public static String createRaceBoardLinkWithDefaultSettings(UUID eventId, String leaderboardName, String leaderboardGroupName, String regattaName, String raceName) {
        return createRaceBoardLinkWithDefaultSettings(eventId, leaderboardName, leaderboardGroupName, regattaName, raceName, null);
    }
    
    public static String createRaceBoardLinkWithDefaultSettings(UUID eventId, String leaderboardName, String leaderboardGroupName, String regattaName, String raceName, String mode) {
        RaceboardContextDefinition raceboardContext = new RaceboardContextDefinition(regattaName,
                raceName, leaderboardName, leaderboardGroupName, eventId, mode);
        RaceBoardPerspectiveOwnSettings perspectiveOwnSettings = new RaceBoardPerspectiveOwnSettings();
        PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings = new PerspectiveCompositeSettings<>(
                perspectiveOwnSettings, Collections.emptyMap());
        return EntryPointWithSettingsLinkFactory.createRaceBoardLink(raceboardContext, settings);
    }

    public static String createRaceBoardLink(RaceboardContextDefinition ctx,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings) {
        return new LinkWithSettingsGenerator<>(RACE_BOARD_PATH, ctx).createUrl(settings);
    }
    
    public static String createRaceBoardLink(String baseUrl, RaceboardContextDefinition ctx,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings) {
        return new LinkWithSettingsGenerator<>(baseUrl, RACE_BOARD_PATH, ctx).createUrl(settings);
    }
    
    public static String createLeaderboardEditingLink(String leaderboardName) {
        final LinkWithSettingsGenerator<Settings> linkWithSettingsGenerator = new LinkWithSettingsGenerator<>(
                LEADERBOARD_EDITING_PATH, new LeaderboardEditContextDefinition(leaderboardName));
        return linkWithSettingsGenerator.createUrl();
    }

}
