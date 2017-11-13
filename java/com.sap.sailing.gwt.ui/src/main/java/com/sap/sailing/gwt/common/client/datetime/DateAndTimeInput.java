package com.sap.sailing.gwt.common.client.datetime;

import java.util.Date;

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
            this.timeFormat = DateTimeFormat.getFormat(accuracy.getTimeFormat());
            this.datetimeFormat = DateTimeFormat.getFormat(accuracy.getDatetimeFormat());
        }

        @Override
        public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
            return addHandler(handler, ValueChangeEvent.getType());
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
