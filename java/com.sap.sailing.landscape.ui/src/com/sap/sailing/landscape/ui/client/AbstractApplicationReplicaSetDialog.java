package com.sap.sailing.landscape.ui.client;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class AbstractApplicationReplicaSetDialog<I extends AbstractApplicationReplicaSetDialog.AbstractApplicationReplicaSetInstructions> extends DataEntryDialog<I> {
    private static final String DEFAULT_INSTANCE_TYPE = "C4_2_XLARGE";
    
    public static class AbstractApplicationReplicaSetInstructions {
        private final String instanceType;
        private final String replicationBearerToken;
        
        public AbstractApplicationReplicaSetInstructions(String instanceType, String replicationBearerToken,
                String releaseNameOrNullForLatestMaster) {
            super();
            this.instanceType = instanceType;
            this.replicationBearerToken = replicationBearerToken;
        }
        public String getInstanceType() {
            return instanceType;
        }
        public String getReplicationBearerToken() {
            return replicationBearerToken;
        }
    }
    
    static class Validator<I extends AbstractApplicationReplicaSetDialog.AbstractApplicationReplicaSetInstructions> implements DataEntryDialog.Validator<I> {
        private final StringMessages stringMessages;
        
        public Validator(StringMessages stringMessages) {
            super();
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(I valueToValidate) {
            final String result;
            if (!Util.hasLength(valueToValidate.getInstanceType())) {
                result = stringMessages.pleaseSelectInstanceTypeForNewMaster();
            } else if (!Util.hasLength(valueToValidate.getReplicationBearerToken())) {
                result = stringMessages.pleaseProvideBearerTokenForSecurityReplication();
            } else {
                result = null;
            }
            return result;
        }
    }
    
    private final StringMessages stringMessages;
    private final ListBox instanceTypeListBox;
    private final TextBox securityReplicationBearerToken;
    private final SuggestBox releaseNameBox;

    public AbstractApplicationReplicaSetDialog(LandscapeManagementWriteServiceAsync landscapeManagementService, Iterable<String> releaseNames,
            StringMessages stringMessages, ErrorReporter errorReporter, Validator<I> validator, DialogCallback<I> callback) {
        super(stringMessages.createApplicationReplicaSet(), /* message */ null, stringMessages.ok(), stringMessages.cancel(), validator, callback);
        this.stringMessages = stringMessages;
        instanceTypeListBox = LandscapeDialogUtil.createInstanceTypeListBox(this, landscapeManagementService, stringMessages, DEFAULT_INSTANCE_TYPE, errorReporter);
        securityReplicationBearerToken = createTextBox("", 40);
        final List<String> releaseNamesAndLatestMaster = new LinkedList<>();
        Util.addAll(releaseNames, releaseNamesAndLatestMaster);
        Collections.sort(releaseNamesAndLatestMaster);
        releaseNamesAndLatestMaster.add(0, stringMessages.latestMasterRelease());
        releaseNameBox = createSuggestBox(releaseNamesAndLatestMaster);
    }
    
    protected StringMessages getStringMessages() {
        return stringMessages;
    }
    
    protected ListBox getInstanceTypeListBox() {
        return instanceTypeListBox;
    }

    protected TextBox getSecurityReplicationBearerToken() {
        return securityReplicationBearerToken;
    }

    protected SuggestBox getReleaseNameBox() {
        return releaseNameBox;
    }
    
    protected String getReleaseNameBoxValue() {
        return (!Util.hasLength(releaseNameBox.getValue()) || Util.equalsWithNull(releaseNameBox.getValue(), stringMessages.latestMasterRelease()))
                ? null : releaseNameBox.getValue();
    }
}
