package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
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
    private ListBox leaderboardSelectionListBox;

    private LeaderboardSettings selectedActLeaderboardSettings; 
    
    public MultiLeaderboardPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, Timer timer,
            LeaderboardSettings leaderboardSettings, String preselectedActLeaderboardName, RaceIdentifier preselectedRace, 
            ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, boolean showRaceDetails, boolean autoExpandLastRaceColumn) {
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.userAgent = userAgent;
        this.showRaceDetails = showRaceDetails;
        this.timer = timer;
        this.selectedActLeaderboardName = preselectedActLeaderboardName;
        
        actLeaderboardNamesAndDisplayNames = new ArrayList<Pair<String, String>>();
        selectedActLeaderboardSettings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null, null, false);
    }

    @Override
    public Widget createWidget() {
        mainPanel = new VerticalPanel();

        leaderboardSelectionListBox = new ListBox();

        leaderboardSelectionListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selIndex = leaderboardSelectionListBox.getSelectedIndex();
                if(selIndex >= 0) {
                    updateSelectedLeaderboard(leaderboardSelectionListBox.getValue(selIndex));
                }
            }
        });
        leaderboardSelectionListBox.setVisible(false);
        mainPanel.add(leaderboardSelectionListBox);
        
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

    private void updateActLeaderboardSelection() {
        if(leaderboardSelectionListBox != null) {
            leaderboardSelectionListBox.clear();
            
            int index = 0;
            for (Pair<String, String> leaderboardNameAndDisplayName : actLeaderboardNamesAndDisplayNames) {
                leaderboardSelectionListBox.addItem(leaderboardNameAndDisplayName.getB(), leaderboardNameAndDisplayName.getA());

                if(selectedActLeaderboardName != null && selectedActLeaderboardName.equals(leaderboardNameAndDisplayName.getA())) {
                    leaderboardSelectionListBox.setSelectedIndex(index);
                }
                index++;
            }
            
            leaderboardSelectionListBox.setVisible(actLeaderboardNamesAndDisplayNames.size() > 0);
        }
    }

    private void updateSelectedLeaderboard(String selectedLeaderboardName) {
        if(selectedLeaderboardName != null) {
            if(selectedActLeaderboardPanel != null) {
                mainPanel.remove(selectedActLeaderboardPanel);
                selectedActLeaderboardPanel = null;
            }
            
            selectedActLeaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                    selectedActLeaderboardSettings, /* preselectedRace*/ null, new CompetitorSelectionModel(true), timer,
                    null, selectedLeaderboardName, errorReporter, stringMessages, userAgent,
                    showRaceDetails, /* raceTimesInfoProvider */null, false,  /* adjustTimerDelay */ true);
            mainPanel.add(selectedActLeaderboardPanel);
        } else {
            if(selectedActLeaderboardPanel != null) {
                mainPanel.remove(selectedActLeaderboardPanel);
                selectedActLeaderboardPanel = null;
            }
        }
    }

    @Override
    public void timeChanged(Date date) {
        if(selectedActLeaderboardPanel != null) {
            selectedActLeaderboardPanel.timeChanged(date);
        }
    }

}
