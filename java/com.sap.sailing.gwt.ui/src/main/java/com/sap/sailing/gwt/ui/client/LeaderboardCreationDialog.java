package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.Pair;

public class LeaderboardCreationDialog extends DataEntryDialog<Pair<String, String[]>> {
    private static final int MAX_NUMBER_OF_DISCARDED_RESULTS = 4;
    private final TextBox entryField;
    private final TextBox[] discardThresholdBoxes;
    private final ErrorReporter errorReporter;
    private final StringConstants stringConstants;
    
    private static class LeaderboardParameterValidator implements Validator<Pair<String, String[]>> {
        private final StringConstants stringConstants;
        private final Collection<String> existingLeaderboardNames;
        
        public LeaderboardParameterValidator(Collection<String> existingLeaderboardNames, StringConstants stringConstants) {
            super();
            this.existingLeaderboardNames = existingLeaderboardNames;
            this.stringConstants = stringConstants;
        }

        @Override
        public String getErrorMessage(Pair<String, String[]> valueToValidate) {
            String errorMessage;
            boolean nonEmpty = valueToValidate.getA() != null && valueToValidate.getA().trim().length() > 0;
            boolean unique = !existingLeaderboardNames.contains(valueToValidate.getA());
            boolean discardThresholdsAscending = true;
            boolean discardThresholdsAreNumeric = valueToValidate.getB().length == 0 || valueToValidate.getB()[0] == null ||
                    valueToValidate.getB()[0].matches("[0-9]*");
            for (int i=1; i<valueToValidate.getB().length; i++) {
                if (valueToValidate.getB()[i] != null && valueToValidate.getB()[i].trim().length() > 0) {
                    try {
                        discardThresholdsAscending = discardThresholdsAscending &&
                                valueToValidate.getB()[i-1] != null && valueToValidate.getB()[i-1].trim().length() > 0 &&
                                Integer.valueOf(valueToValidate.getB()[i-1].trim()) < Integer.valueOf(valueToValidate.getB()[i].trim());
                    } catch (NumberFormatException e) {
                        discardThresholdsAreNumeric = false;
                    }
                }
            }
            if (!nonEmpty) {
                errorMessage = stringConstants.pleaseEnterNonEmptyName();
            } else if (!unique) {
                errorMessage = stringConstants.leaderboardWithThisNameAlreadyExists();
            } else if (!discardThresholdsAreNumeric) {
                errorMessage = stringConstants.discardThresholdsMustBeNumeric();
            } else if (!discardThresholdsAscending) {
                errorMessage = stringConstants.discardThresholdsMustBeAscending();
            } else {
                errorMessage = null;
            }
            return errorMessage;
        }
    }

    public LeaderboardCreationDialog(Collection<String> existingLeaderboardNames, StringConstants stringConstants,
            ErrorReporter errorReporter, AsyncCallback<Pair<String, String[]>> callback) {
        super(stringConstants.leaderboardName(), stringConstants.leaderboardName(), stringConstants.ok(),
                stringConstants.cancel(), new LeaderboardParameterValidator(existingLeaderboardNames, stringConstants), callback);
        this.errorReporter = errorReporter;
        this.stringConstants = stringConstants;
        entryField = createTextBox(/* initial value */null);
        discardThresholdBoxes = new TextBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i=0; i<discardThresholdBoxes.length; i++) {
            discardThresholdBoxes[i] = createTextBox(/* initialValue */ null);
            discardThresholdBoxes[i].setVisibleLength(2);
        }
    }

    @Override
    protected Pair<String, String[]> getResult() {
        List<String> discardThresholds = new ArrayList<String>();
        for (int i=0; i<discardThresholdBoxes.length; i++) {
            if (discardThresholdBoxes[i].getValue() != null && discardThresholdBoxes[i].getValue().trim().length() > 0) {
                try {
                    Integer.valueOf(discardThresholdBoxes[i].getValue().trim()); // ensure there is no exception
                    discardThresholds.add(discardThresholdBoxes[i].getValue().trim());
                } catch (NumberFormatException e) {
                    errorReporter.reportError("Internal error; NumberFormatException for "+discardThresholdBoxes[i].getValue()+
                            " which should have been caught by validation before");
                }
            }
        }
        String[] discardThresholdBoxesContents = discardThresholds.toArray(new String[discardThresholds.size()]);
        return new Pair<String, String[]>(entryField.getValue(), discardThresholdBoxesContents);
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
        for (int i=0; i<discardThresholdBoxes.length; i++) {
            hp.add(new Label(""+(i+1)+"."));
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

}
