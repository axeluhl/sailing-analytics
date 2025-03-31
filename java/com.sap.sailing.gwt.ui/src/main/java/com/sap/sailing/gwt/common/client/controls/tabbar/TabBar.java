package com.sap.sailing.gwt.common.client.controls.tabbar;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.LinkUtil;

/**
 * Capsulates functionality for a tab bar.
 * 
 * @author zhorvath
 *
 */
public class TabBar<T> extends Widget implements HasSelectionHandlers<T> {

    private final TabBarResources.LocalCss style = TabBarResources.INSTANCE.css();

    private final List<T> tabObjects = new ArrayList<>();
    private final DivElement tabBarDiv;
    
    public TabBar() {
        style.ensureInjected();
        tabBarDiv = Document.get().createDivElement();
        tabBarDiv.addClassName(style.navbar());

        setElement(tabBarDiv);

        sinkEvents(Event.ONCLICK);
    }

    @Override
    public void onBrowserEvent(Event event) {
        EventTarget eventTarget = event.getEventTarget();

        if (Element.is(eventTarget)) {
            // An event target can be any JavaScript object, but we are interested in events related to DOM events only
            Element element = eventTarget.cast();

            if (LinkUtil.handleLinkClick(event)) {
                // The user clicked somewhere into the tab bar -> we have to find the tab that was clicked
                for (int i = 0; i < tabBarDiv.getChildCount(); i++) {
                    if (tabBarDiv.getChild(i).isOrHasChild(element)) {
                        event.stopPropagation();
                        event.preventDefault();
                        // the specific tab was clicked -> fire SelectionEvent with the associated place
                        GWT.log("Found " + tabObjects.get(i));
                        SelectionEvent.fire(this, tabObjects.get(i));
                        break;
                    }
                }
            }
        }

        super.onBrowserEvent(event);
    }

    /**
     * Selects the tab associated with the given place.
     * 
     * @param place
     *            the place that should get selected.
     */
    public void select(T tabObject) {
        GWT.log("Should activate: " + tabObject);
        // remove all previous selections
        for (int i = 0; i < tabBarDiv.getChildCount(); i++) {
            Element.as(tabBarDiv.getChild(i)).removeClassName(style.navbar_buttonactive());
        }
        // find the tab that has to be selected for the given place
        for (int i = 0; i < tabObjects.size(); i++) {
            if (tabObject.equals(tabObjects.get(i))) {
                GWT.log("Activate: " + tabObject);
                // found the associated tab -> set the selected style.
                Element.as(tabBarDiv.getChild(i)).addClassName(style.navbar_buttonactive());
            }
        }
    }

    /**
     * Adds a tab with an associated place to be fired when the tab is selected
     *
     * TODO: create custom widget for tab
     *
     * @param title
     *            the label of the tab header.
     * @param place
     *            the place that should be associated with the tab.
     */
    public void addTab(String title, T place, String link) {
        AnchorElement tabElement = Document.get().createAnchorElement();
        tabElement.setHref(link);
        tabElement.setInnerText(title);
        tabElement.addClassName(style.navbar_button());

        tabBarDiv.appendChild(tabElement);
        tabObjects.add(place);
    }

    /** {@inheritDoc} */
    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<T> handler) {
        return addHandler(handler, SelectionEvent.getType());
    }
}
