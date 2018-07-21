package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class AbstractLeaderboardDialog<LD extends LeaderboardDescriptor> extends DataEntryDialog<LD> {
    protected final StringMessages stringMessages;
    protected TextBox nameTextBox;
    protected TextBox displayNameTextBox;
    protected LD leaderboardDescriptor;

    protected DiscardThresholdBoxes discardThresholdBoxes;

    public AbstractLeaderboardDialog(String title, LD leaderboardDescriptor, StringMessages stringMessages,
            Validator<LD> validator,  DialogCallback<LD> callback) {
        super(title, null, stringMessages.ok(), stringMessages.cancel(), validator, callback);
        this.stringMessages = stringMessages;
        this.leaderboardDescriptor = leaderboardDescriptor;
    }

    @Override
    protected LD getResult() {
        leaderboardDescriptor.setName(nameTextBox.getValue().trim()); // avoid trailing blank issues; leaderboard names may appear in URLs
        leaderboardDescriptor.setDisplayName(displayNameTextBox.getValue().trim().isEmpty() ? null : displayNameTextBox.getValue());
        leaderboardDescriptor.setDiscardThresholds(discardThresholdBoxes==null?null:discardThresholdBoxes.getDiscardThresholds());
        return leaderboardDescriptor;
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return nameTextBox;
    }

    protected static ListBox createScoringSchemeListBox(DataEntryDialog<?> dialog, StringMessages stringMessages) {
        ListBox scoringSchemeListBox2 = dialog.createListBox(false);
        for (ScoringSchemeType scoringSchemeType: ScoringSchemeType.values()) {
            scoringSchemeListBox2.addItem(ScoringSchemeTypeFormatter.format(scoringSchemeType, stringMessages));
        }
        return scoringSchemeListBox2;
    }

    protected static ScoringSchemeType getSelectedScoringSchemeType(ListBox scoringSchemeListBox, StringMessages stringMessages) {
        ScoringSchemeType result = null;
        int selIndex = scoringSchemeListBox.getSelectedIndex();
        if (selIndex >= 0) { 
            String itemText = scoringSchemeListBox.getItemText(selIndex);
            for (ScoringSchemeType scoringSchemeType : ScoringSchemeType.values()) {
                if (ScoringSchemeTypeFormatter.format(scoringSchemeType, stringMessages).equals(itemText)) {
                    result = scoringSchemeType;
                    break;
                }
            }
        }
        return result;
    }
}
