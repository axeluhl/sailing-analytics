package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RegattaLeaderboardWithEliminationsCreateDialog extends RegattaLeaderboardWithEliminationsDialog {

    public RegattaLeaderboardWithEliminationsCreateDialog(Collection<StrippedLeaderboardDTO> existingLeaderboards,
            Collection<RegattaDTO> existingRegattas, StringMessages stringMessages, ErrorReporter errorReporter,
            DialogCallback<LeaderboardDescriptor> callback) {
        super(stringMessages.createRegattaLeaderboard(), new LeaderboardDescriptor(), existingRegattas, stringMessages,
                errorReporter, new RegattaLeaderboardWithEliminationsDialog.LeaderboardParameterValidator(stringMessages, existingLeaderboards),
                callback);
        nameTextBox = createTextBox(null);
        nameTextBox.ensureDebugId("NameTextBox");
        nameTextBox.setVisibleLength(50);

        displayNameTextBox = createTextBox(null);
        displayNameTextBox.ensureDebugId("DisplayNameTextBox");
        displayNameTextBox.setVisibleLength(50);

        regattaLeaderboardsListBox = createSortedRegattaLeaderboardsListBox(existingLeaderboards, null);
        regattaLeaderboardsListBox.ensureDebugId("RegattaListBox");
        regattaLeaderboardsListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                int selectedIndex = regattaLeaderboardsListBox.getSelectedIndex();
                if (selectedIndex > 0) {
                    nameTextBox.setText(regattaLeaderboardsListBox.getValue(selectedIndex)/*+" (2)"*/);
                    validateAndUpdate();
                }
            }
        });
    }
}
