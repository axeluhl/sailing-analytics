package com.sap.sse.gwt.client.breadcrumb;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.LinkUtil;

/**
 * Widget that renders multiple clickable items with ">" as spacer.
 * 
 * Every item has:
 * <ul>
 * <li>a Text to show</li>
 * <li>an action that is being triggered when doing a normal click</li>
 * <li>a link that is used when the user clicks to open in a new tab</li>
 * </ul>
 *
 */
public class BreadcrumbPane extends Widget {

    interface Resources extends ClientBundle {
        @Source("Breadcrumb.gss")
        Style style();
        
        @Source("backgroundImage.png")
        DataResource backgroundImage();
    }

    interface Style extends CssResource {

        String event_header__breadcrumb();

        String event_header__breadcrumb__item();

    }

    private static Resources resources;

    private static Resources getResources() {
        if (resources == null) {
            resources = GWT.create(Resources.class);
            resources.style().ensureInjected();
        }
        return resources;
    }

    private final Style style = getResources().style();

    private final DivElement breadcrumDiv;

    public BreadcrumbPane() {
        breadcrumDiv = Document.get().createDivElement();
        breadcrumDiv.addClassName(style.event_header__breadcrumb());

        setElement(breadcrumDiv);
    }

    public void addBreadcrumbItem(String title, String link, final Runnable runnable) {
        addBreadcrumbItem(title, UriUtils.fromString(link), runnable);
    }

    public void addBreadcrumbItem(String title, SafeUri link, final Runnable runnable) {
        AnchorElement breadcrumItem = Document.get().createAnchorElement();
        breadcrumItem.setHref(link);
        breadcrumItem.setInnerText(title);
        breadcrumItem.addClassName(style.event_header__breadcrumb__item());

        breadcrumDiv.appendChild(breadcrumItem);

        Event.sinkEvents(breadcrumItem, Event.ONCLICK);
        Event.setEventListener(breadcrumItem, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (LinkUtil.handleLinkClick(event)) {
                    event.stopPropagation();
                    event.preventDefault();
                    runnable.run();
                }
            }
        });
    }

    public void clear() {
        breadcrumDiv.removeAllChildren();
    }
}
