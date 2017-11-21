package com.sap.sailing.gwt.common.client.datetime;

import java.util.Date;
import java.util.Objects;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

class NativeDateTimeInput extends Widget implements DateTimeInput {

    private static final String ATTR_TYPE = "type", ATTR_STEP = "step";

    static DateTimeInput date() {
        return new NativeDateTimeInput(DateTimeInputType.DATE, "", Accuracy.getDateFormat());
    }

    static DateTimeInput time(Accuracy accuracy) {
        return new NativeDateTimeInput(DateTimeInputType.TIME, accuracy.getStep(), accuracy.getTimeFormat());
    }

    static DateTimeInput datetimeLocale(Accuracy accuracy) {
        return new NativeDateTimeInput(DateTimeInputType.DATETIME_LOCAL, accuracy.getStep(),
                accuracy.getDatetimeFormat());
    }

    private final DateTimeFormat format;
    private final InputElement input;

    private NativeDateTimeInput(DateTimeInputType inputType, String step, DateTimeFormat format) {
        this.input = Document.get().createElement(InputElement.TAG).cast();
        this.input.setAttribute(ATTR_TYPE, inputType.getType());
        this.input.setAttribute(ATTR_STEP, step);
        this.input.addClassName(DateTimeInputResources.INSTANCE.css().dateTimeInput());
        this.format = format;
        setElement(input);
        sinkEvents(Event.ONCHANGE | Event.ONBLUR | Event.ONKEYUP);
    }

    @Override
    public final void onBrowserEvent(Event event) {
        // TODO: Cache latest value to avoid multiple ValueChangeEvents
        final int type = event.getTypeInt();
        if (type == Event.ONCHANGE || type == Event.ONBLUR || type == Event.ONKEYUP) {
            ValueChangeEvent.fire(this, getValue());
        }
        super.onBrowserEvent(event);
    }

    @Override
    public final Date getValue() {
        try {
            return this.format.parse(input.getValue());
        } catch (IllegalArgumentException exc) {
            return null;
        }
    }

    @Override
    public final void setValue(Date value) {
        this.input.setValue(Objects.isNull(value) ? null : format.format(value));
    }

    @Override
    public final HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

}
