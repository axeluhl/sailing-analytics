package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;

/**
 * The {@link RaceBoardEntryPoint} implements a versatile race viewer perspective that comes with a leaderboard, charts
 * and a central map component. Through various options, this viewer and its components can be configured in various
 * ways, making it a powerful and flexible tool. In order to simplify usage for specific anticipated scenarios,
 * a {@link RaceBoardMode} implements a strategy for pre-configuring the various settings and component states
 * in a way useful for that particular scenario. For example, a "start analysis" mode would adjust the
 * {@link LeaderboardSettings} such that the columns making relevant statements about the start are shown
 * whereas others are suppressed; the timer would be set to the time of the race start, and the competitor chart
 * would be shown, with speed over ground as the metric selected and the top three boats selected.<p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RaceBoardMode {
    /**
     * Applies this mode to the {@code raceBoardPanel}, making all adjustments necessary to the perspective, its
     * components and their settings to put the panel into this mode.
     * <p>
     * 
     * Implementations in this same package can use the package-protected (default-scoped) methods of
     * {@link RaceBoardPanel} to access the timer and the components that constitute the panel, such as
     * {@link RaceBoardPanel#getLeaderboardPanel()}, {@link RaceBoardPanel#getMap()} or
     * {@link RaceBoardPanel#getTimer()}.
     */
    void applyTo(RaceBoardPanel raceBoardPanel);
}
