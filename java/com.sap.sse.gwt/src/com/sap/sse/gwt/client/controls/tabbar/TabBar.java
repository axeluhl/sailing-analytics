package com.sap.sse.gwt.client.controls.tabbar;

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
import com.google.gwt.place.shared.Place;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

/**
 * Capsulates functionality for a tab bar.
 * 
 * @author zhorvath
 *
 */
public class TabBar extends Widget implements HasSelectionHandlers<Class<Place>> {

    interface Resources extends ClientBundle {
        @Source("TabBar.css")
        Style style();
    }

    interface Style extends CssResource {
        @ClassName("nav-bar")
        String tabBar();

        @ClassName("nav-bar__button")
        String tab();

        @ClassName("nav-bar__button--active")
        String selected();
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

    private final List<Class<Place>> places = new ArrayList<>();
    private final DivElement tabBarDiv;
    
    public TabBar() {
        tabBarDiv = Document.get().createDivElement();
        tabBarDiv.addClassName(style.tabBar());

        setElement(tabBarDiv);

        sinkEvents(Event.ONCLICK);
    }

    @Override
    public void onBrowserEvent(Event event) {
        EventTarget eventTarget = event.getEventTarget();

        if (Element.is(eventTarget)) {
            // An event target can be any JavaScript object, but we are interested in events related to DOM events only
            Element element = eventTarget.cast();

            if (event.getTypeInt() == Event.ONCLICK) {
                // The user clicked somewhere into the tab bar -> we have to find the tab that was clicked
                for (int i = 0; i < tabBarDiv.getChildCount(); i++) {
                    if (tabBarDiv.getChild(i).isOrHasChild(element)) {
                        event.stopPropagation();
                        event.preventDefault();
                        // the specific tab was clicked -> fire SelectionEvent with the associated place
                        GWT.log("Found " + places.get(i));
                        SelectionEvent.fire(this, places.get(i));
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
    public void select(Class<Place> place) {
        GWT.log("Should activate: " + place.getName());
        // remove all previous selections
        for (int i = 0; i < tabBarDiv.getChildCount(); i++) {
            Element.as(tabBarDiv.getChild(i)).removeClassName(style.selected());
        }
        // find the tab that has to be selected for the given place
        for (int i = 0; i < places.size(); i++) {
            if (place.equals(places.get(i))) {
                GWT.log("Activate: " + place.getName());
                // found the associated tab -> set the selected style.
                Element.as(tabBarDiv.getChild(i)).addClassName(style.selected());
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
    public void addTab(String title, Class<Place> place, String link) {
        AnchorElement tabElement = Document.get().createAnchorElement();
        tabElement.setHref(link);
        tabElement.setInnerText(title);
        tabElement.addClassName(style.tab());

        tabBarDiv.appendChild(tabElement);
        places.add(place);
    }

    /** {@inheritDoc} */
    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<Class<Place>> handler) {
        return addHandler(handler, SelectionEvent.getType());
    }
}
