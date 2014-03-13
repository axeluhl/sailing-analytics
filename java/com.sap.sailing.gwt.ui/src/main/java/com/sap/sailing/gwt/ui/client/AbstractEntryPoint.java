package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.debug.client.DebugInfo;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.gwt.ui.EntryPointUtils;

public abstract class AbstractEntryPoint extends EntryPointUtils implements EntryPoint, ErrorReporter, WindowSizeDetector {
    /**
     * <p>The attribute which is used for the debug id.</p>
     */
    public static final String DEBUG_ID_ATTRIBUTE = "selenium-id"; //$NON-NLS-1$
    
    /**
     * <p>The prefix which is used for the debug id.</p>
     */
    public static final String DEBUG_ID_PREFIX = ""; //$NON-NLS-1$
    
    private DialogBox errorDialogBox;
    private HTML serverResponseLabel;
    private Button dialogCloseButton;
    protected StringMessages stringMessages;
    protected UserAgentDetails userAgent;
    protected Label persistentAlertLabel;
    
    /**
     * Create a remote service proxy to talk to the server-side sailing service.
     */
    protected final SailingServiceAsync sailingService = GWT.create(SailingService.class);

    /**
     * Create a remote service proxy to talk to the server-side media service.
     */
    protected final MediaServiceAsync mediaService = GWT.create(MediaService.class);

    /**
     * Create a remote service proxy to talk to the server-side user management service.
     */
    protected final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);

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
            
            DebugInfo.setDebugIdAttribute(DEBUG_ID_ATTRIBUTE, false);
            DebugInfo.setDebugIdPrefix(DEBUG_ID_PREFIX);
        }
        doOnModuleLoad();
        if (DebugInfo.isDebugIdEnabled()) {
            PendingAjaxCallMarker.decrementPendingAjaxCalls(MarkedAsyncCallback.CATEGORY_GLOBAL);
        }
    }
    
    protected void doOnModuleLoad() {
        stringMessages = GWT.create(StringMessages.class);
        errorDialogBox = createErrorDialog(); /* TODO: Make this more generic (e.g. make it support all kinds of messages) */
        userAgent = new UserAgentDetails(Window.Navigator.getUserAgent());
        persistentAlertLabel = new Label("");
        persistentAlertLabel.setStyleName("global-alert-message");
        
        ServiceDefTarget sailingServiceDef = (ServiceDefTarget) sailingService;
        ServiceDefTarget mediaServiceDef = (ServiceDefTarget) mediaService;
        ServiceDefTarget userManagementServiceDef = (ServiceDefTarget) userManagementService;
        String moduleBaseURL = GWT.getModuleBaseURL();
        String baseURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf('/', moduleBaseURL.length()-2)+1);
        sailingServiceDef.setServiceEntryPoint(baseURL + "sailing");
        mediaServiceDef.setServiceEntryPoint(baseURL + "media");
        userManagementServiceDef.setServiceEntryPoint(baseURL + "usermanagement");
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
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages, this);
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
}
