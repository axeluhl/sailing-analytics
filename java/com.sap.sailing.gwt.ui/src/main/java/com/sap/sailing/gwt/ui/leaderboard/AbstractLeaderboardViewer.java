package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.settings.Settings;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.DebugIdHelper;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;

/**
 * A base class for a leaderboard viewer.
 * 
 * @author Frank Mittag (c163874)
 */
public abstract class AbstractLeaderboardViewer extends SimplePanel {
    protected final StringMessages stringMessages;
    private final LeaderboardPanel leaderboardPanel;
    protected final CompetitorSelectionModel competitorSelectionProvider;
    protected final AsyncActionsExecutor asyncActionsExecutor;

    private FlowPanel componentsNavigationPanel;

    protected final Timer timer;
    protected final boolean hideToolbar;
    
    private final static String STYLE_VIEWER_TOOLBAR = "viewerToolbar";
    private final static String STYLE_VIEWER_TOOLBAR_INNERELEMENT = "viewerToolbar-innerElement";
    private final static String STYLE_VIEWER_TOOLBAR_SETTINGS_BUTTON = "viewerToolbar-settingsButton";

    public AbstractLeaderboardViewer(CompetitorSelectionModel competitorSelectionProvider, AsyncActionsExecutor asyncActionsExecutor,
            Timer timer, StringMessages stringMessages, boolean hideToolbar, LeaderboardPanel leaderboardPanel) {
        this.competitorSelectionProvider = competitorSelectionProvider;
        this.leaderboardPanel = leaderboardPanel;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.stringMessages = stringMessages;
        this.timer = timer;
        this.hideToolbar = hideToolbar;
    }
    
    public LeaderboardPanel getLeaderboardPanel() {
        return leaderboardPanel;
    }

    protected FlowPanel createViewerPanel() {
        FlowPanel mainPanel = new FlowPanel();
        mainPanel.setSize("100%", "100%");
        getElement().getStyle().setMarginLeft(12, Unit.PX);
        getElement().getStyle().setMarginRight(12, Unit.PX);
        if (!hideToolbar) {
            componentsNavigationPanel = new FlowPanel();
            componentsNavigationPanel.addStyleName(STYLE_VIEWER_TOOLBAR);
            mainPanel.add(componentsNavigationPanel);
        }
        return mainPanel;
    }
    
    protected <SettingsType extends Settings> void addComponentToNavigationMenu(final Component<SettingsType> component, boolean isCheckboxEnabled, 
            String componentDisplayName, final boolean hasSettingsWhenComponentIsInvisible) {
        if (!hideToolbar) {
            final String componentName = componentDisplayName != null ? componentDisplayName : component.getLocalizedShortName();
            final String debugIdPrefix = DebugIdHelper.createDebugId(componentName);
            final CheckBox checkBox = new CheckBox(componentName);
            checkBox.ensureDebugId(debugIdPrefix + "DisplayCheckBox");
            final Button settingsButton = new Button("");
            settingsButton.ensureDebugId(debugIdPrefix + "SettingsButton");
            checkBox.getElement().getStyle().setFloat(Style.Float.LEFT);
            checkBox.setEnabled(isCheckboxEnabled);
            checkBox.setValue(component.isVisible());
            checkBox.setTitle(stringMessages.showHideComponent(componentName));
            checkBox.addStyleName(STYLE_VIEWER_TOOLBAR_INNERELEMENT);
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> newValue) {
                    boolean visible = checkBox.getValue();
                    component.setVisible(visible);
                    if (visible && component instanceof TimeListener) {
                        // trigger the component to update its data
                        ((TimeListener) component).timeChanged(timer.getTime(), null);
                    }
                    if (component.hasSettings() && !hasSettingsWhenComponentIsInvisible) {
                        settingsButton.setEnabled(visible);
                    }
                }
            });
            componentsNavigationPanel.add(checkBox);
            if (component.hasSettings()) {
                settingsButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        SettingsDialog<SettingsType> dialog = new SettingsDialog<SettingsType>(component,
                                stringMessages);
                        dialog.ensureDebugId(debugIdPrefix + "SettingsDialog");
                        dialog.show();
                    }
                });
            }
            settingsButton.setEnabled(component.hasSettings() && hasSettingsWhenComponentIsInvisible);
            settingsButton.addStyleName(STYLE_VIEWER_TOOLBAR_SETTINGS_BUTTON);
            settingsButton.getElement().getStyle().setFloat(Style.Float.LEFT);
            settingsButton.setTitle(stringMessages.settingsForComponent(componentName));
            componentsNavigationPanel.add(settingsButton);
        }
    }
}

