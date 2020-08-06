package com.sap.sailing.gwt.home.shared.utils;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class DropdownHandler {

    private HandlerRegistration reg;
    boolean dropdownShown = false;
    private final Element dropdownContainer;
    
    public DropdownHandler(Element dropdownTrigger, Element dropdownContainer) {
        this.dropdownContainer = dropdownContainer;
        Event.sinkEvents(dropdownTrigger, Event.ONCLICK);
        Event.setEventListener(dropdownTrigger, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if(dropdownShown) {
                    return;
                }
                
                show();
            }

        });
    }
    
    public DropdownHandler(FocusWidget dropdownTrigger, Element dropdownContainer) {
        this.dropdownContainer = dropdownContainer;
        dropdownTrigger.addClickHandler(event -> {
            if (dropdownShown) {
                return;
            }
            show();
        });
    }
    
    private void show() {
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
                    hide();
                }
            }
            
        });
    }
    
    private void hide() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                dropdownShown = false;
                dropdownStateChanged(false);
                if(reg != null) {
                    reg.removeHandler();
                    reg = null;
                }
            }
        });
    }
    
    public void setVisible(boolean visible) {
        if(visible == dropdownShown) {
            return;
        }
        if(visible) {
            show();
        } else {
            hide();
        }
    }
    
    protected void dropdownStateChanged(boolean dropdownShown) {
        if(dropdownShown) {
            dropdownContainer.getStyle().clearDisplay();
        } else {
            dropdownContainer.getStyle().setDisplay(Display.NONE);
        }
    }

}
