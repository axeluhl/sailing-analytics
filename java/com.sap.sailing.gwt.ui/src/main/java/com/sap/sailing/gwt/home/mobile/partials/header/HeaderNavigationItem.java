package com.sap.sailing.gwt.home.mobile.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents an entry in the header's popup menu.
 */
public class HeaderNavigationItem extends Widget implements HasClickHandlers {

    @UiField
    AnchorElement linkUi;
    @UiField
    Element listitemUi;
    interface HeaderUiBinder extends UiBinder<AnchorElement, HeaderNavigationItem> {
    }
    
    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);
    
    public HeaderNavigationItem(String linkText, String link) {
        setElement(uiBinder.createAndBindUi(this));
        linkUi.setHref(link != null ? link : "#");
        linkUi.setTitle(linkText);
        listitemUi.setInnerText(linkText);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }
}
