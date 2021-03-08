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
            String defaultInstanceType, ErrorReporter errorReporter) {
        final ListBox instanceTypeBox = dialog.createListBox(/*isMultipleSelect*/false);
        landscapeManagementService.getInstanceTypes(new AsyncCallback<ArrayList<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
            
            @Override
            public void onSuccess(ArrayList<String> result) {
                Collections.sort(result, new NaturalComparator());
                int i=0;
                for (final String instanceType : result) {
                    instanceTypeBox.addItem(instanceType, instanceType);
                    if (instanceType.equals(defaultInstanceType)) {
                        instanceTypeBox.setSelectedIndex(i);
                    }
                    i++;
                }
            }
        });
        return instanceTypeBox;
    }
}
