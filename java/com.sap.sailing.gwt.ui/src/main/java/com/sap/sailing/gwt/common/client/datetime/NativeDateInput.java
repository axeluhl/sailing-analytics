package com.sap.sailing.gwt.common.client.datetime;

import java.util.Date;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Widget;

class NativeDateInput extends Widget implements DateTimeInput {

    private static final String ATTR_TYPE = "type";

    private final DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd");
    private final InputElement input;

    NativeDateInput() {
        this.input = Document.get().createElement(InputElement.TAG).cast();
        this.input.setAttribute(ATTR_TYPE, DateTimeInputType.DATE.getType());
        setElement(input);
    }

    @Override
    public Date getValue() {
        return this.format.parse(input.getValue());
    }

    @Override
    public void setValue(Date value) {
        this.input.setValue(format.format(value));
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
