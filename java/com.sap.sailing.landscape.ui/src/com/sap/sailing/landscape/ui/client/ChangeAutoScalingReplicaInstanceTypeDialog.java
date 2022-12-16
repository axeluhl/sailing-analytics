package com.sap.sailing.landscape.ui.client;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.common.SharedLandscapeConstants;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * Allows the user to specify an instance type for auto-scaling replicas
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ChangeAutoScalingReplicaInstanceTypeDialog extends DataEntryDialog<String> {
    private final StringMessages stringMessages;
    private final ListBox instanceTypeListBox;

    public ChangeAutoScalingReplicaInstanceTypeDialog(LandscapeManagementWriteServiceAsync landscapeManagementService,
            StringMessages stringMessages, ErrorReporter errorReporter, DialogCallback<String> callback) {
        super(stringMessages.moveMasterToOtherInstance(), /* message */ null, stringMessages.ok(), stringMessages.cancel(), /* validator */ null, callback);
        this.stringMessages = stringMessages;
        instanceTypeListBox = LandscapeDialogUtil.createInstanceTypeListBox(this, landscapeManagementService,
                stringMessages, SharedLandscapeConstants.DEFAULT_DEDICATED_INSTANCE_TYPE_NAME, errorReporter);
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(1, 2);
        int row=0;
        result.setWidget(row, 0, new Label(stringMessages.autoScalingReplicaInstanceType()));
        result.setWidget(row++, 1, instanceTypeListBox);
        return result;
    }

    @Override
    protected String getResult() {
        return instanceTypeListBox.getSelectedValue();
    }
}
