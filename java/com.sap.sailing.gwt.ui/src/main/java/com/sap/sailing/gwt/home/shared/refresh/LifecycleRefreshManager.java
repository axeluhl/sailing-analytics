package com.sap.sailing.gwt.home.shared.refresh;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;

public class LifecycleRefreshManager extends RefreshManager {
    private final HasAttachHandlers lifecycleWidget;
    
    public LifecycleRefreshManager(HasAttachHandlers lifecycleWidget, DispatchSystem actionExecutor) {
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
        
        Scheduler.get().scheduleFinally(new ScheduledCommand() {
            @Override
            public void execute() {
                if(canStart()) {
                    start();
                }
            }
        });
    }
    
    @Override
    protected boolean canStart() {
        return lifecycleWidget.isAttached();
    }
}
