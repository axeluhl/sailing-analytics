package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class LandscapeDialogUtil {
    public static ListBox createInstanceTypeListBox(DataEntryDialog<?> dialog,
            LandscapeManagementWriteServiceAsync landscapeManagementService, StringMessages stringMessages,
            String defaultInstanceTypeName, ErrorReporter errorReporter, boolean canBeDeployedInNlbInstanceBasedTargetGroup) {
        return createInstanceTypeListBoxWithAdditionalDefaultEntry(dialog, /* additionalItem */ null,
                /* additionalValue */ null, landscapeManagementService, stringMessages, defaultInstanceTypeName,
                errorReporter, canBeDeployedInNlbInstanceBasedTargetGroup);
    }

    /**
     * @param additionalItem
     *            if not {@code null}, an item with this name is created, and then {@code additionalValue} must not be
     *            {@code null} because it is then used as that item's value. The item will then be set as the one
     *            selected.
     * @param canBeDeployedInNlbInstanceBasedTargetGroup A boolean indicating whether the instance list should filter out those
     *          which cannot be added to NLB, instance-based target groups. True indicates that it needs to be deployable
     *          to this type of target group, so the resulting listbox should not contain the banned instance types.
     */
    public static ListBox createInstanceTypeListBoxWithAdditionalDefaultEntry(DataEntryDialog<?> dialog,
            String additionalItem, String additionalValue,
            LandscapeManagementWriteServiceAsync landscapeManagementService, StringMessages stringMessages,
            String defaultInstanceTypeName, ErrorReporter errorReporter, boolean canBeDeployedInNlbInstanceBasedTargetGroup) {
        final ListBox instanceTypeBox = dialog.createListBox(/* isMultipleSelect */false);
        if (additionalItem != null) {
            instanceTypeBox.addItem(additionalItem, additionalValue);
            instanceTypeBox.setSelectedIndex(0);
        }
        landscapeManagementService.getInstanceTypeNames(canBeDeployedInNlbInstanceBasedTargetGroup, new AsyncCallback<ArrayList<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(ArrayList<String> result) {
                Collections.sort(result, new NaturalComparator());
                int i = 0;
                for (final String instanceType : result) {
                    instanceTypeBox.addItem(instanceType, instanceType);
                    if (additionalItem == null && instanceType.equals(defaultInstanceTypeName)) {
                        instanceTypeBox.setSelectedIndex(i);
                    }
                    i++;
                }
            }
        });
        return instanceTypeBox;
    }

    public static void selectInstanceType(ListBox instanceTypeListBox, String instanceTypeName) {
        for (int i = 0; i < instanceTypeListBox.getItemCount(); i++) {
            if (instanceTypeListBox.getValue(i).equals(instanceTypeName)) {
                instanceTypeListBox.setSelectedIndex(i);
                break;
            }
        }
    }
}
