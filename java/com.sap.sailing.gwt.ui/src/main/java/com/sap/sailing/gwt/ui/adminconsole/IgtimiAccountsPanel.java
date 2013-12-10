package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Iterator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class IgtimiAccountsPanel extends FlowPanel {

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final Label allAccountEmails;
    private final TextBox userEmail;
    private final PasswordTextBox password;
    
    // TODO use ListEditorComposite for user e-mail editing
    
    public IgtimiAccountsPanel(final SailingServiceAsync sailingService, final ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        userEmail = new TextBox();
        password = new PasswordTextBox();
        allAccountEmails = new Label();
        add(new Label(stringMessages.igtimiAccounts()+":"));
        add(allAccountEmails);
        add(userEmail);
        add(password);
        updateAllAccounts(stringMessages);
        Button addAccountButton = new Button(stringMessages.addIgtimiAccount());
        addAccountButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sailingService.authorizeAccessToIgtimiUser(userEmail.getText(), password.getText(), new AsyncCallback<Boolean>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        IgtimiAccountsPanel.this.errorReporter.reportError(stringMessages.errorAuthorizingAccessToIgtimiUser(userEmail.getText(),
                                caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        if (result) {
                            Window.alert(IgtimiAccountsPanel.this.stringMessages.successfullyAuthorizedAccessToIgtimiUser(userEmail.getText()));
                            updateAllAccounts(IgtimiAccountsPanel.this.stringMessages);
                        } else {
                            Window.alert(IgtimiAccountsPanel.this.stringMessages.couldNotAuthorizedAccessToIgtimiUser(userEmail.getText()));
                        }
                    }
                });
            }
        });
        add(addAccountButton);
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateAllAccounts(stringMessages);
            }
        });
        add(refreshButton);
        this.sailingService.getIgtimiAuthorizationUrl(new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(IgtimiAccountsPanel.this.stringMessages.errorGettingIgtimiAuthorizationUrl(caught.getMessage()),
                        /* silentMode */ true);
            }

            @Override
            public void onSuccess(String result) {
                Anchor addIgtimiUserLink = new Anchor(stringMessages.addIgtimiUser(), result); 
                add(addIgtimiUserLink);
            }
        });
    }

    private void updateAllAccounts(final StringMessages stringMessages) {
        this.sailingService.getAllIgtimiAccountEmailAddresses(new AsyncCallback<Iterable<String>>() {
            @Override
            public void onSuccess(Iterable<String> result) {
                StringBuilder sb = new StringBuilder();
                for (Iterator<String> i=result.iterator(); i.hasNext(); ) {
                    sb.append(i.next());
                    if (i.hasNext()) {
                        sb.append(", ");
                    }
                }
                allAccountEmails.setText(sb.toString());
            }
            
            @Override
            public void onFailure(Throwable caught) {
                IgtimiAccountsPanel.this.errorReporter.reportError(stringMessages.errorFetchingIgtimiAccounts(caught.getMessage()));
            }
        });
    }

    
}
