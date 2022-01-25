package com.sap.sailing.landscape.ui.client;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.SharedLandscapeConstants;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CreateApplicationReplicaSetDialog extends AbstractApplicationReplicaSetDialog<CreateApplicationReplicaSetDialog.CreateApplicationReplicaSetInstructions> {
    private static final String DEFAULT_INSTANCE_TYPE = "C4_2_XLARGE";
    
    public static class CreateApplicationReplicaSetInstructions extends AbstractApplicationReplicaSetDialog.AbstractApplicationReplicaSetInstructions {
        private final String name;
        private final String instanceType;
        private final String optionalReplicaInstanceType;
        private final boolean dynamicLoadBalancerMapping;
        private final String optionalDomainName;
        private final Integer optionalMemoryInMegabytesOrNull;
        private final Integer optionalMemoryTotalSizeFactorOrNull;
        private final boolean firstReplicaOnSharedInstance;
        
        public CreateApplicationReplicaSetInstructions(String name, String instanceType,
                String optionalReplicaInstanceType, String releaseNameOrNullForLatestMaster,
                boolean dynamicLoadBalancerMapping, String masterReplicationBearerToken, String replicaReplicationBearerToken,
                String optionalDomainName, Integer optionalMemoryInMegabytesOrNull, Integer optionalMemoryTotalSizeFactorOrNull, boolean firstReplicaOnSharedInstance) {
            super(releaseNameOrNullForLatestMaster, masterReplicationBearerToken, replicaReplicationBearerToken);
            this.name = name;
            this.dynamicLoadBalancerMapping = dynamicLoadBalancerMapping;
            this.optionalDomainName = Util.hasLength(optionalDomainName) ? optionalDomainName : null;
            this.instanceType = instanceType;
            this.optionalReplicaInstanceType = optionalReplicaInstanceType;
            this.optionalMemoryInMegabytesOrNull = optionalMemoryInMegabytesOrNull;
            this.optionalMemoryTotalSizeFactorOrNull = optionalMemoryTotalSizeFactorOrNull;
            this.firstReplicaOnSharedInstance = firstReplicaOnSharedInstance;
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
        public String getInstanceType() {
            return instanceType;
        }
        public String getOptionalReplicaInstanceType() {
            return optionalReplicaInstanceType;
        }
        public Integer getOptionalMemoryInMegabytesOrNull() {
            return optionalMemoryInMegabytesOrNull;
        }
        public Integer getOptionalMemoryTotalSizeFactorOrNull() {
            return optionalMemoryTotalSizeFactorOrNull;
        }
        public boolean isFirstReplicaOnSharedInstance() {
            return firstReplicaOnSharedInstance;
        }
    }
    
    private static class Validator implements DataEntryDialog.Validator<CreateApplicationReplicaSetInstructions> {
        private final StringMessages stringMessages;
        
        public Validator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(CreateApplicationReplicaSetInstructions valueToValidate) {
            final String result;
            if (!Util.hasLength(valueToValidate.getInstanceType())) {
                result = stringMessages.pleaseSelectInstanceTypeForNewMaster();
            } else if (!Util.hasLength(valueToValidate.getMasterReplicationBearerToken())) {
                result = stringMessages.pleaseProvideBearerTokenForSecurityReplication();
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
    private final ListBox instanceTypeListBox;
    private final ListBox replicaInstanceTypeListBox;
    private final IntegerBox memoryInMegabytesBox;
    private final IntegerBox memoryTotalSizeFactorBox;
    private final CheckBox startWithReplicaOnSharedInstanceBox;
    private final String SAME_AS_MASTER_VALUE = "___same_as_master___";

    public CreateApplicationReplicaSetDialog(LandscapeManagementWriteServiceAsync landscapeManagementService, Iterable<String> releaseNames,
            StringMessages stringMessages, ErrorReporter errorReporter, DialogCallback<CreateApplicationReplicaSetInstructions> callback) {
        super(stringMessages.createApplicationReplicaSet(), landscapeManagementService, releaseNames, stringMessages, errorReporter, new Validator(stringMessages), callback);
        this.stringMessages = stringMessages;
        nameBox = createTextBox("", 40);
        dynamicLoadBalancerCheckBox = createCheckbox(stringMessages.useDynamicLoadBalancer());
        domainNameBox = createTextBox(SharedLandscapeConstants.DEFAULT_DOMAIN_NAME, 40);
        instanceTypeListBox = LandscapeDialogUtil.createInstanceTypeListBox(this, landscapeManagementService, stringMessages, DEFAULT_INSTANCE_TYPE, errorReporter);
        replicaInstanceTypeListBox = LandscapeDialogUtil.createInstanceTypeListBoxWithAdditionalDefaultEntry(this,
                stringMessages.sameAsMaster(), SAME_AS_MASTER_VALUE, landscapeManagementService, stringMessages,
                /* default instance type */ null, errorReporter);
        memoryInMegabytesBox = createIntegerBox(null, 7);
        memoryTotalSizeFactorBox = createIntegerBox(null, 2);
        memoryInMegabytesBox.addValueChangeHandler(e->memoryTotalSizeFactorBox.setEnabled(e.getValue() == null));
        startWithReplicaOnSharedInstanceBox = createCheckbox(stringMessages.firstReplicaOnSharedInstance());
    }
    
    protected ListBox getInstanceTypeListBox() {
        return instanceTypeListBox;
    }

    protected ListBox getReplicaInstanceTypeListBox() {
        return replicaInstanceTypeListBox;
    }
    
    protected CheckBox getStartWithReplicaOnSharedInstanceBox() {
        return startWithReplicaOnSharedInstanceBox;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(11, 2);
        int row=0;
        result.setWidget(row, 0, new Label(stringMessages.name()));
        result.setWidget(row++, 1, nameBox);
        result.setWidget(row, 0, new Label(stringMessages.release()));
        result.setWidget(row++, 1, getReleaseNameBox());
        result.setWidget(row, 0, new Label(stringMessages.instanceType()));
        result.setWidget(row++, 1, getInstanceTypeListBox());
        result.setWidget(row, 0, new Label(stringMessages.replicaInstanceType()));
        result.setWidget(row++, 1, getReplicaInstanceTypeListBox());
        result.setWidget(row, 0, new Label(stringMessages.firstReplicaOnSharedInstance()));
        result.setWidget(row++, 1, getStartWithReplicaOnSharedInstanceBox());
        result.setWidget(row, 0, new Label(stringMessages.useDynamicLoadBalancer()));
        result.setWidget(row++, 1, dynamicLoadBalancerCheckBox);
        result.setWidget(row, 0, new Label(stringMessages.bearerTokenForSecurityReplication()));
        result.setWidget(row++, 1, getMasterReplicationBearerTokenBox());
        result.setWidget(row, 0, new Label(stringMessages.replicaReplicationBearerToken()));
        result.setWidget(row++, 1, getReplicaReplicationBearerTokenBox());
        result.setWidget(row, 0, new Label(stringMessages.domainName()));
        result.setWidget(row++, 1, domainNameBox);
        result.setWidget(row, 0, new Label(stringMessages.memoryInMegabytes()));
        result.setWidget(row++, 1, memoryInMegabytesBox);
        result.setWidget(row, 0, new Label(stringMessages.memoryTotalSizeFactor()));
        result.setWidget(row++, 1, memoryTotalSizeFactorBox);
        return result;
    }

    @Override
    public FocusWidget getInitialFocusWidget() {
        return nameBox;
    }
    
    @Override
    protected CreateApplicationReplicaSetInstructions getResult() {
        return new CreateApplicationReplicaSetInstructions(nameBox.getValue(),
                getInstanceTypeListBox().getSelectedValue(),
                getReplicaInstanceTypeListBox().getSelectedValue().equals(SAME_AS_MASTER_VALUE) ? null : getReplicaInstanceTypeListBox().getSelectedValue(),
                getReleaseNameBoxValue(), dynamicLoadBalancerCheckBox.getValue(),
                getMasterReplicationBearerTokenBox().getValue(), getReplicaReplicationBearerTokenBox().getValue(),
                domainNameBox.getValue(), memoryInMegabytesBox.getValue(), memoryTotalSizeFactorBox.getValue(), startWithReplicaOnSharedInstanceBox.getValue());
    }
}
