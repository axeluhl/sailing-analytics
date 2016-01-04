package com.sap.sailing.gwt.common.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;

public final class LinkUtil {
    private static final HyperlinkImpl HYPERLINK_IMPL = GWT.create(HyperlinkImpl.class);
    
    private LinkUtil() {
    }
    
    public static boolean handleLinkClick(Event event) {
        if(event.getTypeInt() != Event.ONCLICK) {
            return false;
        }
        return HYPERLINK_IMPL.handleAsClick(event);
    }


    public static void configureForAction(AnchorElement anchor, final Runnable action) {
        DOM.sinkEvents(anchor, Event.ONCLICK);
        DOM.setEventListener(anchor, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                GWT.log(event.getString());
                if (LinkUtil.handleLinkClick(event)) {
                    event.preventDefault();
                    action.run();
                }
            }
        });
    }
}
