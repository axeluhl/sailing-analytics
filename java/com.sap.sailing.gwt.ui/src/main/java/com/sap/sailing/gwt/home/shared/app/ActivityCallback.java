package com.sap.sailing.gwt.home.shared.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

/**
 * Convenience implementation of {@link AsyncCallback} interface to use in {@link Activity} subclasses.
 * It implements {@link AsyncCallback#onFailure(Throwable)} method for a consistent error handling.
 */
public abstract class ActivityCallback<T> implements AsyncCallback<T> {
    
    private Logger logger = Logger.getLogger(getClass().getName());
    
    private final ErrorAndBusyClientFactory clientFactory;
    private final AcceptsOneWidget contentPanel;
    
    public ActivityCallback(ErrorAndBusyClientFactory clientFactory, AcceptsOneWidget contentPanel) {
        this.clientFactory = clientFactory;
        this.contentPanel = contentPanel;
    }

    @Override
    public void onFailure(Throwable caught) {
        logger.log(Level.SEVERE, "Error while loading data!", caught);
        contentPanel.setWidget(clientFactory.createErrorView("Error while loading data!", caught));
    }
}
