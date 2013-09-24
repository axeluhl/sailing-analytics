package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.UserAgentDetails;
import com.sap.sailing.gwt.ui.client.shared.components.AbstractLazyComponent;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;

/**
 * A panel managing multiple {@link LeaderboardPanel}s (e.g. from a series leaderboard) so that the user can switch between them. 
 * @author Frank
 */
public class MultiLeaderboardPanel extends AbstractLazyComponent<LeaderboardSettings> implements TimeListener {

    private LeaderboardPanel selectedRegattaLeaderboardPanel;
    private FlowPanel selectedRegattaLeaderboardFlowPanel;

    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final SailingServiceAsync sailingService;

    private String selectedRegattaLeaderboardName;
    
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final UserAgentDetails userAgent;
    private final boolean showRaceDetails;
    private final Timer timer;

    private VerticalPanel mainPanel;
    private final List<Pair<String, String>> regattaLeaderboardNamesAndDisplayNames;

    private LeaderboardSettings selectedRegattaLeaderboardSettings; 
    
    private TabPanel regattaLeaderboardsTabPanel;
    private Label regattaLeaderboardsLabel;
    private final String metaLeaderboardName;
    
    public MultiLeaderboardPanel(SailingServiceAsync sailingService, String metaLeaderboardName, AsyncActionsExecutor asyncActionsExecutor, Timer timer,
            LeaderboardSettings leaderboardSettings, String preselectedActLeaderboardName, RaceIdentifier preselectedRace, 
            ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, boolean showRaceDetails, boolean autoExpandLastRaceColumn) {
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.sailingService = sailingService;
        this.metaLeaderboardName = metaLeaderboardName;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.userAgent = userAgent;
        this.showRaceDetails = showRaceDetails;
        this.timer = timer;
        this.selectedRegattaLeaderboardName = preselectedActLeaderboardName;
        
        selectedRegattaLeaderboardFlowPanel = null;
        selectedRegattaLeaderboardPanel = null;
        regattaLeaderboardNamesAndDisplayNames = new ArrayList<Pair<String, String>>();
        selectedRegattaLeaderboardSettings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null, null, false);
    }

    @Override
    public Widget createWidget() {
        mainPanel = new VerticalPanel();
        
        regattaLeaderboardsLabel = new Label(stringMessages.regattaLeaderboards());
        regattaLeaderboardsLabel.setVisible(false);
        regattaLeaderboardsLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        regattaLeaderboardsLabel.getElement().getStyle().setMargin(5, Unit.PX);
        mainPanel.add(regattaLeaderboardsLabel);

        regattaLeaderboardsTabPanel = new TabPanel();
        regattaLeaderboardsTabPanel.setVisible(false);
        regattaLeaderboardsTabPanel.setAnimationEnabled(false);
        regattaLeaderboardsTabPanel.setWidth("100%");
        regattaLeaderboardsTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                Integer tabIndex = event.getSelectedItem();
                if(tabIndex >= 0) {
                    updateSelectedLeaderboard(regattaLeaderboardNamesAndDisplayNames.get(tabIndex).getA(), tabIndex);
                }
            }
        });
        
        mainPanel.add(regattaLeaderboardsTabPanel);
        
        updateRegattaLeaderboardSelection();

        return mainPanel;
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.leaderboards();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<LeaderboardSettings> getSettingsDialogComponent() {
        return selectedRegattaLeaderboardPanel.getSettingsDialogComponent();
    }

    @Override
    public void updateSettings(LeaderboardSettings newSettings) {
        selectedRegattaLeaderboardPanel.updateSettings(newSettings);
    }

    public void setActLeaderboardNames(List<Pair<String, String>> newLeaderboardNamesAndDisplayNames) {
        regattaLeaderboardNamesAndDisplayNames.clear();
        regattaLeaderboardNamesAndDisplayNames.addAll(newLeaderboardNamesAndDisplayNames);
        
        updateRegattaLeaderboardSelection();
    }

    private void readAndUpdateLeaderboardsOfMetaleaderboard() {
        sailingService.getLeaderboardsNamesOfMetaleaderboard(metaLeaderboardName, new AsyncCallback<List<Pair<String, String>>>() {
            
            @Override
            public void onSuccess(List<Pair<String, String>> leaderboardNamesAndDisplayNames) {
                setActLeaderboardNames(leaderboardNamesAndDisplayNames);
            }
            
            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }
    
    private void updateRegattaLeaderboardSelection() {
        if(regattaLeaderboardsTabPanel != null) {
            regattaLeaderboardsTabPanel.clear();
            
            int index = 0;
            int leaderboardCount = regattaLeaderboardNamesAndDisplayNames.size();
            for (Pair<String, String> leaderboardNameAndDisplayName : regattaLeaderboardNamesAndDisplayNames) {
                FlowPanel tabFlowPanel = new FlowPanel();
                regattaLeaderboardsTabPanel.add(tabFlowPanel, leaderboardNameAndDisplayName.getB(), false);

                if(selectedRegattaLeaderboardName != null && selectedRegattaLeaderboardName.equals(leaderboardNameAndDisplayName.getA())) {
                    regattaLeaderboardsTabPanel.selectTab(index);
                }
                index++;
            }
            // show the last leaderboard when no leaderboard is selected yet 
            if(selectedRegattaLeaderboardName == null && leaderboardCount > 0) {
                regattaLeaderboardsTabPanel.selectTab(leaderboardCount-1);
            }
            
            regattaLeaderboardsTabPanel.setVisible(leaderboardCount > 0);
            regattaLeaderboardsLabel.setVisible(leaderboardCount > 0);
        }
    }

    private void updateSelectedLeaderboard(String selectedLeaderboardName, int newTabIndex) {
        if(selectedLeaderboardName != null) {
            if(selectedRegattaLeaderboardPanel != null && selectedRegattaLeaderboardFlowPanel != null) {
                selectedRegattaLeaderboardFlowPanel.remove(selectedRegattaLeaderboardPanel);
                selectedRegattaLeaderboardPanel = null;
                selectedRegattaLeaderboardFlowPanel = null;
            }
            
            selectedRegattaLeaderboardFlowPanel = (FlowPanel) regattaLeaderboardsTabPanel.getWidget(newTabIndex);
            selectedRegattaLeaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                    selectedRegattaLeaderboardSettings, /* preselectedRace*/ null, new CompetitorSelectionModel(true), timer,
                    null, selectedLeaderboardName, errorReporter, stringMessages, userAgent,
                    showRaceDetails, /* raceTimesInfoProvider */null, false,  /* adjustTimerDelay */ true);
            selectedRegattaLeaderboardFlowPanel.add(selectedRegattaLeaderboardPanel);
        } else {
            if(selectedRegattaLeaderboardPanel != null && selectedRegattaLeaderboardFlowPanel != null) {
                selectedRegattaLeaderboardFlowPanel.remove(selectedRegattaLeaderboardPanel);
                selectedRegattaLeaderboardPanel = null;
                selectedRegattaLeaderboardFlowPanel = null;
            }
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        
        if(visible) {
            readAndUpdateLeaderboardsOfMetaleaderboard();
        } else {
            updateSelectedLeaderboard(null, -1);
        }
    }

    @Override
    public void timeChanged(Date date) {
        if(selectedRegattaLeaderboardPanel != null) {
            selectedRegattaLeaderboardPanel.timeChanged(date);
        }
    }

}
