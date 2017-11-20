package com.sap.sailing.gwt.common.client.datetime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlowPanel;

public class DateAndTimeInput extends AbstractInput {

    public DateAndTimeInput(Accuracy accuracy) {
        super(DateTimeInputType.DATETIME_LOCAL.isSupported() ? NativeDateTimeInput.datetimeLocale(accuracy)
                : new DateAndTimePanel(accuracy));
    }

    private static class DateAndTimePanel extends FlowPanel implements DateTimeInput {
        private final DateInput dateInput;
        private final TimeInput timeInput;

        private final DateTimeFormat dateFormat, timeFormat, datetimeFormat;

        public DateAndTimePanel(Accuracy accuracy) {
            add(this.dateInput = new DateInput());
            add(this.timeInput = new TimeInput(accuracy));
            dateInput.addStyleName(DateTimeInputResources.INSTANCE.css().dateTimeInput());
            timeInput.addStyleName(DateTimeInputResources.INSTANCE.css().dateTimeInput());
            this.dateFormat = DateInput.DATE_FORMAT;
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
            final Date dateValue = dateInput.getValue(), timeValue = timeInput.getValue();
            if (Objects.nonNull(dateValue) && Objects.nonNull(timeValue)) {
                final String dateString = dateFormat.format(dateInput.getValue());
                final String timeString = timeFormat.format(timeInput.getValue());
                final String dateAndTimeString = dateString + "T" + timeString;
                return datetimeFormat.parse(dateAndTimeString);
            }
            return null;
        }

        @Override
        public void setValue(Date value) {
            this.dateInput.setValue(value);
            this.timeInput.setValue(value);
        }

    }

}
