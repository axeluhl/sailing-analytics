package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;

public class LeaderboardEditDialog extends DataEntryDialog<LeaderboardDAO> {
    private static final int MAX_NUMBER_OF_DISCARDED_RESULTS = 4;
    private final TextBox entryField;
    private final IntegerBox[] discardThresholdBoxes;
    private final StringConstants stringConstants;

    private final CreateOrEditMode mode;

    private enum CreateOrEditMode {
        CREATE, EDIT;
    }

    private LeaderboardDAO leaderboard;

    private static class LeaderboardParameterValidator implements Validator<LeaderboardDAO> {
        private final StringConstants stringConstants;
        private final Collection<String> existingLeaderboardNames;

        private final CreateOrEditMode mode;

        public LeaderboardParameterValidator(Collection<String> existingLeaderboardNames,
                StringConstants stringConstants, CreateOrEditMode mode) {
            super();
            this.existingLeaderboardNames = existingLeaderboardNames;
            this.stringConstants = stringConstants;
            this.mode = mode;
        }

        @Override
        public String getErrorMessage(LeaderboardDAO leaderboard) {
            String errorMessage;
            boolean nonEmpty = leaderboard.name != null && leaderboard.name.length() > 0;

            boolean discardThresholdsAscending = true;
            for (int i = 1; i < leaderboard.discardThresholds.length; i++) {
                // TODO what are correct values for discarding Thresholds?
                if (0 < leaderboard.discardThresholds.length){ 
                    discardThresholdsAscending = discardThresholdsAscending
                            && leaderboard.discardThresholds[i - 1] < leaderboard.discardThresholds[i];
                }
            }
            boolean unique = true;
            if (mode == CreateOrEditMode.CREATE) {
                unique = !existingLeaderboardNames.contains(leaderboard.name);
            }
            if (!nonEmpty) {
                errorMessage = stringConstants.pleaseEnterNonEmptyName();
            } else if (!unique) {
                errorMessage = stringConstants.leaderboardWithThisNameAlreadyExists();
            } else if (!discardThresholdsAscending) {
                errorMessage = stringConstants.discardThresholdsMustBeAscending();
            } else {
                errorMessage = null;
            }
            return errorMessage;
        }
    }

    public LeaderboardEditDialog(Collection<String> existingLeaderboardNames, StringConstants stringConstants,
            AsyncCallback<LeaderboardDAO> callback) {
        super(stringConstants.leaderboardName(), stringConstants.leaderboardName(), stringConstants.ok(),
                stringConstants.cancel(), new LeaderboardParameterValidator(existingLeaderboardNames, stringConstants,
                        CreateOrEditMode.CREATE), callback);
        this.leaderboard = new LeaderboardDAO();
        this.stringConstants = stringConstants;
        this.entryField = createTextBox(null);
        discardThresholdBoxes = new IntegerBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            discardThresholdBoxes[i] = createIntegerBoxWithOptionalValue(null, 2);
            discardThresholdBoxes[i].setVisibleLength(2);
        }
        mode = CreateOrEditMode.CREATE;
    }

    public LeaderboardEditDialog(LeaderboardDAO leaderboard, Collection<String> existingLeaderboardNames,
            StringConstants stringConstants, AsyncCallback<LeaderboardDAO> callback) {
        super(leaderboard.name, stringConstants.leaderboardName(), stringConstants.ok(), stringConstants.cancel(),
                new LeaderboardParameterValidator(existingLeaderboardNames, stringConstants, CreateOrEditMode.EDIT),
                callback);
        this.leaderboard = leaderboard;
        this.stringConstants = stringConstants;
        this.entryField = createTextBox(leaderboard.name);
        discardThresholdBoxes = new IntegerBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            if (i < leaderboard.discardThresholds.length) {
                discardThresholdBoxes[i] = createIntegerBox(leaderboard.discardThresholds[i], 2);
            } else {
                discardThresholdBoxes[i] = createIntegerBoxWithOptionalValue(null, 2);
            }
            discardThresholdBoxes[i].setVisibleLength(2);
        }
        mode = CreateOrEditMode.EDIT;
    }

    @Override
    protected LeaderboardDAO getResult() {
        List<Integer> discardThresholds = new ArrayList<Integer>();
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            if (discardThresholdBoxes[i].getValue() != null
                    && discardThresholdBoxes[i].getValue().toString().length() > 0) {
                discardThresholds.add(discardThresholdBoxes[i].getValue());
            }
        }
        int[] discardThresholdsBoxContents = new int[discardThresholds.size()];
        for (int i = 0; i < discardThresholds.size(); i++) {
            discardThresholdsBoxContents[i] = discardThresholds.get(i);
        }
        leaderboard.name = entryField.getValue();
        leaderboard.discardThresholds = discardThresholdsBoxContents;
        return leaderboard;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        panel.add(entryField);
        panel.add(new Label(stringConstants.discardRacesFromHowManyStartedRacesOn()));
        HorizontalPanel hp = new HorizontalPanel();
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            hp.add(new Label("" + (i + 1) + "."));
            hp.add(discardThresholdBoxes[i]);
        }
        panel.add(hp);
        return panel;
    }

    @Override
    public void show() {
        super.show();
        entryField.setFocus(true);
    }

    public CreateOrEditMode getMode() {
        return mode;
    }
}
