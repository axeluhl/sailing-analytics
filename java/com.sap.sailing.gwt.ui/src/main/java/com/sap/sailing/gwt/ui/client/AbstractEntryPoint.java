package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.debug.client.DebugInfo;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.async.PendingAjaxCallMarker;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.gwt.shared.DebugConstants;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public abstract class AbstractEntryPoint implements EntryPoint, ErrorReporter, WindowSizeDetector {
    private DialogBox errorDialogBox;
    private HTML serverResponseLabel;
    private Button dialogCloseButton;
    protected StringMessages stringMessages;
    protected UserAgentDetails userAgent;
    protected Label persistentAlertLabel;
    private UserService userService;
    private UserManagementServiceAsync userManagementService;

    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while " //$NON-NLS-1$
            + "attempting to contact the server. Please check your network " + "connection and try again."; //$NON-NLS-1$ //$NON-NLS-2$

    @Override
    public final void onModuleLoad() {
        if (DebugInfo.isDebugIdEnabled()) {
            PendingAjaxCallBundle bundle = GWT.create(PendingAjaxCallBundle.class);
            TextResource script = bundle.ajaxSemaphoreJS();
            JavaScriptInjector.inject(script.getText());
            DebugInfo.setDebugIdAttribute(DebugConstants.DEBUG_ID_ATTRIBUTE, false);
            DebugInfo.setDebugIdPrefix(DebugConstants.DEBUG_ID_PREFIX);
        }
        doOnModuleLoad();
        if (DebugInfo.isDebugIdEnabled()) {
            PendingAjaxCallMarker.decrementPendingAjaxCalls(MarkedAsyncCallback.CATEGORY_GLOBAL);
        }
    }
    
    protected void doOnModuleLoad() {
        userManagementService = GWT.create(UserManagementService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService, com.sap.sse.security.ui.client.RemoteServiceMappingConstants.userManagementServiceRemotePath);
        userService = new UserService(userManagementService);
        stringMessages = GWT.create(StringMessages.class);
        errorDialogBox = createErrorDialog(); /* TODO: Make this more generic (e.g. make it support all kinds of messages) */
        userAgent = new UserAgentDetails(Window.Navigator.getUserAgent());
        persistentAlertLabel = new Label("");
        persistentAlertLabel.setStyleName("global-alert-message");
    }
    
    protected UserManagementServiceAsync getUserManagementService() {
        return userManagementService;
    }
    
    protected UserService getUserService() {
        return userService;
    }

    @Override
    public void reportError(String message) {
        errorDialogBox.setText(message);
        serverResponseLabel.addStyleName("serverResponseLabelError"); //$NON-NLS-1$
        serverResponseLabel.setHTML(SERVER_ERROR);
        errorDialogBox.center();
        dialogCloseButton.setFocus(true);
    }

    @Override
    public void reportError(String message, boolean silentMode) {
        if (silentMode) {
            Window.setStatus(message);
        } else {
            reportError(message);
        }
    }
    
    @Override
    public void reportPersistentInformation(String message) {
        persistentAlertLabel.setText(message);
    }
    
    @Override
    public boolean isSmallWidth() {
        int width = Window.getClientWidth();
        return width <= 720;
    }

    public void createErrorPage(String message) {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages, this, getUserService());
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        RootPanel.get().add(logoAndTitlePanel);
        RootPanel.get().add(new Label(message));
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
        dialogVPanel.add(new HTML("<b>Error communicating with server</b>")); //$NON-NLS-1$
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

    /**
     * Sets the size of the tab panel when the tab panel is attached to the DOM
     */
    protected void setTabPanelSize(final TabLayoutPanel advancedTabPanel, final String width, final String height) {
        advancedTabPanel.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                advancedTabPanel.getElement().getParentElement().getStyle().setProperty("width", width);
                advancedTabPanel.getElement().getParentElement().getStyle().setProperty("height", height);
            }
        });
    }
}
