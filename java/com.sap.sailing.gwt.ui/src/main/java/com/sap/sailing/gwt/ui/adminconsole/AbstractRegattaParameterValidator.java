package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;

public abstract class AbstractRegattaParameterValidator implements Validator<RegattaDTO> {
    protected final StringMessages stringMessages;

    public AbstractRegattaParameterValidator(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public String getErrorMessage(RegattaDTO regattaToValidate) {
        String errorMessage = null;
        if (regattaToValidate.buoyZoneRadiusInHullLengths == null) {
            errorMessage = stringMessages.incorrectValueForRegattaBuoyZoneRadiusInHullLengths();
        } else if (regattaToValidate.useStartTimeInference
                && regattaToValidate.controlTrackingFromStartAndFinishTimes) {
            errorMessage = stringMessages.useOnlyOneOfStartTimeInferenceAndControlTrackingFromStartAndFinishTimes();
        }
        return errorMessage;
    }
}
