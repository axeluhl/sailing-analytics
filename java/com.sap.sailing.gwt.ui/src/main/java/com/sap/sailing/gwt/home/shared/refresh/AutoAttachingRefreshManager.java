package com.sap.sailing.gwt.home.shared.refresh;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;

public class AutoAttachingRefreshManager extends LifecycleRefreshManager {
    private static final Logger LOG = Logger.getLogger(AutoAttachingRefreshManager.class.getName());

    private final Widget content;

    private AcceptsOneWidget container;
    
    public AutoAttachingRefreshManager(Widget content, AcceptsOneWidget container, DispatchSystem actionExecutor) {
        super((container instanceof HasAttachHandlers) ? ((HasAttachHandlers) container) : content, actionExecutor);
        this.content = content;
        this.container = container;
    }
    
    @Override
    protected boolean canExecute() {
        return super.canExecute() || container != null;
    }
    
    @Override
    protected void onSuccessfulUpdate() {
        if(container != null) {
            LOG.log(Level.FINE, "Attaching refreshable content to container");
            container.setWidget(content);
            container = null;
        }
    }
}
