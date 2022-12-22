package com.sap.sailing.landscape.ui.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class ShardManagementDialog extends DialogBox {
    //private final ShardManagementPanel shardPanel;

    ShardManagementDialog(LandscapeManagementWriteServiceAsync landscapeManagementWriteServiceAsync,
            SailingApplicationReplicaSetDTO<String> applicastionReplicaSet, String region, String passphrase,
            ErrorReporter errorReporter, StringMessages stringMessages) {
//        shardPanel = new ShardManagementPanel(landscapeManagementWriteServiceAsync, errorReporter, stringMessages,
//                this);
//        shardPanel.setRegion(region);
//        shardPanel.setPassphrase(passphrase);
//        shardPanel.setReplicaSet(applicastionReplicaSet);
//        VerticalPanel vPlane = new VerticalPanel();
//        CaptionPanel caption = new CaptionPanel(stringMessages.shard());
//        caption.add(shardPanel);
//        vPlane.add(caption);
//        final Button closeButton = new Button(stringMessages.close());
//        closeButton.addClickHandler(event -> hide());
//        vPlane.add(closeButton);
//        setGlassEnabled(true);
//        setAutoHideEnabled(true);
//        add(vPlane);
//        center();
    }

    @Override
    public void show() {
        super.show();
    }

}
