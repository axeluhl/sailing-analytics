package com.sap.sailing.gwt.common.client.datetime;

import java.util.Date;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

public interface DateTimeInput extends IsWidget, HasValue<Date> {

    enum Accuracy {
        MINUTES(60, "yyyy-MM-dd'T'HH:mm", "HH:mm"), SECONDS(1, "yyyy-MM-dd'T'HH:mm:ss", "HH:mm:ss");

        private final String step, datetimeFormat, timeFormat;

        private Accuracy(int step, String datetimeFormat, String timeFormat) {
            this.step = String.valueOf(step);
            this.datetimeFormat = datetimeFormat;
            this.timeFormat = timeFormat;
        }

        public String getStep() {
            return step;
        }

        public String getDatetimeFormat() {
            return datetimeFormat;
        }

        public String getTimeFormat() {
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
