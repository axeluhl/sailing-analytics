package com.sap.sailing.gwt.home.shared.dispatch;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

/**
 * Convenience implementation of {@link AsyncCallback} interface to use in {@link Activity} subclasses.
 * It implements {@link AsyncCallback#onFailure(Throwable)} method for a consistent error handling.
 */
public abstract class ActivityCallback<T> implements AsyncCallback<T> {
    
    private final ErrorAndBusyClientFactory clientFactory;
    private final AcceptsOneWidget contentPanel;
    
    public ActivityCallback(ErrorAndBusyClientFactory clientFactory, AcceptsOneWidget contentPanel) {
        this.clientFactory = clientFactory;
        this.contentPanel = contentPanel;
    }

    @Override
    public final void onFailure(Throwable caught) {
        contentPanel.setWidget(clientFactory.createErrorView("Error while loading data!", caught));
    }

}
