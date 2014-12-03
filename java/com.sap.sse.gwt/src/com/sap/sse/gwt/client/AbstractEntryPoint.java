package com.sap.sse.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.debug.client.DebugInfo;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.async.PendingAjaxCallMarker;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.gwt.shared.DebugConstants;

public abstract class AbstractEntryPoint<S extends StringMessages> implements EntryPoint, ErrorReporter, WindowSizeDetector  {
    protected UserAgentDetails userAgent;
    private DialogBox errorDialogBox;
    private HTML serverResponseLabel;
    private Button dialogCloseButton;
    private S stringMessages;
    protected Label persistentAlertLabel;

    @Override
    public final void onModuleLoad() {
        this.stringMessages = createStringMessages();
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
    
    /**
     * Provides the concrete implementation the opportunity to specify a concrete subclass of the {@link StringMessages} type to
     * use for i18n support.
     */
    protected abstract S createStringMessages();
    
    protected void doOnModuleLoad() {
        userAgent = new UserAgentDetails(Window.Navigator.getUserAgent());
        errorDialogBox = createErrorDialog(); /* TODO: Make this more generic (e.g. make it support all kinds of messages) */
        persistentAlertLabel = new Label("");
        persistentAlertLabel.setStyleName("global-alert-message");
    }
    
    /**
     * Sets the size of the tab panel when the tab panel is attached to the DOM
     */
    public static void setTabPanelSize(final TabLayoutPanel advancedTabPanel, final String width, final String height) {
        advancedTabPanel.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                advancedTabPanel.getElement().getParentElement().getStyle().setProperty("width", width);
                advancedTabPanel.getElement().getParentElement().getStyle().setProperty("height", height);
            }
        });
    }

    @Override
    public void reportError(String message) {
        errorDialogBox.setText(message);
        serverResponseLabel.addStyleName("serverResponseLabelError"); //$NON-NLS-1$
        serverResponseLabel.setHTML(getStringMessages().serverError());
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

    private DialogBox createErrorDialog() {
        // Create the popup dialog box
        final DialogBox myErrorDialogBox = new DialogBox();
        myErrorDialogBox.setText(getStringMessages().remoteProcedureCall());
        myErrorDialogBox.setAnimationEnabled(true);
        dialogCloseButton = new Button(getStringMessages().close());
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

    protected S getStringMessages() {
        return stringMessages;
    }
}
