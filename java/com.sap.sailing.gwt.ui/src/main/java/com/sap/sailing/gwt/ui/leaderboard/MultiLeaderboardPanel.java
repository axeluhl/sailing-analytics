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

    private LeaderboardPanel selectedLeaderboardPanel;

    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final SailingServiceAsync sailingService;

    private final String preselectedLeaderboardName;
    private String selectedLeaderboardName;
    
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final UserAgentDetails userAgent;
    private final boolean showRaceDetails;
    private final Timer timer;

    private VerticalPanel mainPanel;
    private final List<Pair<String, String>> leaderboardNamesAndDisplayNames;
    private ListBox leaderboardSelectionListBox;

    public MultiLeaderboardPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, Timer timer,
            LeaderboardSettings leaderboardSettings, String preselectedLeaderboardName, RaceIdentifier preselectedRace, 
            ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, boolean showRaceDetails, boolean autoExpandLastRaceColumn) {
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.userAgent = userAgent;
        this.showRaceDetails = showRaceDetails;
        this.timer = timer;
        this.preselectedLeaderboardName = preselectedLeaderboardName;
        
        leaderboardNamesAndDisplayNames = new ArrayList<Pair<String, String>>();
    }

    @Override
    public Widget createWidget() {
        mainPanel = new VerticalPanel();

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
        
        updateLeaderboardSelection();

        return mainPanel;
    }

    @Override
    public String getLocalizedShortName() {
        return "Leaderboards";
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<LeaderboardSettings> getSettingsDialogComponent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateSettings(LeaderboardSettings newSettings) {
        // TODO Auto-generated method stub
        
    }

    public void setLeaderboardNames(List<Pair<String, String>> newLeaderboardNamesAndDisplayNames) {
        leaderboardNamesAndDisplayNames.clear();
        leaderboardNamesAndDisplayNames.addAll(newLeaderboardNamesAndDisplayNames);
        
        updateLeaderboardSelection();
    }

    private void updateLeaderboardSelection() {
        if(leaderboardSelectionListBox != null) {
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
    }

    private void updateSelectedLeaderboard(String selectedLeaderboardName) {
        if(selectedLeaderboardName != null) {
            if(selectedLeaderboardPanel == null) {
                LeaderboardSettings newDefaultSettings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null, null, false);
                selectedLeaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                        newDefaultSettings, /* preselectedRace*/ null, new CompetitorSelectionModel(true), timer,
                        "leaderboardGroupName", selectedLeaderboardName, errorReporter, stringMessages, userAgent,
                        showRaceDetails, /* raceTimesInfoProvider */null, false);              
            }
        } else {
            if(selectedLeaderboardPanel != null) {
                remove(selectedLeaderboardPanel);
                selectedLeaderboardPanel = null;
            }
        }
    }

    @Override
    public void timeChanged(Date date) {
        if(selectedLeaderboardPanel != null) {
            selectedLeaderboardPanel.timeChanged(date);
        }
    }

}
