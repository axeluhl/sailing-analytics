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
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.UserAgentDetails;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;

public class LeaderboardViewer extends SimplePanel {
    private final StringMessages stringMessages;

    private final LeaderboardPanel leaderboardPanel;
    private final MultiCompetitorLeaderboardChart multiCompetitorChart;
    
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final Timer timer;
    private FlowPanel componentsNavigationPanel;

    private final CompetitorSelectionModel competitorSelectionProvider;
   
    private final static String STYLE_VIEWER_TOOLBAR = "viewerToolbar";
    private final static String STYLE_VIEWER_TOOLBAR_INNERELEMENT = "viewerToolbar-innerElement";
    private final static String STYLE_VIEWER_TOOLBAR_SETTINGS_BUTTON = "viewerToolbar-settingsButton";
    
    public LeaderboardViewer(long delayToLiveMillis, SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            LeaderboardSettings leaderboardSettings, RaceIdentifier preselectedRace, String leaderboardGroupName,
            String leaderboardName, ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, boolean showRaceDetails, boolean autoExpandLastRaceColumn, boolean showRankChart) {
        this.stringMessages = stringMessages;
        this.asyncActionsExecutor = asyncActionsExecutor;
        
        competitorSelectionProvider = new CompetitorSelectionModel(/* hasMultiSelection */true);
        
        FlowPanel mainPanel = new FlowPanel();
        mainPanel.setSize("100%", "100%");
        setWidget(mainPanel);
        
        getElement().getStyle().setMarginLeft(12, Unit.PX);
        getElement().getStyle().setMarginRight(12, Unit.PX);

        componentsNavigationPanel = new FlowPanel();
        componentsNavigationPanel.addStyleName(STYLE_VIEWER_TOOLBAR);
        mainPanel.add(componentsNavigationPanel);

        
        timer = new Timer(PlayModes.Replay, /*delayBetweenAutoAdvancesInMilliseconds*/ 3000l);
        timer.setLivePlayDelayInMillis(delayToLiveMillis);
        leaderboardPanel = createLeaderboardPanel(delayToLiveMillis, sailingService, leaderboardSettings, preselectedRace,
                leaderboardGroupName, leaderboardName, errorReporter, stringMessages, userAgent,
                showRaceDetails, autoExpandLastRaceColumn);

        multiCompetitorChart = new MultiCompetitorLeaderboardChart(sailingService, asyncActionsExecutor, leaderboardName, DetailType.REGATTA_RANK, competitorSelectionProvider, timer,
                stringMessages, errorReporter);
        multiCompetitorChart.setVisible(showRankChart); 
        multiCompetitorChart.getElement().getStyle().setMarginTop(10, Unit.PX);
        multiCompetitorChart.getElement().getStyle().setMarginBottom(10, Unit.PX);

        mainPanel.add(leaderboardPanel);
        mainPanel.add(multiCompetitorChart);

        addComponentToNavigationMenu(leaderboardPanel, false);
        addComponentToNavigationMenu(multiCompetitorChart, true);
        
        if(showRankChart) {
            multiCompetitorChart.timeChanged(timer.getTime());
        }
    }

    private LeaderboardPanel createLeaderboardPanel(long delayToLiveMillis, SailingServiceAsync sailingService,
            LeaderboardSettings leaderboardSettings, RaceIdentifier preselectedRace, String leaderboardGroupName,
            String leaderboardName, ErrorReporter leaderboardEntryPoint, StringMessages stringMessages,
            UserAgentDetails userAgent, boolean showRaceDetails, boolean autoExpandLastRaceColumn) {

        LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                leaderboardSettings, preselectedRace, competitorSelectionProvider, timer,
                leaderboardGroupName, leaderboardName, leaderboardEntryPoint, stringMessages, userAgent,
                showRaceDetails, /* raceTimesInfoProvider */null, autoExpandLastRaceColumn);
 
        return leaderboardPanel;
    }

    private <SettingsType> void addComponentToNavigationMenu(final Component<SettingsType> component, boolean withCheckbox) {
        final CheckBox checkBox= new CheckBox(component.getLocalizedShortName());
        checkBox.getElement().getStyle().setFloat(Style.Float.LEFT);

        checkBox.setEnabled(withCheckbox);
        checkBox.setValue(component.isVisible());
        checkBox.setTitle(stringMessages.showHideComponent(component.getLocalizedShortName()));
        checkBox.addStyleName(STYLE_VIEWER_TOOLBAR_INNERELEMENT);

        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> newValue) {
                boolean visible = checkBox.getValue();
                component.setVisible(visible);

                if (visible && component instanceof TimeListener) {
                    // trigger the component to update its data
                    ((TimeListener) component).timeChanged(timer.getTime());
                }
            }
        });

        componentsNavigationPanel.add(checkBox);

        Button settingsButton = new Button("");
        
        if(component.hasSettings()) {
            settingsButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    new SettingsDialog<SettingsType>(component, stringMessages).show();
                }
            });
        } else {
           settingsButton.setEnabled(false);
        }
        settingsButton.addStyleName(STYLE_VIEWER_TOOLBAR_SETTINGS_BUTTON);
        settingsButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        settingsButton.setTitle(stringMessages.settingsForComponent(component.getLocalizedShortName()));
        
        componentsNavigationPanel.add(settingsButton);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        
        if(visible) {
            timer.addTimeListener(multiCompetitorChart);
        } else {
            timer.removeTimeListener(multiCompetitorChart);
        }
    }
}
