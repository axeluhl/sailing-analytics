package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class CreateDefaultRegattaLeaderboardDialog extends AbstractCancelableDialog{

    private RegattaDTO regatta;
    private Button okButton;
    
    public CreateDefaultRegattaLeaderboardDialog(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, RegattaDTO regatta) {
        super(sailingService, stringMessages, errorReporter);
        this.regatta = regatta;
        setupUi();
    }   

    protected void addButtons(Panel buttonPanel) {
        super.addButtons(buttonPanel);
        okButton = new Button(stringMessages.ok());
        okButton.setTitle(stringMessages.canOnlyBeEditedBeforeStartingTracking());
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ok();
            }
        });
        buttonPanel.add(okButton);
    }

    protected void ok() {
        RegattaIdentifier regattaIdentifier = new RegattaName(regatta.getName());
        sailingService.createRegattaLeaderboard(regattaIdentifier, /* displayName */ null, new int[]{},
                new AsyncCallback<StrippedLeaderboardDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create default regatta leaderboard for " + regatta.getName()
                        + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(StrippedLeaderboardDTO result) {
                hide();
            }
        });
    }

}
