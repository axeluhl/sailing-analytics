package com.sap.sse.gwt.client.controls.datetime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

/**
 * Factory class to create native input field of type "{@link #datetimeLocale(Accuracy) datetime-local}",
 * "{@link #date() date}" or "{@link #time(Accuracy) time}".
 */
class NativeDateTimeInput extends Widget implements DateTimeInput {

    private static final String ATTR_TYPE = "type", ATTR_STEP = "step";

    /**
     * Creates a native input field of type "date".
     * 
     * @return the newly created instance as {@link DateTimeInput} abstraction
     */
    static DateTimeInput date() {
        return new NativeDateTimeInput(DateTimeInputType.DATE, "", Accuracy.getDateFormat(), Collections.emptyList());
    }

    /**
     * Creates a native input field of type "time" which the provided {@link Accuracy accuracy}.
     * 
     * @param accuracy
     *            {@link Accuracy} for the new native input field
     * 
     * @return the newly created instance as {@link DateTimeInput} abstraction
     */
    static DateTimeInput time(Accuracy accuracy) {
        final List<DateTimeFormat> additionalFormats = new ArrayList<>();
        switch (accuracy) {
        case MILLISECONDS:
            additionalFormats.add(Accuracy.SECONDS.getTimeFormat());
        case SECONDS:
            additionalFormats.add(Accuracy.MINUTES.getTimeFormat());
        default:
            break;
        }
        return new NativeDateTimeInput(DateTimeInputType.TIME, accuracy.getStep(), accuracy.getTimeFormat(), additionalFormats);
    }

    /**
     * Creates a native input field of type "datetime-local" which the provided {@link Accuracy accuracy}.
     * 
     * @param accuracy
     *            {@link Accuracy} for the new native input field
     * 
     * @return the newly created instance as {@link DateTimeInput} abstraction
     */
    static DateTimeInput datetimeLocale(Accuracy accuracy) {
        final List<DateTimeFormat> additionalFormats = new ArrayList<>();
        switch (accuracy) {
        case MILLISECONDS:
            additionalFormats.add(Accuracy.SECONDS.getDatetimeFormat());
        case SECONDS:
            additionalFormats.add(Accuracy.MINUTES.getDatetimeFormat());
        default:
            break;
        }
        return new NativeDateTimeInput(DateTimeInputType.DATETIME_LOCAL, accuracy.getStep(),
                accuracy.getDatetimeFormat(), additionalFormats);
    }

    private final DateTimeFormat format;
    private final InputElement input;
    private final List<DateTimeFormat> additionalFormatsForParsing;

    private NativeDateTimeInput(DateTimeInputType inputType, String step, DateTimeFormat format, List<DateTimeFormat> additionalFormatsForParsing) {
        this.additionalFormatsForParsing = additionalFormatsForParsing;
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
        final String value = input.getValue();
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return this.format.parse(value);
        } catch (IllegalArgumentException exc) {
            for (DateTimeFormat additionalFormat : additionalFormatsForParsing) {
                try {
                    // at least Google Chrome omits the seconds, if they are 00 in input field
                    return additionalFormat.parse(value);
                } catch (IllegalArgumentException exc2) {
                }
            }
        }
        return null;
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
