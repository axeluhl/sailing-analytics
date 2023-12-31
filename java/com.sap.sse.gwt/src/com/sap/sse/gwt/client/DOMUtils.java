package com.sap.sse.gwt.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Utility class contained convenience methods regarding DOM operations.
 */
public final class DOMUtils {

    private DOMUtils() {
    }

    /**
     * Scrolls the top of the provided {@link Widget widget} to the topmost possible position in the viewport.
     * 
     * @param widget
     *            {@link Widget} to scroll to the topmost possible position
     */
    public static void scrollToTop(final Widget widget) {
        getViewportElement().setScrollTop(widget.getAbsoluteTop());
    }

    /**
     * Scrolls the top of the provided {@link Element element} to the topmost possible position in the viewport.
     * 
     * @param element
     *            {@link Element} to scroll to the topmost possible position
     */
    public static void scrollToTop(final Element element) {
        getViewportElement().setScrollTop(element.getAbsoluteTop());
    }

    private static Element getViewportElement() {
        final Document doc = Document.get();
        return doc.isCSS1Compat() ? doc.getDocumentElement() : doc.getBody();
    }

    /**
     * Returns a live list of the {@link OptionElement}s constituting the list box contents. Live here means that
     * adding/removing items to the list box reflects in the node list returned. The {@link OptionElement}s in the node
     * list can be used, e.g., to set a tooltip on the elements, using {@link OptionElement#setTitle(String)}.
     */
    public static NodeList<OptionElement> getOptions(ListBox listBox) {
        final SelectElement selectElement = listBox.getElement().cast();
        return selectElement.getOptions();
    }
}