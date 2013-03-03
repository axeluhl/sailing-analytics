package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class RegattaLeaderboardCreateDialog extends RegattaLeaderboardDialog {
    
    public RegattaLeaderboardCreateDialog(Collection<StrippedLeaderboardDTO> existingLeaderboards, Collection<RegattaDTO> existingRegattas, StringMessages stringConstants,
            ErrorReporter errorReporter, DialogCallback<LeaderboardDescriptor> callback) {
        super(stringConstants.createRegattaLeaderboard(), new LeaderboardDescriptor(), existingRegattas, stringConstants, errorReporter, new RegattaLeaderboardDialog.LeaderboardParameterValidator(stringConstants, existingLeaderboards), callback);

        nameTextBox = createTextBox(null);
        displayNameTextBox = createTextBox(null);

        // the name of the regatta leaderboard will be derived from the selected regatta
        nameTextBox.setEnabled(false);
        nameTextBox.setVisibleLength(50);
        displayNameTextBox.setVisibleLength(50);

        regattaListBox = createSortedRegattaListBox(existingRegattas, null);
        regattaListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                int selectedIndex = regattaListBox.getSelectedIndex();
                if (selectedIndex > 0) {
                    nameTextBox.setText(regattaListBox.getValue(selectedIndex)); 
                }
            }
        });
        
        discardThresholdBoxes = initEmptyDiscardThresholdBoxes(this);
    }

}
