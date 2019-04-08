package com.sap.sailing.gwt.ui.polarmining;

import com.sap.sailing.datamining.shared.FoilingSegmentsDataMiningSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;

/**
 * Allows validation of {@link FoilingSegmentsDataMiningSettings}
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class FoilingSegmentsDataMiningSettingsValidator implements Validator<FoilingSegmentsDataMiningSettings> {

    private final StringMessages stringMessages;

    public FoilingSegmentsDataMiningSettingsValidator(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public String getErrorMessage(FoilingSegmentsDataMiningSettings valueToValidate) {
        String errorMessage = null;
        if (valueToValidate.getMinimumRideHeight() == null) {
            errorMessage = stringMessages.needToProvideValidMinimumRideHeight();
        }
        // TODO add validation rules for FoilingSegmentsDataMiningSettings
        return errorMessage;
    }
}
