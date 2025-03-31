package com.sap.sse.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;

/**
 * When just attaching a handler to an {@link Anchor} or {@link AnchorElement} to trigger a place change there are muiltiple disadvantages:
 * <ul>
 *   <li>Opening the page in a new tab via middle click or Ctrl+Click doesn't work</li>
 *   <li>Clicks with any mouse key will trigger the handler</li>
 * </ul>
 * 
 * This util helps to work around such problems.
 */
public final class LinkUtil {
    private static final HyperlinkImpl HYPERLINK_IMPL = GWT.create(HyperlinkImpl.class);
    
    private LinkUtil() {
    }
    
    /**
     * @param event a browser event to check if it is a "normal" click event
     * @return true if the event is a "normal" click event, false otherwise
     */
    public static boolean handleLinkClick(Event event) {
        if (event.getTypeInt() != Event.ONCLICK) {
            return false;
        }
        return HYPERLINK_IMPL.handleAsClick(event);
    }


    /**
     * Configures click handling of the given {@link AnchorElement} to trigger the given action when a "normal" click was triggered.
     */
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
