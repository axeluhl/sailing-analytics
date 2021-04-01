package com.sap.sailing.landscape.ui.client;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class AbstractApplicationReplicaSetDialog<I extends AbstractApplicationReplicaSetDialog.AbstractApplicationReplicaSetInstructions> extends DataEntryDialog<I> {
    public static class AbstractApplicationReplicaSetInstructions {
        private final String releaseNameOrNullForLatestMaster;
        
        public AbstractApplicationReplicaSetInstructions(String releaseNameOrNullForLatestMaster) {
            super();
            this.releaseNameOrNullForLatestMaster = releaseNameOrNullForLatestMaster;
        }
        public String getReleaseNameOrNullForLatestMaster() {
            return releaseNameOrNullForLatestMaster;
        }
    }
    
    private final StringMessages stringMessages;
    private final SuggestBox releaseNameBox;

    public AbstractApplicationReplicaSetDialog(LandscapeManagementWriteServiceAsync landscapeManagementService, Iterable<String> releaseNames,
            StringMessages stringMessages, ErrorReporter errorReporter, Validator<I> validator, DialogCallback<I> callback) {
        super(stringMessages.createApplicationReplicaSet(), /* message */ null, stringMessages.ok(), stringMessages.cancel(), validator, callback);
        this.stringMessages = stringMessages;
        final List<String> releaseNamesAndLatestMaster = new LinkedList<>();
        Util.addAll(releaseNames, releaseNamesAndLatestMaster);
        final Comparator<String> newestFirstComaprator = (r1, r2)->r2.compareTo(r1);
        Collections.sort(releaseNamesAndLatestMaster, newestFirstComaprator);
        releaseNamesAndLatestMaster.add(0, stringMessages.latestMasterRelease());
        releaseNameBox = createSuggestBox(releaseNamesAndLatestMaster);
        if (releaseNameBox.getSuggestOracle() instanceof MultiWordSuggestOracle) {
            ((MultiWordSuggestOracle) releaseNameBox.getSuggestOracle()).setComparator(newestFirstComaprator);
        }
        releaseNameBox.setValue(stringMessages.latestMasterRelease());
    }
    
    protected StringMessages getStringMessages() {
        return stringMessages;
    }
    
    protected SuggestBox getReleaseNameBox() {
        return releaseNameBox;
    }
    
    protected String getReleaseNameBoxValue() {
        return (!Util.hasLength(releaseNameBox.getValue()) || Util.equalsWithNull(releaseNameBox.getValue(), stringMessages.latestMasterRelease()))
                ? null : releaseNameBox.getValue();
    }
}
