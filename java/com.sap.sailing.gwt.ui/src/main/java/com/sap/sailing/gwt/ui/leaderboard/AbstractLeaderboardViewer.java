package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.gwt.settings.client.leaderboard.AbstractLeaderboardPerspectiveLifecycle;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardPerspectiveOwnSettings;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.DebugIdHelper;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveComposite;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * A base class for a leaderboard viewer.
 * 
 * @author Frank Mittag (c163874)
 */
public abstract class AbstractLeaderboardViewer<PL extends AbstractLeaderboardPerspectiveLifecycle> extends AbstractPerspectiveComposite<PL, LeaderboardPerspectiveOwnSettings> {
    
    protected static final ViewerToolbar RES = GWT.create(ViewerToolbar.class);
    protected final StringMessages stringMessages;
    private LeaderboardPanel<?> leaderboardPanel;
    protected final CompetitorSelectionModel competitorSelectionProvider;
    protected final AsyncActionsExecutor asyncActionsExecutor;

    private FlowPanel componentsNavigationPanel;

    protected final Timer timer;
    protected final boolean hideToolbar;


    public AbstractLeaderboardViewer(Component<?> parent,
            ComponentContext<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> componentContext,
            PL lifecycle,
            PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> settings,
            CompetitorSelectionModel competitorSelectionProvider, AsyncActionsExecutor asyncActionsExecutor,
            Timer timer, StringMessages stringMessages) {
        super(parent, componentContext, lifecycle, settings);
        

        this.competitorSelectionProvider = competitorSelectionProvider;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.stringMessages = stringMessages;
        this.timer = timer;
        this.hideToolbar = settings.getPerspectiveOwnSettings().isHideToolbar();
        
        RES.css().ensureInjected();
    }
    
    protected void init(LeaderboardPanel<?> leaderboardPanel) {
        addChildComponent(leaderboardPanel);
        this.leaderboardPanel = leaderboardPanel;
    }

    public LeaderboardPanel<?> getLeaderboardPanel() {
        return leaderboardPanel;
    }

    protected FlowPanel createViewerPanel() {
        FlowPanel mainPanel = new FlowPanel();
        mainPanel.setSize("100%", "100%");
        mainPanel.getElement().getStyle().setMarginLeft(12, Unit.PX);
        mainPanel.getElement().getStyle().setMarginRight(12, Unit.PX);
        if (!hideToolbar) {
            componentsNavigationPanel = new FlowPanel();
            componentsNavigationPanel.addStyleName(RES.css().viewerToolbar());
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
            checkBox.addStyleName(RES.css().viewerToolbarInnerElement());
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
            settingsButton.addStyleName(RES.css().viewerToolbarSettingsButton());
            settingsButton.getElement().getStyle().setFloat(Style.Float.LEFT);
            settingsButton.setTitle(stringMessages.settingsForComponent(componentName));
            componentsNavigationPanel.add(settingsButton);
        }
    }


    @Override
    public SettingsDialogComponent<LeaderboardPerspectiveOwnSettings> getPerspectiveOwnSettingsDialogComponent() {
        return null;
    }

    @Override
    public boolean hasPerspectiveOwnSettings() {
        return true;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getDependentCssClassName() {
        return null;
    }
}

