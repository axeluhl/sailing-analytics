package com.sap.sailing.landscape.ui.client;

import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.MongoScalingInstructionsDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.landscape.common.shared.MongoDBConstants;

public class MongoScalingDialog extends DataEntryDialog<MongoScalingInstructionsDTO> {
    private static class MongoScalingInstructionsValidator implements Validator<MongoScalingInstructionsDTO> {
        private final StringMessages stringMessages;
        
        public MongoScalingInstructionsValidator(StringMessages stringMessages) {
            super();
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(MongoScalingInstructionsDTO valueToValidate) {
            final String result;
            if (valueToValidate == null) {
                result = null;
            } else {
                final Iterable<Pair<String, Integer>> hostsAndPortsToShutdownOnNonDefaultPorts = Util.filter(valueToValidate.getHostnamesAndPortsToShutDown(),
                        hostnameAndPortToShutDown->hostnameAndPortToShutDown.getB() != MongoDBConstants.DEFAULT_PORT);
                if (!Util.isEmpty(hostsAndPortsToShutdownOnNonDefaultPorts)) {
                    result = stringMessages.youCannotShutdownMongoDBInstancesNotRunningOnDefaultPort(Util.joinStrings(", ", hostsAndPortsToShutdownOnNonDefaultPorts));
                } else {
                    result = null;
                }
            }
            return result;
        }
    }

    public MongoScalingDialog(StringMessages stringMessages,
            LandscapeManagementWriteServiceAsync landscapeManagementService,
            DialogCallback<MongoScalingInstructionsDTO> dialogCallback) {
        super(stringMessages.scale(), stringMessages.scale(), stringMessages.ok(), stringMessages.cancel(),
                /* validator */ new MongoScalingInstructionsValidator(stringMessages), /* animationEnabled */ true, dialogCallback);
    }

    @Override
    protected MongoScalingInstructionsDTO getResult() {
        // TODO Implement MongoScalingDialog.getResult(...)
        return null;
    }
}
