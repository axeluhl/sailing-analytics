package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RegattaLeaderboardCreateDialog extends RegattaLeaderboardDialog {

    public RegattaLeaderboardCreateDialog(Collection<StrippedLeaderboardDTO> existingLeaderboards,
            Collection<RegattaDTO> existingRegattas, StringMessages stringMessages, ErrorReporter errorReporter,
            DialogCallback<LeaderboardDescriptor> callback) {
        super(stringMessages.createRegattaLeaderboard(), new LeaderboardDescriptor(), existingRegattas, stringMessages,
                errorReporter, new RegattaLeaderboardDialog.LeaderboardParameterValidator(stringMessages, existingLeaderboards),
                callback);
        nameTextBox = createTextBox(null);
        nameTextBox.ensureDebugId("NameTextBox");
        nameTextBox.setVisibleLength(50);
        // the name of the regatta leaderboard will be derived from the selected regatta
        nameTextBox.setEnabled(false);

        displayNameTextBox = createTextBox(null);
        displayNameTextBox.ensureDebugId("DisplayNameTextBox");
        displayNameTextBox.setVisibleLength(50);

        regattaListBox = createSortedRegattaListBox(existingRegattas, null);
        regattaListBox.ensureDebugId("RegattaListBox");
        regattaListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                int selectedIndex = regattaListBox.getSelectedIndex();
                if (selectedIndex > 0) {
                    nameTextBox.setText(regattaListBox.getValue(selectedIndex)); 
                }
                adjustVisibilityOfResultDiscardingRuleComponent();
            }
        });
        discardThresholdBoxes = new DiscardThresholdBoxes(this, stringMessages);
    }

}
