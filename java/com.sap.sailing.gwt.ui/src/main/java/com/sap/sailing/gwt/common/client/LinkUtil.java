package com.sap.sailing.gwt.common.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Event;
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
}
