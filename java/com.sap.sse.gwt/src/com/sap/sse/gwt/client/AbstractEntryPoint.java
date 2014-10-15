package com.sap.sse.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.debug.client.DebugInfo;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.async.PendingAjaxCallMarker;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.gwt.shared.DebugConstants;

public abstract class AbstractEntryPoint implements EntryPoint {
    protected UserAgentDetails userAgent;

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
        userAgent = new UserAgentDetails(Window.Navigator.getUserAgent());
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
