package com.sap.sailing.gwt.ui.polarmining;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.polars.datamining.shared.PolarDataMiningSettings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;

/**
 * Allows validation of {@link PolarDataMiningSettings}, such that confidence values are not <0 or >1 and so on.
 * 
 * @author D054528 (Frederik Petersen)
 *
 */
public class PolarDataMiningSettingsValidator implements Validator<PolarDataMiningSettings> {

    private final StringMessages stringMessages;

    public PolarDataMiningSettingsValidator(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public String getErrorMessage(PolarDataMiningSettings valueToValidate) {
        String errorMessage = null;
        if (oneOrMoreParametersAreNull(valueToValidate)) {
            errorMessage = stringMessages.oneOrMoreParametersAreEmpty();
        } else {
            errorMessage = validateConfidenceValues(valueToValidate, errorMessage);
            errorMessage = validateMinDataValues(valueToValidate, errorMessage);
            errorMessage = validateNumberOfColumns(valueToValidate.getNumberOfHistogramColumns(), errorMessage);
        }
        return errorMessage;
    }

    private boolean oneOrMoreParametersAreNull(PolarDataMiningSettings valueToValidate) {
        boolean oneOrMoreParametersAreNull = false;
        if (valueToValidate.getMinimumDataCountPerAngle() == null
                || valueToValidate.getMinimumDataCountPerGraph() == null) {
            oneOrMoreParametersAreNull = true;
        }
        return oneOrMoreParametersAreNull;
    }

    private String validateNumberOfColumns(int numberOfHistogramColumns, String errorMessage) {
        if (numberOfHistogramColumns < 2) {
            String numberOfColumnsAtLeast2 = stringMessages.numberOfColumnsAtLeast2();
            if (errorMessage == null) {
                errorMessage = numberOfColumnsAtLeast2;
            } else {
                errorMessage = errorMessage + "\n" + numberOfColumnsAtLeast2;
            }
        }

        return errorMessage;
    }

    private String validateMinDataValues(PolarDataMiningSettings valueToValidate, String errorMessage) {
        Integer minDataPerAngle = valueToValidate.getMinimumDataCountPerAngle();
        Integer minDataPerGraph = valueToValidate.getMinimumDataCountPerGraph();
        if (minDataPerAngle < 0 || minDataPerGraph < 0) {
            String minDataValuesNeedToBeAtLeastZero = stringMessages.minDataValuesNeedToBeAtLeastZero();
            if (errorMessage == null) {
                errorMessage = minDataValuesNeedToBeAtLeastZero;
            } else {
                errorMessage = errorMessage + "\n" + minDataValuesNeedToBeAtLeastZero;
            }
        }
        return errorMessage;
    }

    private String validateConfidenceValues(PolarDataMiningSettings valueToValidate, String errorMessage) {
        double minimumWindConfidence = valueToValidate.getMinimumWindConfidence();
        if (minimumWindConfidence < 0 || minimumWindConfidence > 1) {
            errorMessage = stringMessages.confidenceShouldBeBetween();
        }
        return errorMessage;
    }

}
