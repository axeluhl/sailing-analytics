package com.sap.sailing.landscape.ui.client;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.common.SharedLandscapeConstants;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * Allows the user to specify the parameters required for moving a replica set's master process to a different
 * instance.
 * <p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class MoveMasterProcessDialog extends DataEntryDialog<MoveMasterProcessDialog.MoveMasterToOtherInstanceInstructions> {
    
    public static class MoveMasterToOtherInstanceInstructions  {
        private final boolean sharedMasterInstance;
        private final String instanceTypeOrNull;
        private final Integer optionalMemoryInMegabytesOrNull;
        private final Integer optionalMemoryTotalSizeFactorOrNull;
        private final String masterReplicationBearerToken;
        private final String replicaReplicationBearerToken;
        
        public MoveMasterToOtherInstanceInstructions(boolean sharedMasterInstance,
                String instanceTypeOrNull,
                String masterReplicationBearerToken, String replicaReplicationBearerToken, Integer optionalMemoryInMegabytesOrNull,
                Integer optionalMemoryTotalSizeFactorOrNull) {
            this.sharedMasterInstance = sharedMasterInstance;
            this.instanceTypeOrNull = instanceTypeOrNull;
            this.optionalMemoryInMegabytesOrNull = optionalMemoryInMegabytesOrNull;
            this.optionalMemoryTotalSizeFactorOrNull = optionalMemoryTotalSizeFactorOrNull;
            this.masterReplicationBearerToken = masterReplicationBearerToken;
            this.replicaReplicationBearerToken = replicaReplicationBearerToken;
        }
        public boolean isSharedMasterInstance() {
            return sharedMasterInstance;
        }
        public String getDedicatedInstanceType() {
            return instanceTypeOrNull;
        }
        public Integer getOptionalMemoryInMegabytesOrNull() {
            return optionalMemoryInMegabytesOrNull;
        }
        public Integer getOptionalMemoryTotalSizeFactorOrNull() {
            return optionalMemoryTotalSizeFactorOrNull;
        }
        public String getInstanceTypeOrNull() {
            return instanceTypeOrNull;
        }
        public String getMasterReplicationBearerToken() {
            return masterReplicationBearerToken;
        }
        public String getReplicaReplicationBearerToken() {
            return replicaReplicationBearerToken;
        }
    }
    
    private final StringMessages stringMessages;
    private final CheckBox sharedMasterInstanceBox;
    private final ListBox instanceTypeListBox;
    private final Label instanceTypeLabel;
    private final TextBox masterReplicationBearerTokenBox;
    private final TextBox replicaReplicationBearerTokenBox;
    private final IntegerBox memoryInMegabytesBox;
    private final IntegerBox memoryTotalSizeFactorBox;
    private boolean memoryAsFactorToTotalMemoryAdjusted;

    public MoveMasterProcessDialog(LandscapeManagementWriteServiceAsync landscapeManagementService,
            StringMessages stringMessages, ErrorReporter errorReporter, DialogCallback<MoveMasterToOtherInstanceInstructions> callback) {
        super(stringMessages.moveMasterToOtherInstance(), /* message */ null, stringMessages.ok(), stringMessages.cancel(), /* validator */ null, callback);
        this.stringMessages = stringMessages;
        instanceTypeListBox = LandscapeDialogUtil.createInstanceTypeListBox(this, landscapeManagementService,
                stringMessages, SharedLandscapeConstants.DEFAULT_DEDICATED_INSTANCE_TYPE_NAME, errorReporter);
        instanceTypeLabel = new Label();
        masterReplicationBearerTokenBox = createTextBox("", 40);
        replicaReplicationBearerTokenBox = createTextBox("", 40);
        memoryInMegabytesBox = createIntegerBox(null, 7);
        memoryTotalSizeFactorBox = createIntegerBox(null, 2);
        memoryTotalSizeFactorBox.addValueChangeHandler(e->memoryAsFactorToTotalMemoryAdjusted=true);
        memoryInMegabytesBox.addValueChangeHandler(e->memoryTotalSizeFactorBox.setEnabled(e.getValue() == null));
        sharedMasterInstanceBox = createCheckbox(stringMessages.sharedMasterInstance());
        sharedMasterInstanceBox.addValueChangeHandler(e->updateInstanceTypeBasedOnSharedMasterInstanceBox());
        updateInstanceTypeBasedOnSharedMasterInstanceBox();
    }
    
    private void updateInstanceTypeBasedOnSharedMasterInstanceBox() {
        instanceTypeLabel.setText(sharedMasterInstanceBox.getValue() ? stringMessages.sharedMasterInstanceType() : stringMessages.dedicatedInstanceType());
        LandscapeDialogUtil.selectInstanceType(instanceTypeListBox,
                sharedMasterInstanceBox.getValue() ? SharedLandscapeConstants.DEFAULT_SHARED_INSTANCE_TYPE_NAME : SharedLandscapeConstants.DEFAULT_DEDICATED_INSTANCE_TYPE_NAME);
        if (!memoryAsFactorToTotalMemoryAdjusted) {
            if (sharedMasterInstanceBox.getValue()) {
                memoryTotalSizeFactorBox.setValue(SharedLandscapeConstants.DEFAULT_NUMBER_OF_PROCESSES_IN_MEMORY);
            } else {
                memoryTotalSizeFactorBox.setText("");
            }
        }
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(6, 2);
        int row=0;
        result.setWidget(row, 0, new Label(stringMessages.sharedMasterInstance()));
        result.setWidget(row++, 1, sharedMasterInstanceBox);
        result.setWidget(row, 0, instanceTypeLabel);
        result.setWidget(row++, 1, instanceTypeListBox);
        result.setWidget(row, 0, new Label(stringMessages.bearerTokenForSecurityReplication()));
        result.setWidget(row++, 1, masterReplicationBearerTokenBox);
        result.setWidget(row, 0, new Label(stringMessages.replicaReplicationBearerToken()));
        result.setWidget(row++, 1, replicaReplicationBearerTokenBox);
        result.setWidget(row, 0, new Label(stringMessages.memoryInMegabytes()));
        result.setWidget(row++, 1, memoryInMegabytesBox);
        result.setWidget(row, 0, new Label(stringMessages.memoryTotalSizeFactor()));
        result.setWidget(row++, 1, memoryTotalSizeFactorBox);
        return result;
    }

    @Override
    protected MoveMasterToOtherInstanceInstructions getResult() {
        return new MoveMasterToOtherInstanceInstructions(sharedMasterInstanceBox.getValue(),
                instanceTypeListBox.getSelectedValue(),
                masterReplicationBearerTokenBox.getValue(), replicaReplicationBearerTokenBox.getValue(),
                memoryInMegabytesBox.getValue(), memoryTotalSizeFactorBox.getValue());
    }
}
