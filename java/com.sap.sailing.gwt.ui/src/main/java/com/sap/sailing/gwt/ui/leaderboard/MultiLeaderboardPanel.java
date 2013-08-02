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

    private LeaderboardPanel selectedActLeaderboardPanel;
    private FlowPanel selectedActLeaderboardFlowPanel;

    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final SailingServiceAsync sailingService;

    private String selectedActLeaderboardName;
    
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final UserAgentDetails userAgent;
    private final boolean showRaceDetails;
    private final Timer timer;

    private VerticalPanel mainPanel;
    private final List<Pair<String, String>> actLeaderboardNamesAndDisplayNames;

    private LeaderboardSettings selectedActLeaderboardSettings; 
    
    private TabPanel actLeaderboardsTabPanel;
    private Label actLeaderboardsLabel;
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
        this.selectedActLeaderboardName = preselectedActLeaderboardName;
        
        selectedActLeaderboardFlowPanel = null;
        selectedActLeaderboardPanel = null;
        actLeaderboardNamesAndDisplayNames = new ArrayList<Pair<String, String>>();
        selectedActLeaderboardSettings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null, null, false);
    }

    @Override
    public Widget createWidget() {
        mainPanel = new VerticalPanel();
        
        actLeaderboardsLabel = new Label(stringMessages.actLeaderboards());
        actLeaderboardsLabel.setVisible(false);
        actLeaderboardsLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        actLeaderboardsLabel.getElement().getStyle().setMargin(5, Unit.PX);
        mainPanel.add(actLeaderboardsLabel);

        actLeaderboardsTabPanel = new TabPanel();
        actLeaderboardsTabPanel.setVisible(false);
        actLeaderboardsTabPanel.setAnimationEnabled(false);
        actLeaderboardsTabPanel.setWidth("100%");
        actLeaderboardsTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                Integer tabIndex = event.getSelectedItem();
                if(tabIndex >= 0) {
                    updateSelectedLeaderboard(actLeaderboardNamesAndDisplayNames.get(tabIndex).getA(), tabIndex);
                }
            }
        });
        
        mainPanel.add(actLeaderboardsTabPanel);
        
        updateActLeaderboardSelection();

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
        return selectedActLeaderboardPanel.getSettingsDialogComponent();
    }

    @Override
    public void updateSettings(LeaderboardSettings newSettings) {
        selectedActLeaderboardPanel.updateSettings(newSettings);
    }

    public void setActLeaderboardNames(List<Pair<String, String>> newLeaderboardNamesAndDisplayNames) {
        actLeaderboardNamesAndDisplayNames.clear();
        actLeaderboardNamesAndDisplayNames.addAll(newLeaderboardNamesAndDisplayNames);
        
        updateActLeaderboardSelection();
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
    
    private void updateActLeaderboardSelection() {
        if(actLeaderboardsTabPanel != null) {
            actLeaderboardsTabPanel.clear();
            
            int index = 0;
            int leaderboardCount = actLeaderboardNamesAndDisplayNames.size();
            for (Pair<String, String> leaderboardNameAndDisplayName : actLeaderboardNamesAndDisplayNames) {
                FlowPanel tabFlowPanel = new FlowPanel();
                actLeaderboardsTabPanel.add(tabFlowPanel, leaderboardNameAndDisplayName.getB(), false);

                if(selectedActLeaderboardName != null && selectedActLeaderboardName.equals(leaderboardNameAndDisplayName.getA())) {
                    actLeaderboardsTabPanel.selectTab(index);
                }
                index++;
            }
            // show the last leaderboard when no leaderboard is selected yet 
            if(selectedActLeaderboardName == null && leaderboardCount > 0) {
                actLeaderboardsTabPanel.selectTab(leaderboardCount-1);
            }
            
            actLeaderboardsTabPanel.setVisible(leaderboardCount > 0);
            actLeaderboardsLabel.setVisible(leaderboardCount > 0);
        }
    }

    private void updateSelectedLeaderboard(String selectedLeaderboardName, int newTabIndex) {
        if(selectedLeaderboardName != null) {
            if(selectedActLeaderboardPanel != null && selectedActLeaderboardFlowPanel != null) {
                selectedActLeaderboardFlowPanel.remove(selectedActLeaderboardPanel);
                selectedActLeaderboardPanel = null;
                selectedActLeaderboardFlowPanel = null;
            }
            
            selectedActLeaderboardFlowPanel = (FlowPanel) actLeaderboardsTabPanel.getWidget(newTabIndex);
            selectedActLeaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                    selectedActLeaderboardSettings, /* preselectedRace*/ null, new CompetitorSelectionModel(true), timer,
                    null, selectedLeaderboardName, errorReporter, stringMessages, userAgent,
                    showRaceDetails, /* raceTimesInfoProvider */null, false,  /* adjustTimerDelay */ true);
            selectedActLeaderboardFlowPanel.add(selectedActLeaderboardPanel);
        } else {
            if(selectedActLeaderboardPanel != null && selectedActLeaderboardFlowPanel != null) {
                selectedActLeaderboardFlowPanel.remove(selectedActLeaderboardPanel);
                selectedActLeaderboardPanel = null;
                selectedActLeaderboardFlowPanel = null;
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
        if(selectedActLeaderboardPanel != null) {
            selectedActLeaderboardPanel.timeChanged(date);
        }
    }

}
