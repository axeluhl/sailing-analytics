package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class LinkRegattaLeaderboardToLeaderboardGroupOfEventDialog extends DataEntryDialog<LeaderboardGroupDTO> {
    private final List<LeaderboardGroupDTO> leaderboardGroups;
    private ListBox leaderboardGroupBox;
    
    public LinkRegattaLeaderboardToLeaderboardGroupOfEventDialog(SailingServiceAsync sailingService, final StringMessages stringMessages,
            ErrorReporter errorReporter, StrippedLeaderboardDTO leaderboard, EventDTO event, DialogCallback<LeaderboardGroupDTO> callback) {
        super(stringMessages.linkLeaderboardToLeaderboardGroupOfEvent(),
                /*message*/ stringMessages.doYouWantToLinkLeaderboardToLeaderboardGroupOfEvent(leaderboard.name, event.getName()),
                stringMessages.yes(), stringMessages.no(),
                /*validator*/ new Validator<LeaderboardGroupDTO>() {
                    @Override
                    public String getErrorMessage(LeaderboardGroupDTO selectedLeaderboardGroup) {
                        final String errorMessage;
                        if (selectedLeaderboardGroup == null) {
                            errorMessage = stringMessages.pleaseSelectALeaderboardGroup();
                        } else {
                            errorMessage = null;
                        }
                        return errorMessage;
                    }
                }, callback);
        this.leaderboardGroups = new ArrayList<>();
        leaderboardGroupBox = createListBox(/* isMultipleSelect */ false);
        for (final LeaderboardGroupDTO lgDTO : event.getLeaderboardGroups()) {
            this.leaderboardGroups.add(lgDTO);
            this.leaderboardGroupBox.addItem(lgDTO.getName());
        }
    }   

    @Override
    protected LeaderboardGroupDTO getResult() {
        final LeaderboardGroupDTO result;
        if (leaderboardGroupBox.getSelectedItemText() == null) {
            result = null;
        } else {
            result = leaderboardGroups.get(leaderboardGroupBox.getSelectedIndex());
        }
        return result;
    }

}
