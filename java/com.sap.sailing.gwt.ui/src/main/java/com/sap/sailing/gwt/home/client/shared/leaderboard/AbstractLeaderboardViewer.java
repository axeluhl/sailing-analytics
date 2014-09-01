package com.sap.sailing.gwt.home.client.shared.leaderboard;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.DebugIdHelper;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;

/**
 * A base class for a leaderboard viewer.
 * 
 * @author Frank Mittag (c163874)
 */
public abstract class AbstractLeaderboardViewer extends SimplePanel {
    private final LeaderboardPanel leaderboardPanel;
    protected final CompetitorSelectionModel competitorSelectionProvider;
    protected final AsyncActionsExecutor asyncActionsExecutor;

    protected final Timer timer;

    public AbstractLeaderboardViewer(CompetitorSelectionModel competitorSelectionProvider, AsyncActionsExecutor asyncActionsExecutor,
            Timer timer, LeaderboardPanel leaderboardPanel) {
        this.competitorSelectionProvider = competitorSelectionProvider;
        this.leaderboardPanel = leaderboardPanel;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.timer = timer;
    }
    
    public LeaderboardPanel getLeaderboardPanel() {
        return leaderboardPanel;
    }

    protected <SettingsType> void addComponentToNavigationMenu(final Component<SettingsType> component, boolean isCheckboxEnabled, 
            String componentDisplayName, final boolean hasSettingsWhenComponentIsInvisible) {
        final String componentName = componentDisplayName != null ? componentDisplayName : component.getLocalizedShortName();
        final String debugIdPrefix = DebugIdHelper.createDebugId(componentName);
        final Button settingsButton = new Button("");
        settingsButton.ensureDebugId(debugIdPrefix + "SettingsButton");
        if (component.hasSettings()) {
            settingsButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    SettingsDialog<SettingsType> dialog = new SettingsDialog<SettingsType>(component,
                            StringMessages.INSTANCE);
                    dialog.ensureDebugId(debugIdPrefix + "SettingsDialog");
                    dialog.show();
                }
            });
        }
        settingsButton.setEnabled(component.hasSettings() && hasSettingsWhenComponentIsInvisible);
//        settingsButton.addStyleName(STYLE_VIEWER_TOOLBAR_SETTINGS_BUTTON);
        settingsButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        settingsButton.setTitle(StringMessages.INSTANCE.settingsForComponent(componentName));
    }
}

