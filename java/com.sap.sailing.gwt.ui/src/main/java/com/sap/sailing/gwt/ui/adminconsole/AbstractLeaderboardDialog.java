package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;

public abstract class AbstractLeaderboardDialog extends DataEntryDialog<LeaderboardDescriptor> {
    protected final StringMessages stringMessages;
    protected TextBox nameTextBox;
    protected LeaderboardDescriptor leaderboard;
    
    protected LongBox[] discardThresholdBoxes;
    protected static final int MAX_NUMBER_OF_DISCARDED_RESULTS = 4;

    public AbstractLeaderboardDialog(String title, LeaderboardDescriptor leaderboardDTO, StringMessages stringConstants,
            Validator<LeaderboardDescriptor> validator,  DialogCallback<LeaderboardDescriptor> callback) {
        super(title, null, stringConstants.ok(), stringConstants.cancel(), validator, callback);
        this.stringMessages = stringConstants;
        this.leaderboard = leaderboardDTO;
    }
    
    @Override
    protected LeaderboardDescriptor getResult() {
        int[] discardThresholdsBoxContents = getDiscardThresholds(discardThresholdBoxes);
        leaderboard.setName(nameTextBox.getValue());
        leaderboard.setDiscardThresholds(discardThresholdsBoxContents);
        return leaderboard;
    }

    protected static int[] getDiscardThresholds(LongBox[] discardThresholdBoxes) {
        List<Integer> discardThresholds = new ArrayList<Integer>();
        // go backwards; starting from first non-zero element, add them; take over leading zeroes which validator shall discard
        for (int i = discardThresholdBoxes.length-1; i>=0; i--) {
            if ((discardThresholdBoxes[i].getValue() != null
                    && discardThresholdBoxes[i].getValue().toString().length() > 0) || !discardThresholds.isEmpty()) {
                if (discardThresholdBoxes[i].getValue() == null) {
                    discardThresholds.add(0, 0);
                } else {
                    discardThresholds.add(0, discardThresholdBoxes[i].getValue().intValue());
                }
            }
        }
        int[] discardThresholdsBoxContents = new int[discardThresholds.size()];
        for (int i = 0; i < discardThresholds.size(); i++) {
            discardThresholdsBoxContents[i] = discardThresholds.get(i);
        }
        return discardThresholdsBoxContents;
    }

    @Override
    public void show() {
        super.show();
        nameTextBox.setFocus(true);
    }

    protected static LongBox[] initEmptyDiscardThresholdBoxes(DataEntryDialog<?> dialog) {
        LongBox[] result = new LongBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i = 0; i < result.length; i++) {
            result[i] = dialog.createLongBoxWithOptionalValue(null, 2);
            result[i].setVisibleLength(2);
        }
        return result;
    }

    protected static LongBox[] initPrefilledDiscardThresholdBoxes(int[] valuesToShow, DataEntryDialog<?> dialog) {
        LongBox[] result = new LongBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i = 0; i < result.length; i++) {
            if (i < valuesToShow.length) {
                result[i] = dialog.createLongBox(valuesToShow[i], 2);
            } else {
                result[i] = dialog.createLongBoxWithOptionalValue(null, 2);
            }
            result[i].setVisibleLength(2);
        }
        return result;
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
