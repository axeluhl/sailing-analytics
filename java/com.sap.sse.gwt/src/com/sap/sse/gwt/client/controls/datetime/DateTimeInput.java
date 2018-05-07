package com.sap.sse.gwt.client.controls.datetime;

import java.util.Date;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface for date and/or time related input widgets holding a {@link Date date} value.
 * 
 * @see com.google.gwt.user.client.ui.IsWidget
 * @see com.google.gwt.user.client.ui.HasValue
 */
public interface DateTimeInput extends IsWidget, HasValue<Date> {

    /**
     * Enumeration describing the accuracy of an input field of type "datetime-local" or "time".
     */
    public enum Accuracy {
        MINUTES("60", "yyyy-MM-dd'T'HH:mm", "HH:mm"), SECONDS("1", "yyyy-MM-dd'T'HH:mm:ss", "HH:mm:ss"), MILLISECONDS("0.001", "yyyy-MM-dd'T'HH:mm:ss.SSS", "HH:mm:ss.SSS" );

        private static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd");

        private final DateTimeFormat datetimeFormat, timeFormat;
        private final String step;

        private Accuracy(String step, String datetimeFormat, String timeFormat) {
            this.step = step;
            this.datetimeFormat = DateTimeFormat.getFormat(datetimeFormat);
            this.timeFormat = DateTimeFormat.getFormat(timeFormat);
        }

        /**
         * @return the {@link DateTimeFormat} for date inputs (always the same)
         */
        public static DateTimeFormat getDateFormat() {
            return DATE_FORMAT;
        }

        /**
         * @return the {@link DateTimeFormat} for time inputs
         */
        public DateTimeFormat getTimeFormat() {
            return timeFormat;
        }

        /**
         * @return the {@link DateTimeFormat} for date and time inputs
         */
        public DateTimeFormat getDatetimeFormat() {
            return datetimeFormat;
        }

        /**
         * @return the "step" attribute value to set on input fields of type "datetime-local" or "time"
         */
        public String getStep() {
            return step;
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
