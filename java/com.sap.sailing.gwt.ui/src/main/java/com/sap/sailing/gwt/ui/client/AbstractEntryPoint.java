package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class AbstractEntryPoint implements EntryPoint, ErrorReporter {

    private DialogBox errorDialogBox;
    private HTML serverResponseLabel;
    private Button dialogCloseButton;
    protected StringConstants stringConstants;
    
    /**
     * Create a remote service proxy to talk to the server-side sailing service.
     */
    protected final SailingServiceAsync sailingService = GWT.create(SailingService.class);

    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while " //$NON-NLS-1$
            + "attempting to contact the server. Please check your network " + "connection and try again."; //$NON-NLS-1$ //$NON-NLS-2$

    @Override
    public void onModuleLoad() {
        errorDialogBox = createErrorDialog();
        stringConstants = GWT.create(StringConstants.class);
        ServiceDefTarget serviceDef = (ServiceDefTarget) sailingService;
        String moduleBaseURL = GWT.getModuleBaseURL();
        String baseURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf('/', moduleBaseURL.length()-2)+1);
        serviceDef.setServiceEntryPoint(baseURL + "sailing");
    }
    
    @Override
    public void reportError(String message) {
        errorDialogBox.setText(message);
        serverResponseLabel.addStyleName("serverResponseLabelError"); //$NON-NLS-1$
        serverResponseLabel.setHTML(SERVER_ERROR);
        errorDialogBox.center();
        dialogCloseButton.setFocus(true);
    }
    
    private DialogBox createErrorDialog() {
        // Create the popup dialog box
        final DialogBox myErrorDialogBox = new DialogBox();
        myErrorDialogBox.setText("Remote Procedure Call"); //$NON-NLS-1$
        myErrorDialogBox.setAnimationEnabled(true);
        dialogCloseButton = new Button("Close"); //$NON-NLS-1$
        // We can set the id of a widget by accessing its Element
        dialogCloseButton.getElement().setId("closeButton"); //$NON-NLS-1$
        final Label textToServerLabel = new Label();
        serverResponseLabel = new HTML();
        VerticalPanel dialogVPanel = new VerticalPanel();
        dialogVPanel.addStyleName("dialogVPanel"); //$NON-NLS-1$
        dialogVPanel.add(new HTML("<b>Sending name to the server:</b>")); //$NON-NLS-1$
        dialogVPanel.add(textToServerLabel);
        dialogVPanel.add(new HTML("<br><b>Server replies:</b>")); //$NON-NLS-1$
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
