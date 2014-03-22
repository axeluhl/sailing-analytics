package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.UserAgentDetails;
import com.sap.sailing.gwt.ui.client.shared.components.AbstractLazyComponent;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;

/**
 * A panel managing multiple {@link LeaderboardPanel}s (e.g. from a meta leaderboard) so that the user can switch between them. 
 * @author Frank
 */
public class MultiLeaderboardPanel extends AbstractLazyComponent<LeaderboardSettings> implements TimeListener {

    private LeaderboardPanel selectedLeaderboardPanel;
    private FlowPanel selectedLeaderboardFlowPanel;

    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final SailingServiceAsync sailingService;

    private String selectedLeaderboardName;
    
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final UserAgentDetails userAgent;
    private final boolean showRaceDetails;
    private final Timer timer;

    private VerticalPanel mainPanel;
    private final List<Pair<String, String>> leaderboardNamesAndDisplayNames;
    private final Map<String, LeaderboardSettings> leaderboardNamesAndSettings;

    private TabPanel leaderboardsTabPanel;
    private Label leaderboardsLabel;
    private final String metaLeaderboardName;
    
    public MultiLeaderboardPanel(SailingServiceAsync sailingService, String metaLeaderboardName, AsyncActionsExecutor asyncActionsExecutor, Timer timer,
            String preselectedLeaderboardName, RaceIdentifier preselectedRace, 
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
        this.selectedLeaderboardName = preselectedLeaderboardName;
        
        selectedLeaderboardFlowPanel = null;
        selectedLeaderboardPanel = null;
        leaderboardNamesAndDisplayNames = new ArrayList<Pair<String, String>>();
        leaderboardNamesAndSettings = new HashMap<String, LeaderboardSettings>();
    }

    private LeaderboardSettings getOrCreateLeaderboardSettings(String leaderboardName, LeaderboardSettings currentLeaderboardSettings) {
        LeaderboardSettings newLeaderboardSettings = leaderboardNamesAndSettings.get(leaderboardName);
        if(newLeaderboardSettings == null) {
            newLeaderboardSettings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null, null, false);
        }
        if(currentLeaderboardSettings != null) {
            newLeaderboardSettings = LeaderboardSettingsFactory.getInstance().mergeLeaderboardSettings(newLeaderboardSettings, currentLeaderboardSettings);
        }
        leaderboardNamesAndSettings.put(leaderboardName, newLeaderboardSettings);
            
        return newLeaderboardSettings;
    }
    
    @Override
    public Widget createWidget() {
        mainPanel = new VerticalPanel();
        
        leaderboardsLabel = new Label(stringMessages.regattaLeaderboards());
        leaderboardsLabel.setVisible(false);
        leaderboardsLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        leaderboardsLabel.getElement().getStyle().setMargin(5, Unit.PX);
        mainPanel.add(leaderboardsLabel);

        leaderboardsTabPanel = new TabPanel();
        leaderboardsTabPanel.setVisible(false);
        leaderboardsTabPanel.setAnimationEnabled(false);
        leaderboardsTabPanel.setWidth("100%");
        leaderboardsTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                Integer tabIndex = event.getSelectedItem();
                if(tabIndex >= 0) {
                    updateSelectedLeaderboard(leaderboardNamesAndDisplayNames.get(tabIndex).getA(), tabIndex);
                }
            }
        });
        
        mainPanel.add(leaderboardsTabPanel);
        
        updateLeaderboardSelection();

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
        return selectedLeaderboardPanel.getSettingsDialogComponent();
    }

    @Override
    public void updateSettings(LeaderboardSettings newSettings) {
        leaderboardNamesAndSettings.put(selectedLeaderboardName, newSettings);
        selectedLeaderboardPanel.updateSettings(newSettings);
    }

    public void setLeaderboardNames(List<Pair<String, String>> newLeaderboardNamesAndDisplayNames) {
        leaderboardNamesAndDisplayNames.clear();
        leaderboardNamesAndDisplayNames.addAll(newLeaderboardNamesAndDisplayNames);
        
        updateLeaderboardSelection();
    }

    private void readAndUpdateLeaderboardsOfMetaleaderboard() {
        sailingService.getLeaderboardsNamesOfMetaleaderboard(metaLeaderboardName, new AsyncCallback<List<Pair<String, String>>>() {
            
            @Override
            public void onSuccess(List<Pair<String, String>> leaderboardNamesAndDisplayNames) {
                setLeaderboardNames(leaderboardNamesAndDisplayNames);
            }
            
            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }
    
    private void updateLeaderboardSelection() {
        if(leaderboardsTabPanel != null) {
            leaderboardsTabPanel.clear();
            
            int index = 0;
            int leaderboardCount = leaderboardNamesAndDisplayNames.size();
            for (Pair<String, String> leaderboardNameAndDisplayName : leaderboardNamesAndDisplayNames) {
                FlowPanel tabFlowPanel = new FlowPanel();
                leaderboardsTabPanel.add(tabFlowPanel, leaderboardNameAndDisplayName.getB(), false);

                if(selectedLeaderboardName != null && selectedLeaderboardName.equals(leaderboardNameAndDisplayName.getA())) {
                    leaderboardsTabPanel.selectTab(index);
                }
                index++;
            }
            // show the last leaderboard when no leaderboard is selected yet 
            if(selectedLeaderboardName == null && leaderboardCount > 0) {
                leaderboardsTabPanel.selectTab(leaderboardCount-1);
            }
            
            leaderboardsTabPanel.setVisible(leaderboardCount > 0);
            leaderboardsLabel.setVisible(leaderboardCount > 0);
        }
    }

    private void updateSelectedLeaderboard(String newSelectedLeaderboardName, int newTabIndex) {
        if(newSelectedLeaderboardName != null) {
            if(selectedLeaderboardPanel != null && selectedLeaderboardFlowPanel != null) {
                selectedLeaderboardPanel.removeAllListeners();
                selectedLeaderboardFlowPanel.remove(selectedLeaderboardPanel);
                selectedLeaderboardPanel = null;
                selectedLeaderboardFlowPanel = null;
            }
            
            selectedLeaderboardFlowPanel = (FlowPanel) leaderboardsTabPanel.getWidget(newTabIndex);
            LeaderboardSettings newLeaderboardSettings = getOrCreateLeaderboardSettings(newSelectedLeaderboardName, leaderboardNamesAndSettings.get(selectedLeaderboardName));
            selectedLeaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor, newLeaderboardSettings,
                    /* preselectedRace*/ null, new CompetitorSelectionModel(true), timer,
                    null, newSelectedLeaderboardName, errorReporter, stringMessages, userAgent,
                    showRaceDetails, /* raceTimesInfoProvider */null, false,  /* adjustTimerDelay */ true);
            selectedLeaderboardFlowPanel.add(selectedLeaderboardPanel);
        } else {
            if(selectedLeaderboardPanel != null && selectedLeaderboardFlowPanel != null) {
                selectedLeaderboardPanel.removeAllListeners();
                selectedLeaderboardFlowPanel.remove(selectedLeaderboardPanel);
                selectedLeaderboardPanel = null;
                selectedLeaderboardFlowPanel = null;
            }
        }
        this.selectedLeaderboardName = newSelectedLeaderboardName;
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
    public void timeChanged(Date newTime, Date oldTime) {
        if(selectedLeaderboardPanel != null) {
            selectedLeaderboardPanel.timeChanged(newTime, oldTime);
        }
    }

}
