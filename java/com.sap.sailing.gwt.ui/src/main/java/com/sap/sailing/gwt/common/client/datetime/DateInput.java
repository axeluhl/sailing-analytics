package com.sap.sailing.gwt.common.client.datetime;

import java.util.Date;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.datepicker.client.DateBox;

public class DateInput extends Composite implements DateTimeInput {

    private final HasValue<Date> delegate;

    public DateInput() {
        if (DateTimeInputType.TIME.isSupported()) {
            final NativeDateInput input = new NativeDateInput();
            initWidget(input);
            this.delegate = input;
        } else {
            final DateBox input = new DateBox();
            initWidget(input);
            this.delegate = input;
        }
    }

    @Override
    public Date getValue() {
        return delegate.getValue();
    }

    @Override
    public void setValue(Date value) {
        this.delegate.setValue(value);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
        return delegate.addValueChangeHandler(handler);
    }

}
