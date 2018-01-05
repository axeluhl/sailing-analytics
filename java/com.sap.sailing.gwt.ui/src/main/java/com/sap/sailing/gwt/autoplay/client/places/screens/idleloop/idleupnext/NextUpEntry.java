package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.idleupnext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class NextUpEntry extends Widget {
    private static NextUpEntryUiBinder uiBinder = GWT.create(NextUpEntryUiBinder.class);

    interface NextUpEntryUiBinder extends UiBinder<Element, NextUpEntry> {
    }

    @UiField
    DivElement newsTimeUi;
    @UiField
    DivElement newsTextUi;

    public NextUpEntry(String time, String text) {
        setElement(uiBinder.createAndBindUi(this));
        newsTimeUi.setInnerText(time);
        newsTextUi.setInnerText(text);
    }
}
