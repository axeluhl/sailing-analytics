package com.sap.sailing.landscape.ui.client;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CreateApplicationReplicaSetDialog extends DataEntryDialog<CreateApplicationReplicaSetDialog.CreateApplicationReplicaSetInstructions> {
    private static final String DEFAULT_INSTANCE_TYPE = "C4_2_XLARGE";
    
    public static class CreateApplicationReplicaSetInstructions {
        private final String name;
        private final String masterInstanceType;
        private final boolean dynamicLoadBalancerMapping;
        private final String securityReplicationBearerToken;
        private final String optionalDomainName;
        
        public CreateApplicationReplicaSetInstructions(String name, String masterInstanceType,
                boolean dynamicLoadBalancerMapping, String securityReplicationBearerToken, String optionalDomainName) {
            super();
            this.name = name;
            this.masterInstanceType = masterInstanceType;
            this.dynamicLoadBalancerMapping = dynamicLoadBalancerMapping;
            this.securityReplicationBearerToken = securityReplicationBearerToken;
            this.optionalDomainName = Util.hasLength(optionalDomainName) ? optionalDomainName : null;
        }
        public String getName() {
            return name;
        }
        public String getMasterInstanceType() {
            return masterInstanceType;
        }
        public boolean isDynamicLoadBalancerMapping() {
            return dynamicLoadBalancerMapping;
        }
        public String getSecurityReplicationBearerToken() {
            return securityReplicationBearerToken;
        }
        public String getOptionalDomainName() {
            return optionalDomainName;
        }
    }
    
    private static class Validator implements DataEntryDialog.Validator<CreateApplicationReplicaSetDialog.CreateApplicationReplicaSetInstructions> {
        private final StringMessages stringMessages;
        
        public Validator(StringMessages stringMessages) {
            super();
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(CreateApplicationReplicaSetInstructions valueToValidate) {
            final String result;
            if (!Util.hasLength(valueToValidate.getName())) {
                result = stringMessages.pleaseProvideApplicationReplicaSetName();
            } else if (!Util.hasLength(valueToValidate.getMasterInstanceType())) {
                result = stringMessages.pleaseSelectInstanceTypeForNewMaster();
            } else if (!Util.hasLength(valueToValidate.getSecurityReplicationBearerToken())) {
                result = stringMessages.pleaseProvideBearerTokenForSecurityReplication();
            } else {
                result = null;
            }
            return result;
        }
    }
    
    private final StringMessages stringMessages;
    private final TextBox nameBox;
    private final ListBox instanceTypeListBox;
    private final CheckBox dynamicLoadBalancerCheckBox;
    private final TextBox securityReplicationBearerToken;
    private final TextBox domainNameBox;

    public CreateApplicationReplicaSetDialog(LandscapeManagementWriteServiceAsync landscapeManagementService, StringMessages stringMessages,
            ErrorReporter errorReporter, DialogCallback<CreateApplicationReplicaSetInstructions> callback) {
        super(stringMessages.createApplicationReplicaSet(), /* message */ null, stringMessages.ok(), stringMessages.cancel(), new Validator(stringMessages), callback);
        this.stringMessages = stringMessages;
        nameBox = createTextBox("", 40);
        instanceTypeListBox = LandscapeDialogUtil.createInstanceTypeListBox(this, landscapeManagementService, stringMessages, DEFAULT_INSTANCE_TYPE, errorReporter);
        dynamicLoadBalancerCheckBox = createCheckbox(stringMessages.useDynamicLoadBalancer());
        securityReplicationBearerToken = createTextBox("", 40);
        domainNameBox = createTextBox("", 40);
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(5, 2);
        int row=0;
        result.setWidget(row, 0, new Label(stringMessages.name()));
        result.setWidget(row++, 1, nameBox);
        result.setWidget(row, 0, new Label(stringMessages.instanceType()));
        result.setWidget(row++, 1, instanceTypeListBox);
        result.setWidget(row, 0, new Label(stringMessages.useDynamicLoadBalancer()));
        result.setWidget(row++, 1, dynamicLoadBalancerCheckBox);
        result.setWidget(row, 0, new Label(stringMessages.bearerTokenForSecurityReplication()));
        result.setWidget(row++, 1, securityReplicationBearerToken);
        result.setWidget(row, 0, new Label(stringMessages.domainName()));
        result.setWidget(row++, 1, domainNameBox);
        return result;
    }

    @Override
    protected CreateApplicationReplicaSetInstructions getResult() {
        return new CreateApplicationReplicaSetInstructions(nameBox.getValue(), instanceTypeListBox.getSelectedValue(),
                dynamicLoadBalancerCheckBox.getValue(), securityReplicationBearerToken.getValue(), domainNameBox.getValue());
    }
}
