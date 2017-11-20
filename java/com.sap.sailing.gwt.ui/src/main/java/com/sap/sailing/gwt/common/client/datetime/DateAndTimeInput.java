package com.sap.sailing.gwt.common.client.datetime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class DateAndTimeInput extends Composite implements DateTimeInput {

    private final DateTimeInput delegate;

    public DateAndTimeInput(Accuracy accuracy) {
        this.delegate = DateTimeInputType.DATETIME_LOCAL.isSupported() ? new NativeDatetimeLocalInput(accuracy)
                : new DateAndTimePanel(accuracy);
        initWidget(delegate.asWidget());
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

    private class DateAndTimePanel extends FlowPanel implements DateTimeInput {
        private final DateInput dateInput;
        private final TimeInput timeInput;

        private final DateTimeFormat dateFormat, timeFormat, datetimeFormat;

        public DateAndTimePanel(Accuracy accuracy) {
            add(this.dateInput = new DateInput());
            add(this.timeInput = new TimeInput(accuracy));
            this.dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd");
            this.timeFormat = accuracy.getTimeFormat();
            this.datetimeFormat = accuracy.getDatetimeFormat();
        }

        @Override
        public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
            final ValueChangeHandler<Date> internalHandler = event -> ValueChangeEvent.fire(this, getValue());
            final List<HandlerRegistration> handlerRegistrations = new ArrayList<>(3);
            handlerRegistrations.add(dateInput.addValueChangeHandler(internalHandler));
            handlerRegistrations.add(timeInput.addValueChangeHandler(internalHandler));
            handlerRegistrations.add(addHandler(handler, ValueChangeEvent.getType()));
            return () -> handlerRegistrations.forEach(HandlerRegistration::removeHandler);
        }

        @Override
        public Date getValue() {
            final String dateString = dateFormat.format(dateInput.getValue());
            final String timeString = timeFormat.format(timeInput.getValue());
            final String dateAndTimeString = dateString + "T" + timeString;
            return datetimeFormat.parse(dateAndTimeString);
        }

        @Override
        public void setValue(Date value) {
            this.dateInput.setValue(value);
            this.timeInput.setValue(value);
        }

    }

}
