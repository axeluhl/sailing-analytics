package com.sap.sailing.landscape.ui.client;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.common.SharedLandscapeConstants;
import com.sap.sailing.landscape.ui.client.SwitchToReplicaOnSharedInstanceDialog.SwitchToReplicaOnSharedInstanceDialogInstructions;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SwitchToReplicaOnSharedInstanceDialog extends DataEntryDialog<SwitchToReplicaOnSharedInstanceDialogInstructions> {
    public static class SwitchToReplicaOnSharedInstanceDialogInstructions {
        private final String replicaReplicationBearerToken;
        private final String optionalSharedReplicaInstanceType;
        private final Integer optionalMemoryInMegabytesOrNull;
        private final Integer optionalMemoryTotalSizeFactorOrNull;
        public SwitchToReplicaOnSharedInstanceDialogInstructions(String replicaReplicationBearerToken,
                String optionalSharedReplicaInstanceType, Integer optionalMemoryInMegabytesOrNull,
                Integer optionalMemoryTotalSizeFactorOrNull) {
            super();
            this.replicaReplicationBearerToken = replicaReplicationBearerToken;
            this.optionalSharedReplicaInstanceType = optionalSharedReplicaInstanceType;
            this.optionalMemoryInMegabytesOrNull = optionalMemoryInMegabytesOrNull;
            this.optionalMemoryTotalSizeFactorOrNull = optionalMemoryTotalSizeFactorOrNull;
        }
        public String getReplicaReplicationBearerToken() {
            return replicaReplicationBearerToken;
        }
        public Integer getOptionalMemoryInMegabytesOrNull() {
            return optionalMemoryInMegabytesOrNull;
        }
        public Integer getOptionalMemoryTotalSizeFactorOrNull() {
            return optionalMemoryTotalSizeFactorOrNull;
        }
        public String getOptionalSharedReplicaInstanceType() {
            return optionalSharedReplicaInstanceType;
        }
    }

    private final ListBox sharedInstanceTypeListBox;
    private final IntegerBox memoryInMegabytesBox;
    private final IntegerBox memoryTotalSizeFactorBox;
    private final TextBox replicaReplicationBearerTokenBox;
    private final StringMessages stringMessages;
    private static final String DEFAULT_INSTANCE_TYPE = "___default___";

    public SwitchToReplicaOnSharedInstanceDialog(StringMessages stringMessages, ErrorReporter errorReporter,
            LandscapeManagementWriteServiceAsync landscapeManagementService,
            DialogCallback<SwitchToReplicaOnSharedInstanceDialogInstructions> callback) {
        super(stringMessages.switchToReplicaOnSharedInstance(), stringMessages.switchToReplicaOnSharedInstance(),
                stringMessages.ok(), stringMessages.cancel(), /* validator */ null, callback);
        this.stringMessages = stringMessages;
        sharedInstanceTypeListBox = LandscapeDialogUtil.createInstanceTypeListBoxWithAdditionalDefaultEntry(this,
                stringMessages.sameAsMaster(), DEFAULT_INSTANCE_TYPE,
                landscapeManagementService,
                stringMessages, SharedLandscapeConstants.DEFAULT_SHARED_INSTANCE_TYPE_NAME, errorReporter, /* canBeDeployedInNlbInstanceBasedTargetGroup */ false);
        replicaReplicationBearerTokenBox = createTextBox("", 40);
        memoryInMegabytesBox = createIntegerBox(null, 7);
        memoryTotalSizeFactorBox = createIntegerBox(SharedLandscapeConstants.DEFAULT_NUMBER_OF_PROCESSES_IN_MEMORY, 2);
        memoryInMegabytesBox.addValueChangeHandler(e->memoryTotalSizeFactorBox.setEnabled(e.getValue() == null));
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(4, 2);
        int row=0;
        result.setWidget(row, 0, new Label(stringMessages.sharedReplicaInstanceType()));
        result.setWidget(row++, 1, sharedInstanceTypeListBox);
        result.setWidget(row, 0, new Label(stringMessages.replicaReplicationBearerToken()));
        result.setWidget(row++, 1, replicaReplicationBearerTokenBox);
        result.setWidget(row, 0, new Label(stringMessages.memoryInMegabytes()));
        result.setWidget(row++, 1, memoryInMegabytesBox);
        result.setWidget(row, 0, new Label(stringMessages.memoryTotalSizeFactor()));
        result.setWidget(row++, 1, memoryTotalSizeFactorBox);
        return result;
    }

    @Override
    protected SwitchToReplicaOnSharedInstanceDialogInstructions getResult() {
        return new SwitchToReplicaOnSharedInstanceDialogInstructions(replicaReplicationBearerTokenBox.getText(),
                sharedInstanceTypeListBox.getSelectedValue().equals(DEFAULT_INSTANCE_TYPE) ? null : sharedInstanceTypeListBox.getSelectedValue(),
                        memoryInMegabytesBox.getValue(), memoryTotalSizeFactorBox.getValue());
    }

}
