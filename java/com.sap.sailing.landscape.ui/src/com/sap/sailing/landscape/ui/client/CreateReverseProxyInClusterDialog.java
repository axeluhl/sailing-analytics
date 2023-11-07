package com.sap.sailing.landscape.ui.client;

import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
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

    public static class CreateReverseProxyDTO {

        private String instanceType;
        private String name;
        private String region;
        private String availabilityZone;

        public CreateReverseProxyDTO(String name, String instanceType, String availabilityZone) {
            this.name = name;
            this.instanceType = instanceType;
            this.availabilityZone = availabilityZone;

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
            // TODO Auto-generated method stub
            return null;
        }

        public void setRegion(String region) {
            this.region = region;

        }

    }

    private StringMessages stringMessages;
    private final TextBox proxyName;
    private final ListBox dedicatedInstanceTypeListBox;

    public CreateReverseProxyInClusterDialog(StringMessages stringMessages, ErrorReporter errorReporter,
            LandscapeManagementWriteServiceAsync landscapeManagementService,
            DialogCallback<CreateReverseProxyInClusterDialog.CreateReverseProxyDTO> callback) {
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
        return result;
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return proxyName;
    }

    @Override
    protected CreateReverseProxyInClusterDialog.CreateReverseProxyDTO getResult() {
        return new CreateReverseProxyDTO(proxyName.getText(), dedicatedInstanceTypeListBox.getSelectedValue(), "TODO");
    }
}
