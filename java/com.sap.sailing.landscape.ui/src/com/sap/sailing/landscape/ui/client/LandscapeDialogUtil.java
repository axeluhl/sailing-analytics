package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.landscape.common.AzFormat;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class LandscapeDialogUtil {
    public static ListBox createInstanceTypeListBox(DataEntryDialog<?> dialog,
            LandscapeManagementWriteServiceAsync landscapeManagementService, StringMessages stringMessages,
            String defaultInstanceTypeName, ErrorReporter errorReporter) {
        return createInstanceTypeListBoxWithAdditionalDefaultEntry(dialog, /* additionalItem */ null,
                /* additionalValue */ null, landscapeManagementService, stringMessages, defaultInstanceTypeName,
                errorReporter);
    }

    /**
     * @param additionalItem
     *            if not {@code null}, an item with this name is created, and then {@code additionalValue} must not be
     *            {@code null} because it is then used as that item's value. The item will then be set as the one
     *            selected.
     */
    public static ListBox createInstanceTypeListBoxWithAdditionalDefaultEntry(DataEntryDialog<?> dialog,
            String additionalItem, String additionalValue,
            LandscapeManagementWriteServiceAsync landscapeManagementService, StringMessages stringMessages,
            String defaultInstanceTypeName, ErrorReporter errorReporter) {
        final ListBox instanceTypeBox = dialog.createListBox(/* isMultipleSelect */false);
        if (additionalItem != null) {
            instanceTypeBox.addItem(additionalItem, additionalValue);
            instanceTypeBox.setSelectedIndex(0);
        }
        landscapeManagementService.getInstanceTypeNames(new AsyncCallback<ArrayList<String>>() {
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

    /**
     * Creates a dropdown list of availability zones in the mixed format, initially selecting the item containing the az
     * name, which is the least populated
     * 
     * @param defaultAZ
     *            The least populated az name.
     */
    public static ListBox createInstanceAZTypeListBox(DataEntryDialog<?> dialog,
            LandscapeManagementWriteServiceAsync landscapeManagementService, StringMessages stringMessages,
            String defaultAZ, ErrorReporter errorReporter, String region) {
        return createInstanceAZTypeListBoxWithAdditionalDefaultEntry(dialog, /* additionalItem */ null,
                /* additionalValue */ null, landscapeManagementService, stringMessages, defaultAZ, errorReporter,
                region);
    }


    public static ListBox createInstanceAZTypeListBoxWithAdditionalDefaultEntry(DataEntryDialog<?> dialog,
            String additionalItem, String additionalValue,
            LandscapeManagementWriteServiceAsync landscapeManagementService, StringMessages stringMessages,
            String defaultAZName, ErrorReporter errorReporter, String region) {
        final ListBox availabilityZoneBox = dialog.createListBox(false);
        if (additionalItem != null) {
            availabilityZoneBox.addItem(additionalItem, additionalValue);
            availabilityZoneBox.setSelectedIndex(0);
        }
        landscapeManagementService.getAvailabilityZones(region, AzFormat.MIXED,  new AsyncCallback<ArrayList<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());

            }

            @Override
            public void onSuccess(ArrayList<String> result) {
                Collections.sort(result);
                int i = 0;
                for (String az : result) {
                    availabilityZoneBox.addItem(az, az);
                    if (additionalItem == null && az.contains(defaultAZName)) {
                        availabilityZoneBox.setSelectedIndex(i);
                    }
                    i++;
                }
            }
        });
        return availabilityZoneBox;
    }
}
