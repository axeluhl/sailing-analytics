package com.sap.sse.gwt.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.Notification.NotificationType;

public class DefaultErrorReporter<S extends StringMessages> implements ErrorReporter {
    private DialogBox errorDialogBox;
    private HTML serverResponseLabel;
    private Button dialogCloseButton;
    private S stringMessages;
    protected Label persistentAlertLabel;
    
    public DefaultErrorReporter(S stringMessages) {
        this.stringMessages = stringMessages;

        /* TODO: Make this more generic (e.g. make it support all kinds of messages) */
        errorDialogBox = createErrorDialog(); 
        persistentAlertLabel = new Label("");
        persistentAlertLabel.setStyleName("global-alert-message");
    }
    

    @Override
    public void reportError(String title, String message) {
        errorDialogBox.setText(title);
        serverResponseLabel.addStyleName("serverResponseLabelError"); //$NON-NLS-1$
        serverResponseLabel.setHTML(SafeHtmlUtils.fromString(message).asString() + "<br><br>");
        errorDialogBox.center();
        dialogCloseButton.setFocus(true);
    }
    
    @Override
    public void reportError(String message) {
        errorDialogBox.setText(message);
        serverResponseLabel.addStyleName("serverResponseLabelError"); //$NON-NLS-1$
        serverResponseLabel.setHTML(stringMessages.serverError());
        errorDialogBox.center();
        dialogCloseButton.setFocus(true);
    }

    @Override
    public void reportError(String message, boolean silentMode) {
        if (silentMode) {
            Notification.notify(message, NotificationType.WARNING);
        } else {
            reportError(message);
        }
    }
    
    @Override
    public void reportPersistentInformation(String message) {
        persistentAlertLabel.setText(message);
    }

    @Override
    public Widget getPersistentInformationWidget() {
        return persistentAlertLabel;
    }

    private DialogBox createErrorDialog() {
        // Create the popup dialog box
        final DialogBox myErrorDialogBox = new DialogBox();
        myErrorDialogBox.setText(stringMessages.remoteProcedureCall());
        myErrorDialogBox.setAnimationEnabled(true);
        dialogCloseButton = new Button(stringMessages.close());
        // We can set the id of a widget by accessing its Element
        dialogCloseButton.getElement().setId("closeButton"); //$NON-NLS-1$
        final Label textToServerLabel = new Label();
        serverResponseLabel = new HTML();
        VerticalPanel dialogVPanel = new VerticalPanel();
        dialogVPanel.add(new HTML("<b>"+stringMessages.errorCommunicatingWithServer()+"</b>")); //$NON-NLS-1$
        dialogVPanel.add(textToServerLabel);
        dialogVPanel.add(new HTML("<br><b>"+stringMessages.serverReplies()+"</b>")); //$NON-NLS-1$
        dialogVPanel.add(serverResponseLabel);
        dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
        dialogVPanel.add(dialogCloseButton);
        myErrorDialogBox.setWidget(dialogVPanel);
        // Add a handler to close the DialogBox
        dialogCloseButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                myErrorDialogBox.hide();
            }
        });
        return myErrorDialogBox;
    }

}
