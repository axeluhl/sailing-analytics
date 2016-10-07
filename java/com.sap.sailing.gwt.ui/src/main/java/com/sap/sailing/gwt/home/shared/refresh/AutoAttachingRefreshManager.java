package com.sap.sailing.gwt.home.shared.refresh;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;

/**
 * Specialized {@link LifecycleRefreshManager} that is bound to the lifecycle of a given {@link AcceptsOneWidget} and
 * ensures that a content {@link Widget} is being attached when the first refresh is successfully finished. This enures
 * that no uninitialized {@link RefreshableWidget} are initially shown. Instead you can explicitly set a placeholder
 * widget to the container that is automatically replaced with the actual content when the data is available.
 */
public class AutoAttachingRefreshManager extends LifecycleRefreshManager {
    private static final Logger LOG = Logger.getLogger(AutoAttachingRefreshManager.class.getName());

    private final Widget content;

    private AcceptsOneWidget container;
    
    public AutoAttachingRefreshManager(Widget content, AcceptsOneWidget container, SailingDispatchSystem actionExecutor) {
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
