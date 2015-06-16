package com.sap.sailing.gwt.home.client.shared;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.EventListener;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class DropdownHandler {

    private HandlerRegistration reg;
    boolean dropdownShown = false;
    
    public DropdownHandler(Element dropdownTrigger, final Element dropdownContainer) {
        Event.sinkEvents(dropdownTrigger, Event.ONCLICK);
        Event.setEventListener(dropdownTrigger, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if(dropdownShown) {
                    return;
                }
                
                dropdownShown = true;
                dropdownStateChanged(true);
                reg = Event.addNativePreviewHandler(new NativePreviewHandler() {
                    @Override
                    public void onPreviewNativeEvent(NativePreviewEvent event) {
                        EventTarget eventTarget = event.getNativeEvent().getEventTarget();
                        if(!Element.is(eventTarget)) {
                            return;
                        }
                        Element evtElement = Element.as(eventTarget);
                        if(event.getTypeInt() == Event.ONCLICK && !dropdownContainer.isOrHasChild(evtElement)) {
                            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                                @Override
                                public void execute() {
                                    dropdownShown = false;
                                    dropdownStateChanged(false);
                                    reg.removeHandler();
                                    reg = null;
                                }
                            });
                        }
                    }
                });
            }
        });
    }
    
    protected void dropdownStateChanged(boolean dropdownShown) {
    }

}
