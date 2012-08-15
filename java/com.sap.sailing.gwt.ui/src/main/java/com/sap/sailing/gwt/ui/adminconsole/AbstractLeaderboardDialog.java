package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class AbstractLeaderboardDialog extends DataEntryDialog<LeaderboardDescriptor> {
    protected final StringMessages stringConstants;
    protected TextBox nameTextBox;
    protected LeaderboardDescriptor leaderboard;
    
    protected LongBox[] discardThresholdBoxes;
    protected static final int MAX_NUMBER_OF_DISCARDED_RESULTS = 4;

    public AbstractLeaderboardDialog(String title, LeaderboardDescriptor leaderboardDTO, StringMessages stringConstants,
            Validator<LeaderboardDescriptor> validator,  AsyncCallback<LeaderboardDescriptor> callback) {
        super(title, null, stringConstants.ok(), stringConstants.cancel(), validator, callback);
        this.stringConstants = stringConstants;
        this.leaderboard = leaderboardDTO;
    }
    
    @Override
    protected LeaderboardDescriptor getResult() {
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
        leaderboard.name = nameTextBox.getValue();
        leaderboard.discardThresholds = discardThresholdsBoxContents;
        
        return leaderboard;
    }

    @Override
    public void show() {
        super.show();
        nameTextBox.setFocus(true);
    }    
}
