package com.sap.sailing.gwt.ui.polarsheets;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.ui.DataEntryDialog.Validator;

public class PolarSheetGenerationSettingsValidator implements Validator<PolarSheetGenerationSettings> {
    
    private final StringMessages stringMessages;

    public PolarSheetGenerationSettingsValidator(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public String getErrorMessage(PolarSheetGenerationSettings valueToValidate) {
        String errorMessage = null;
        if (oneOrMoreParametersAreNull(valueToValidate)) {
            errorMessage = stringMessages.oneOrMoreParametersAreEmpty();
        } else {
            errorMessage = validateConfidenceValues(valueToValidate, errorMessage);
            errorMessage = validateMinDataValues(valueToValidate, errorMessage);
            errorMessage = validateNumberOfColumns(valueToValidate.getNumberOfHistogramColumns(), errorMessage);
            if (valueToValidate.shouldRemoveOutliers()) {
                errorMessage = validateOutlierRadius(valueToValidate.getOutlierDetectionNeighborhoodRadius(),
                        errorMessage);
                errorMessage = validateOutlierNeighborhoodPct(valueToValidate.getOutlierMinimumNeighborhoodPct(),
                        errorMessage);
            }
        }
        return errorMessage;
    }

    private boolean oneOrMoreParametersAreNull(PolarSheetGenerationSettings valueToValidate) {
        boolean oneOrMoreParametersAreNull = false;
        if (valueToValidate.getMinimumDataCountPerAngle() == null ||
                valueToValidate.getMinimumDataCountPerGraph() == null) {
            oneOrMoreParametersAreNull = true;
        }
        return oneOrMoreParametersAreNull;
    }

    private String validateOutlierNeighborhoodPct(double outlierMinimumNeighborhoodPct, String errorMessage) {
        if (outlierMinimumNeighborhoodPct <= 0 && outlierMinimumNeighborhoodPct > 1) {
            String outlierMinimumNeighborhoodPctString = stringMessages.outlierMinimumNeighborhoodPctString();
            if (errorMessage == null) {
                errorMessage = outlierMinimumNeighborhoodPctString;
            } else {
                errorMessage = errorMessage + "\n" + outlierMinimumNeighborhoodPctString;
            }
        }
        
        return errorMessage;
    }

    private String validateOutlierRadius(double outlierDetectionNeighborhoodRadius, String errorMessage) {
        if (outlierDetectionNeighborhoodRadius <= 0) {
            String outlierRadiusNeedsToBePositiveString = stringMessages.outlierRadiusNeedsToBePositiveString();
            if (errorMessage == null) {
                errorMessage = outlierRadiusNeedsToBePositiveString;
            } else {
                errorMessage = errorMessage + "\n" + outlierRadiusNeedsToBePositiveString;
            }
        }
        
        return errorMessage;
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

    private String validateMinDataValues(PolarSheetGenerationSettings valueToValidate, String errorMessage) {
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

    private String validateConfidenceValues(PolarSheetGenerationSettings valueToValidate, String errorMessage) {
        double minimumConfidenceMeasure = valueToValidate.getMinimumConfidenceMeasure();
        double minimumWindConfidence = valueToValidate.getMinimumWindConfidence();
        if (minimumConfidenceMeasure < 0 || minimumConfidenceMeasure > 1 ||
                minimumWindConfidence < 0 || minimumWindConfidence > 1) {
            errorMessage = stringMessages.confidenceShouldBeBetween();
        }
        return errorMessage;
    }

}
