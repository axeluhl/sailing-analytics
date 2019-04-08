package com.sap.sailing.gwt.autoplay.client.places.screens.preevent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class IdlePreEventEntry extends Widget {
    private static final NextUpEntryUiBinder uiBinder = GWT.create(NextUpEntryUiBinder.class);

    interface NextUpEntryUiBinder extends UiBinder<Element, IdlePreEventEntry> {
    }

    @UiField
    DivElement newsTimeUi;

    public IdlePreEventEntry(String time) {
        setElement(uiBinder.createAndBindUi(this));
        newsTimeUi.setInnerText(time);
    }
}
