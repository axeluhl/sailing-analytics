package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util.Pair;
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
import com.sap.sailing.gwt.ui.client.shared.components.ComponentViewerViewModes;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;

public class MetaLeaderboardViewer extends SimplePanel {    
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final SailingServiceAsync sailingService;
    private final CompetitorSelectionModel competitorSelectionProvider;

    private FlowPanel componentsNavigationPanel;

    private final LeaderboardPanel metaLeaderboardPanel;
    private LeaderboardPanel selectedLeaderboardPanel;
    private final MultiCompetitorLeaderboardChart multiCompetitorChart;

    private String selectedLeaderboardName;
    
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final String metaLeaderboardName;
    private final String leaderboardGroupName;
    private final UserAgentDetails userAgent;
    private final boolean showRaceDetails;
    private final Timer timer;
    private final ComponentViewerViewModes viewMode;
    
    private final FlowPanel mainPanel;
    private final List<String> leaderboards;
    private ListBox leaderboardSelectionListBox;
    
    private final static String STYLE_VIEWER_TOOLBAR = "viewerToolbar";
    private final static String STYLE_VIEWER_TOOLBAR_INNERELEMENT = "viewerToolbar-innerElement";
    private final static String STYLE_VIEWER_TOOLBAR_SETTINGS_BUTTON = "viewerToolbar-settingsButton";

    public MetaLeaderboardViewer(long delayToLiveMillis, SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            LeaderboardSettings leaderboardSettings, String preselectedLeaderboardName, RaceIdentifier preselectedRace, String leaderboardGroupName,
            String metaLeaderboardName, ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, boolean showRaceDetails, boolean autoExpandLastRaceColumn, boolean showRankChart) {
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        
        asyncActionsExecutor = new AsyncActionsExecutor();
        this.metaLeaderboardName = metaLeaderboardName;
        this.leaderboardGroupName = leaderboardGroupName;
        this.userAgent = userAgent;
        this.showRaceDetails = showRaceDetails;
        this.selectedLeaderboardName = preselectedLeaderboardName;
        
        viewMode = ComponentViewerViewModes.WEB_VIEW;

        competitorSelectionProvider = new CompetitorSelectionModel(/* hasMultiSelection */true);

        mainPanel = new FlowPanel();
        mainPanel.setSize("100%", "100%");
        setWidget(mainPanel);
        
        getElement().getStyle().setMarginLeft(12, Unit.PX);
        getElement().getStyle().setMarginRight(12, Unit.PX);
        
        final Label overallStandingsLabel = new Label(stringMessages.overallStandings());
        overallStandingsLabel.setStyleName("leaderboardHeading");

        componentsNavigationPanel = new FlowPanel();
        componentsNavigationPanel.addStyleName(STYLE_VIEWER_TOOLBAR);
        mainPanel.add(componentsNavigationPanel);

        timer = new Timer(PlayModes.Replay, /*delayBetweenAutoAdvancesInMilliseconds*/ 3000l);
        timer.setLivePlayDelayInMillis(delayToLiveMillis);

        metaLeaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                leaderboardSettings, preselectedRace, competitorSelectionProvider, timer,
                leaderboardGroupName, metaLeaderboardName, errorReporter, stringMessages, userAgent,
                showRaceDetails, /* raceTimesInfoProvider */null, autoExpandLastRaceColumn);
        
        multiCompetitorChart = new MultiCompetitorLeaderboardChart(sailingService, asyncActionsExecutor, metaLeaderboardName, DetailType.REGATTA_RANK, competitorSelectionProvider, timer,
                stringMessages, errorReporter);
        multiCompetitorChart.setVisible(showRankChart); 
        multiCompetitorChart.getElement().getStyle().setMarginTop(10, Unit.PX);
        multiCompetitorChart.getElement().getStyle().setMarginBottom(10, Unit.PX);
        
        mainPanel.add(metaLeaderboardPanel);
        mainPanel.add(multiCompetitorChart);

        addComponentToNavigationMenu(metaLeaderboardPanel, false, "Series Leaderboard");
        addComponentToNavigationMenu(multiCompetitorChart, true, null);
        addComponentToNavigationMenu(selectedLeaderboardPanel, false, "Act Leaderboard");
    
        leaderboards = new ArrayList<String>();
        leaderboardSelectionListBox = new ListBox();
        leaderboardSelectionListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selIndex =leaderboardSelectionListBox.getSelectedIndex();
                if(selIndex >= 0) {
                    updateSelectedLeaderboard(leaderboardSelectionListBox.getItemText(selIndex));
                }
            }
        });
        leaderboardSelectionListBox.setVisible(false);
        mainPanel.add(leaderboardSelectionListBox);
        
        updateLeaderboardsOfMetaleaderboard();
        
        if (selectedLeaderboardName != null) {
            updateSelectedLeaderboard(selectedLeaderboardName);
        }
    }

    private void updateLeaderboardsOfMetaleaderboard() {
        sailingService.getLeaderboardsNamesOfMetaleaderboard(metaLeaderboardName, new AsyncCallback<List<Pair<String, String>>>() {
            
            @Override
            public void onSuccess(List<Pair<String, String>> leaderboardNamesAndDisplayNames) {
                leaderboardSelectionListBox.clear();
                
                int index = 0;
                for (Pair<String, String> leaderboardNameAndDisplayName : leaderboardNamesAndDisplayNames) {
                    leaderboardSelectionListBox.addItem(leaderboardNameAndDisplayName.getB(), leaderboardNameAndDisplayName.getA());
                    if(selectedLeaderboardName != null && selectedLeaderboardName.equals(leaderboardNameAndDisplayName.getA())) {
                        leaderboardSelectionListBox.setSelectedIndex(index);
                    }
                    index++;
                }
                
                leaderboardSelectionListBox.setVisible(leaderboardNamesAndDisplayNames.size() > 0);
            }
            
            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }
    
    private <SettingsType> void addComponentToNavigationMenu(final Component<SettingsType> component, boolean isCheckboxEnabled, 
            String componentDisplayName) {
        final String componentName = componentDisplayName != null ? componentDisplayName : component.getLocalizedShortName(); 
        final CheckBox checkBox= new CheckBox(componentName);
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
        settingsButton.setTitle(stringMessages.settingsForComponent(componentName));
        
        componentsNavigationPanel.add(settingsButton);
    }
    
    private void updateSelectedLeaderboard(String selectedLeaderboardName) {
        if(selectedLeaderboardName != null) {
            if(selectedLeaderboardPanel == null) {
                LeaderboardSettings newDefaultSettings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null, null, false);
                selectedLeaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                        newDefaultSettings, /* preselectedRace*/ null, competitorSelectionProvider, timer,
                        leaderboardGroupName, metaLeaderboardName, errorReporter, stringMessages, userAgent,
                        showRaceDetails, /* raceTimesInfoProvider */null, false);              
            }
        } else {
            if(selectedLeaderboardPanel != null) {
                remove(selectedLeaderboardPanel);
                selectedLeaderboardPanel = null;
            }
        }
    }

}
