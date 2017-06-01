package com.sap.sailing.gwt.home.desktop.places.event.regatta;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.desktop.places.Consumer;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardUrlSettings;
import com.sap.sailing.gwt.settings.client.utils.StorageDefinitionIdFactory;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ClassicLeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sse.gwt.client.mutationobserver.ElementSizeMutationObserver;
import com.sap.sse.gwt.client.mutationobserver.ElementSizeMutationObserver.DomMutationCallback;
import com.sap.sse.gwt.client.shared.perspective.ComponentContext;
import com.sap.sse.gwt.client.shared.perspective.ComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.DefaultOnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.perspective.SettingsStorageManager;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.settings.PlaceBasedUserSettingsStorageManager;
import com.sap.sse.security.ui.settings.StorageDefinitionId;

/**
 * An abstract regatta tabView with some shared functions between the leaderboard tab and competitors chart tab 
 */
public abstract class SharedLeaderboardRegattaTabView<T extends AbstractEventRegattaPlace> extends Composite implements RegattaTabView<T>,
        LeaderboardUpdateListener {
    private boolean initialLeaderboardSizeCalculated = false;

    public SharedLeaderboardRegattaTabView() {
    }

    public void createSharedLeaderboardPanel(String leaderboardName, RegattaAnalyticsDataManager regattaAnalyticsManager, UserService userService,
            String placeToken, final Consumer<LeaderboardPanel> leaderboardConsumer) {
        
        // FIXME remove
        boolean autoExpandLastRaceColumn = GwtHttpRequestUtils.getBooleanParameter(
                LeaderboardUrlSettings.PARAM_AUTO_EXPAND_LAST_RACE_COLUMN, false);
        
        final ComponentContext<LeaderboardSettings> componentContext = createLeaderboardComponentContext(leaderboardName, userService,
                placeToken);
        componentContext.initInitialSettings(new DefaultOnSettingsLoadedCallback<LeaderboardSettings>() {
            @Override
            public void onSuccess(LeaderboardSettings leaderboardSettings) {
                final ClassicLeaderboardPanel leaderboardPanel = regattaAnalyticsManager.createLeaderboardPanel(null, componentContext, //
                        leaderboardSettings, //
                        null, //
                        "leaderboardGroupName", // TODO: keep using magic string? ask frank!
                        leaderboardName, //
                        true,
                        autoExpandLastRaceColumn);
                leaderboardPanel.addAttachHandler(new Handler() {

                    @Override
                    public void onAttachOrDetach(AttachEvent event) {
                        if(!event.isAttached()) {
                            componentContext.dispose();
                        }
                    }
                    
                });
                
                if(ElementSizeMutationObserver.isSupported()) {
                    ElementSizeMutationObserver observer = new ElementSizeMutationObserver(new DomMutationCallback() {
                        @Override
                        public void onSizeChanged(int newWidth, int newHeight) {
                            if(newWidth > 0 && newHeight > 0 && newWidth > 1500 && initialLeaderboardSizeCalculated == false) {
                                int numberOfLastRacesToShow = (1500 - 600) / 50;
                                leaderboardPanel.setRaceColumnSelectionToLastNStrategy(numberOfLastRacesToShow);
                                initialLeaderboardSizeCalculated = true;
                            }
                        }
                    }); 
                    observer.observe(leaderboardPanel.getLeaderboardTable().getElement());
                }
                leaderboardConsumer.consume(leaderboardPanel);
            }
        });
    }
    
    protected ComponentContext<LeaderboardSettings> createLeaderboardComponentContext(String leaderboardName, UserService userService,
            String placeToken) {
        final LeaderboardPanelLifecycle lifeCycle = new LeaderboardPanelLifecycle(null, StringMessages.INSTANCE);
        final StorageDefinitionId storageDefinitionId = StorageDefinitionIdFactory.createStorageDefinitionIdForEventRegattaLeaderboard(leaderboardName);
        final SettingsStorageManager<LeaderboardSettings> settingsStorageManager = new PlaceBasedUserSettingsStorageManager<>(
                userService, storageDefinitionId, placeToken);

        final ComponentContext<LeaderboardSettings> componentContext = new ComponentContextWithSettingsStorage<>(
                lifeCycle, settingsStorageManager);
        return componentContext;
    }
}