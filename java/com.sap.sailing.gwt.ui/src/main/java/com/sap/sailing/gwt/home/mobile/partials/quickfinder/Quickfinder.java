package com.sap.sailing.gwt.home.mobile.partials.quickfinder;

import java.util.LinkedHashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.OptGroupElement;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.places.RegattaLeaderboardNavigationProvider;

public class Quickfinder extends Widget implements HasSelectionHandlers<String> {
    private static QuickfinderUiBinder uiBinder = GWT.create(QuickfinderUiBinder.class);

    interface QuickfinderUiBinder extends UiBinder<Element, Quickfinder> {
    }

    private final LinkedHashMap<String, OptGroupElement> groups = new LinkedHashMap<String, OptGroupElement>();

    @UiField
    protected SelectElement selectUi;
    
    public Quickfinder(final RegattaLeaderboardNavigationProvider presenter) {
        setElement(uiBinder.createAndBindUi(this));
        Event.sinkEvents(selectUi, Event.ONCHANGE);
        Event.setEventListener(selectUi, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                OptionElement oe = event.getEventTarget().cast();
                presenter.getRegattaMiniLeaderboardNavigation(oe.getValue()).goToPlace();
            }
        });        
    }
    
    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<String> handler) {
        return addHandler(handler, SelectionEvent.getType());
    }

    public void addPlaceholderItem(String label, String value) {
        addItemToElement(selectUi, label, value, true, true);
    }

    public void addItem(String label, String value) {
        addItemToElement(selectUi, label, value, false, false);
    }

    public void addItemToGroup(String groupLabel, String itemLabel, String value) {
        OptGroupElement oge = createorFindGroup(groupLabel);
        addItemToElement(oge, itemLabel, value, false, false);
    }

    public void addGroup(String label) {
        if (groups.containsKey(label)) {
            return;
        }
        createorFindGroup(label);
    }

    private OptGroupElement createorFindGroup(String label) {
        OptGroupElement oge = groups.get(label);
        if (oge == null) {
            oge = Document.get().createOptGroupElement();
            oge.setLabel(label);
            groups.put(label, oge);
            selectUi.appendChild(oge);
        }
        return oge;
    }
    
    private void addItemToElement(Element parent, String label, String value, boolean selected, boolean disabled) {
        OptionElement oe = Document.get().createOptionElement();
        oe.setLabel(label);
        oe.setValue(value);
        oe.setDefaultSelected(selected);
        oe.setDisabled(disabled);
        parent.appendChild(oe);
    }

}
