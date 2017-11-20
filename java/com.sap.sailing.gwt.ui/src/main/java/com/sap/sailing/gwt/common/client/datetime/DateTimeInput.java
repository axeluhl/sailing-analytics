package com.sap.sailing.gwt.common.client.datetime;

import java.util.Date;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

public interface DateTimeInput extends IsWidget, HasValue<Date> {

    public enum Accuracy {
        MINUTES(60, "yyyy-MM-dd'T'HH:mm", "HH:mm"), SECONDS(1, "yyyy-MM-dd'T'HH:mm:ss", "HH:mm:ss");

        private static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd");

        private final String step;
        private final DateTimeFormat datetimeFormat, timeFormat;

        private Accuracy(int step, String datetimeFormat, String timeFormat) {
            this.step = String.valueOf(step);
            this.datetimeFormat = DateTimeFormat.getFormat(datetimeFormat);
            this.timeFormat = DateTimeFormat.getFormat(timeFormat);
        }

        public static DateTimeFormat getDateFormat() {
            return DATE_FORMAT;
        }

        public String getStep() {
            return step;
        }

        public DateTimeFormat getDatetimeFormat() {
            return datetimeFormat;
        }

        public DateTimeFormat getTimeFormat() {
            return timeFormat;
        }
    }

    @Override
    default void setValue(Date value, boolean fireEvents) {
        this.setValue(value);
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

}
