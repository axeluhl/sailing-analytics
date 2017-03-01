package com.sap.sailing.gwt.home.desktop.places.fakeseries;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.desktop.places.Consumer;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardUrlSettings;
import com.sap.sailing.gwt.settings.client.utils.StorageDefinitionIdFactory;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sse.gwt.client.shared.perspective.ComponentContext;
import com.sap.sse.gwt.client.shared.perspective.ComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.DefaultOnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.perspective.SettingsStorageManager;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.settings.PlaceBasedUserSettingsStorageManager;
import com.sap.sse.security.ui.settings.StorageDefinitionId;

/**
 * An abstract series tabView with some shared functions between the overall leaderboard tab and competitors chart tab
 */
public abstract class SharedLeaderboardEventSeriesTabView<T extends AbstractSeriesTabPlace> extends Composite
        implements SeriesTabView<T>, LeaderboardUpdateListener {

    public SharedLeaderboardEventSeriesTabView() {
    }

    protected void createSharedLeaderboardPanel(String leaderboardName,
            EventSeriesAnalyticsDataManager eventSeriesAnalyticsManager, UserService userService,
            String placeToken, final Consumer<LeaderboardPanel> consumer) {
        
        // FIXME remove
        boolean autoExpandLastRaceColumn = GwtHttpRequestUtils
                .getBooleanParameter(LeaderboardUrlSettings.PARAM_AUTO_EXPAND_LAST_RACE_COLUMN, false);

        final ComponentContext<LeaderboardSettings> componentContext = createLeaderboardComponentContext(leaderboardName, userService,
                placeToken);
        componentContext.initInitialSettings(new DefaultOnSettingsLoadedCallback<LeaderboardSettings>() {
            @Override
            public void onSuccess(LeaderboardSettings leaderboardSettings) {
                LeaderboardPanel leaderboardPanel = eventSeriesAnalyticsManager.createOverallLeaderboardPanel(null,
                        componentContext, leaderboardSettings, null, "leaderboardGroupName",
                        leaderboardName, true, // this information came from place, now hard coded. check with frank
                        autoExpandLastRaceColumn);
                leaderboardPanel.addLeaderboardUpdateListener(SharedLeaderboardEventSeriesTabView.this);
                consumer.consume(leaderboardPanel);
            }
        });
    }

    protected ComponentContext<LeaderboardSettings> createLeaderboardComponentContext(String leaderboardName, UserService userService,
            String placeToken) {
        final LeaderboardPanelLifecycle lifeCycle = new LeaderboardPanelLifecycle(null, StringMessages.INSTANCE);
        final StorageDefinitionId storageDefinitionId = StorageDefinitionIdFactory.createStorageDefinitionIdForSeriesOverallLeaderboard(leaderboardName);
        final SettingsStorageManager<LeaderboardSettings> settingsStorageManager = new PlaceBasedUserSettingsStorageManager<>(
                userService, storageDefinitionId, placeToken);

        final ComponentContext<LeaderboardSettings> componentContext = new ComponentContextWithSettingsStorage<>(
                lifeCycle, settingsStorageManager);
        return componentContext;
    }
}
