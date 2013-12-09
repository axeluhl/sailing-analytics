package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class IgtimiAccountsPanel extends SimplePanel {

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private Iterable<String> allAccountEmails;
    private final TextBox userEmail;
    private final PasswordTextBox password;
    
    // TODO use ListEditorComposite for user e-mail editing
    
    public IgtimiAccountsPanel(final SailingServiceAsync sailingService, final ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        userEmail = new TextBox();
        password = new PasswordTextBox();
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
                        Window.alert(IgtimiAccountsPanel.this.stringMessages.successfullyAuthorizedAccessToIgtimiUser(userEmail.getText()));
                    }
                });
            }
        });
        add(addAccountButton);
    }

    private void updateAllAccounts(final StringMessages stringMessages) {
        this.sailingService.getAllIgtimiAccountEmailAddresses(new AsyncCallback<Iterable<String>>() {
            @Override
            public void onSuccess(Iterable<String> result) {
                allAccountEmails = result;
            }
            
            @Override
            public void onFailure(Throwable caught) {
                IgtimiAccountsPanel.this.errorReporter.reportError(stringMessages.errorFetchingIgtimiAccounts(caught.getMessage()));
            }
        });
    }

    
}
