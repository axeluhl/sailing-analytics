package com.sap.sailing.landscape.ui.client;

import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.common.SharedLandscapeConstants;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;

public class CreateReverseProxyInClusterDialog
        extends DataEntryDialog<CreateReverseProxyInClusterDialog.CreateReverseProxyDTO> {

    public static class CreateReverseProxyDTO implements IsSerializable {

        private String instanceType;
        private String name;
        private String region;
        private String availabilityZone;
        private String keyName;
        
        @Deprecated
        public CreateReverseProxyDTO() { // essential line for GWT serialization

        }

        public CreateReverseProxyDTO(String name, String instanceType, String availabilityZone, String region) {
            this.name = name;
            this.instanceType = instanceType;
            this.availabilityZone = availabilityZone;
            this.region = region;

        }

        public String getName() {
            return name;
        }

        public String getInstanceType() {
            return instanceType;
        }

        public String getRegion() {
            return region;
        }

        public String getAvailabilityZone() {
            return availabilityZone;
        }

        public String getKey() {
            return keyName;
        }

        public void setRegion(String region) {
            this.region = region;

        }

        public void setKey(String key) {
            keyName = key;
            
        }

    }

    private StringMessages stringMessages;
    private final TextBox proxyName;
    private final ListBox dedicatedInstanceTypeListBox;
    private ListBox availabilityZone;
    private String region;

    public CreateReverseProxyInClusterDialog(StringMessages stringMessages, ErrorReporter errorReporter,
            LandscapeManagementWriteServiceAsync landscapeManagementService, String region,
            String leastpopulatedAzId, DialogCallback<CreateReverseProxyInClusterDialog.CreateReverseProxyDTO> callback) {
        super(stringMessages.reverseProxies(), stringMessages.reverseProxies(), stringMessages.ok(),
                stringMessages.cancel(), new Validator<CreateReverseProxyInClusterDialog.CreateReverseProxyDTO>() {

                    @Override
                    public String getErrorMessage(
                            CreateReverseProxyInClusterDialog.CreateReverseProxyDTO valueToValidate) {

                        if (!Util.hasLength(valueToValidate.getName())) {
                            return stringMessages.pleaseProvideNonEmptyName();
                        } else {
                            return null;
                        }
                    }

                }, callback);
        this.stringMessages = stringMessages;
        proxyName = createTextBox("", 20);
        dedicatedInstanceTypeListBox = LandscapeDialogUtil.createInstanceTypeListBox(this, landscapeManagementService,
                stringMessages, SharedLandscapeConstants.DEFAULT_DEDICATED_INSTANCE_TYPE_NAME, errorReporter); // TODO:
                                                                                                               // research
                                                                                                               // best
                                                                                                               // default.
        availabilityZone = LandscapeDialogUtil.createInstanceAZTypeListBox(this, landscapeManagementService,
                stringMessages, leastpopulatedAzId, errorReporter, region);
        this.region= region; 
    }

    @Override
    protected Widget getAdditionalWidget() {
        final FormPanel result = new FormPanel();
        final VerticalPanel verticalPanel = new VerticalPanel();
        result.add(verticalPanel);
        verticalPanel.add(new Label(stringMessages.name()));
        verticalPanel.add(proxyName);
        verticalPanel.add(new Label(stringMessages.instanceType()));
        verticalPanel.add(dedicatedInstanceTypeListBox);
        verticalPanel.add(new Label(stringMessages.availabilityZone()));
        verticalPanel.add(availabilityZone);
        return result;
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return proxyName;
    }

    @Override
    protected CreateReverseProxyInClusterDialog.CreateReverseProxyDTO getResult() {
        return new CreateReverseProxyDTO(proxyName.getText(), dedicatedInstanceTypeListBox.getSelectedValue(),
                availabilityZone.getSelectedItemText(), region);
    }
}
