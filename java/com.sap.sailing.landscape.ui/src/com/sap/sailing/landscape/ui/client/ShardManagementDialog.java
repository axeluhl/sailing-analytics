package com.sap.sailing.landscape.ui.client;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class ShardManagementDialog extends DataEntryDialog<Boolean> {
    private final ShardManagementPanel shardPanel;

    ShardManagementDialog(LandscapeManagementWriteServiceAsync landscapeManagementWriteServiceAsync,
            SailingApplicationReplicaSetDTO<String> applicastionReplicaSet, String region, String passphrase,
            ErrorReporter errorReporter, StringMessages stringMessages, DialogCallback<Boolean> callback) {
        super(stringMessages.shard(), stringMessages.shardingDescription(), stringMessages.close(), /* no cancel button */ null, /* validator */ null, callback);
        shardPanel = new ShardManagementPanel(landscapeManagementWriteServiceAsync, errorReporter, stringMessages);
        shardPanel.setRegion(region);
        shardPanel.setPassphrase(passphrase);
        shardPanel.setReplicaSet(applicastionReplicaSet);
        shardPanel.refresh();
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        return shardPanel;
    }

    @Override
    protected Boolean getResult() {
        return shardPanel.hasAnythingChanged();
    }
}
