package com.sap.sse.gwt.client;

import com.google.gwt.dom.client.Element;

/**
 * Utility class contained convenience methods regarding DOM operations.
 */
public final class DOMUtils {

    private DOMUtils() {
    }

    /**
     * Scrolls the top of the provided {@link Element element} to the top of the viewport.
     * 
     * @param element
     *            {@link Element} to scroll to the top
     */
    public static void scrollToTop(final Element element) {
        int top = getOffsetTopOf(element);
        Element current = element.getParentElement();
        while (current != null) {
            if (top != current.getScrollTop()) {
                current.setScrollTop(top);
            }
            top += getOffsetTopOf(current) - current.getScrollTop();
            current = current.getParentElement();
        }
    }

    private static int getOffsetTopOf(final Element element) {
        final int top = element.getOffsetTop();
        final boolean parentIsOffsetParent = element.getParentElement() == element.getOffsetParent();
        return parentIsOffsetParent ? top : top - element.getParentElement().getOffsetTop();
    }

}