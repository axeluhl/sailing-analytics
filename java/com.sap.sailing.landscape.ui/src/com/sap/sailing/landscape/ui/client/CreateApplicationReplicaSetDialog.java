package com.sap.sailing.landscape.ui.client;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.SharedLandscapeConstants;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;

public class CreateApplicationReplicaSetDialog extends AbstractApplicationReplicaSetDialog<CreateApplicationReplicaSetDialog.CreateApplicationReplicaSetInstructions> {
    public static class CreateApplicationReplicaSetInstructions extends AbstractApplicationReplicaSetDialog.AbstractApplicationReplicaSetInstructions {
        private final String name;
        private final boolean dynamicLoadBalancerMapping;
        private final String optionalDomainName;
        
        public CreateApplicationReplicaSetInstructions(String name, String masterInstanceType,
                String releaseNameOrNullForLatestMaster, boolean dynamicLoadBalancerMapping,
                String securityReplicationBearerToken, String optionalDomainName) {
            super(masterInstanceType, securityReplicationBearerToken, releaseNameOrNullForLatestMaster);
            this.name = name;
            this.dynamicLoadBalancerMapping = dynamicLoadBalancerMapping;
            this.optionalDomainName = Util.hasLength(optionalDomainName) ? optionalDomainName : null;
        }
        public String getName() {
            return name;
        }
        public boolean isDynamicLoadBalancerMapping() {
            return dynamicLoadBalancerMapping;
        }
        public String getOptionalDomainName() {
            return optionalDomainName;
        }
    }
    
    private static class Validator extends AbstractApplicationReplicaSetDialog.Validator<CreateApplicationReplicaSetInstructions> {
        private final StringMessages stringMessages;
        
        public Validator(StringMessages stringMessages) {
            super(stringMessages);
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(CreateApplicationReplicaSetInstructions valueToValidate) {
            final String result;
            final String superResult = super.getErrorMessage(valueToValidate);
            if (superResult != null) {
                result = superResult;
            } else if (!Util.hasLength(valueToValidate.getName())) {
                result = stringMessages.pleaseProvideApplicationReplicaSetName();
            } else {
                result = null;
            }
            return result;
        }
    }
    
    private final StringMessages stringMessages;
    private final TextBox nameBox;
    private final CheckBox dynamicLoadBalancerCheckBox;
    private final TextBox domainNameBox;

    public CreateApplicationReplicaSetDialog(LandscapeManagementWriteServiceAsync landscapeManagementService, Iterable<String> releaseNames,
            StringMessages stringMessages, ErrorReporter errorReporter, DialogCallback<CreateApplicationReplicaSetInstructions> callback) {
        super(landscapeManagementService, releaseNames, stringMessages, errorReporter, new Validator(stringMessages), callback);
        this.stringMessages = stringMessages;
        nameBox = createTextBox("", 40);
        dynamicLoadBalancerCheckBox = createCheckbox(stringMessages.useDynamicLoadBalancer());
        domainNameBox = createTextBox(SharedLandscapeConstants.DEFAULT_DOMAIN_NAME, 40);
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(6, 2);
        int row=0;
        result.setWidget(row, 0, new Label(stringMessages.name()));
        result.setWidget(row++, 1, nameBox);
        result.setWidget(row, 0, new Label(stringMessages.release()));
        result.setWidget(row++, 1, getReleaseNameBox());
        result.setWidget(row, 0, new Label(stringMessages.instanceType()));
        result.setWidget(row++, 1, getInstanceTypeListBox());
        result.setWidget(row, 0, new Label(stringMessages.useDynamicLoadBalancer()));
        result.setWidget(row++, 1, dynamicLoadBalancerCheckBox);
        result.setWidget(row, 0, new Label(stringMessages.bearerTokenForSecurityReplication()));
        result.setWidget(row++, 1, getSecurityReplicationBearerToken());
        result.setWidget(row, 0, new Label(stringMessages.domainName()));
        result.setWidget(row++, 1, domainNameBox);
        return result;
    }

    @Override
    public FocusWidget getInitialFocusWidget() {
        return nameBox;
    }
    
    @Override
    protected CreateApplicationReplicaSetInstructions getResult() {
        return new CreateApplicationReplicaSetInstructions(nameBox.getValue(), getInstanceTypeListBox().getSelectedValue(),
                getReleaseNameBoxValue(), dynamicLoadBalancerCheckBox.getValue(), getSecurityReplicationBearerToken().getValue(), domainNameBox.getValue());
    }
}
