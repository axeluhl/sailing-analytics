package com.sap.sailing.gwt.home.shared.refresh;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;

/**
 * Specialized {@link RefreshManager} that is being bound to the lifecycle of a given widget. This is typically a
 * container that holds the layout of the inner refreshable parts.
 * 
 * This should always be used when refresh functionality is used by a UI part that can disappear sometime. This is e.g
 * the case for Activities that are replaced by another Activity when a new Place fires.
 * 
 * The implementation ensures that no memory leak is produced by the inner Timer instance by cleanly stopping all work
 * when the lifecycle widget is detached from the DOM tree.
 */
public class LifecycleRefreshManager extends RefreshManager {
    private final HasAttachHandlers lifecycleWidget;
    
    /**
     * @param lifecycleWidget the widget that defines the lifecycle of this {@link RefreshManager}.
     * @param actionExecutor the {@link SailingDispatchSystem} to use for remote communication
     */
    public LifecycleRefreshManager(HasAttachHandlers lifecycleWidget, SailingDispatchSystem actionExecutor) {
        super(actionExecutor);
        this.lifecycleWidget = lifecycleWidget;
        lifecycleWidget.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    start();
                } else {
                    cancel();
                }
            }
        });
        
        // if the widget is already attached, the listener above won't fire, so we trigger the first refresh directly.
        // scheduleFinally is used to wait until all RefreshableWidgets are added.
        // Otherwise, there would be no RefreshableWidget and the initial refresh would be skipped, so that not refresh would occur at all. 
        Scheduler.get().scheduleFinally(new ScheduledCommand() {
            @Override
            public void execute() {
                if(canExecute()) {
                    start();
                }
            }
        });
    }
    
    @Override
    protected boolean canExecute() {
        // Refreshes can only occur if the lifecycleWidget is actually attached to the DOM tree.
        // Other wise we would produce a memory consuming zombie that eats the browser's resources and unnecessarily fetches data from the server.
        return lifecycleWidget.isAttached();
    }
}
