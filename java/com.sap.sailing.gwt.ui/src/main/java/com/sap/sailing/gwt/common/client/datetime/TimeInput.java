package com.sap.sailing.gwt.common.client.datetime;

import java.text.ParseException;
import java.util.Date;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.ui.ValueBox;

public class TimeInput extends AbstractInput {

    public TimeInput(Accuracy accuracy) {
        super(DateTimeInputType.TIME.isSupported() ? NativeDateTimeInput.time(accuracy)
                : new TimeBox(new TimeConverter(accuracy)));
    }

    private static class TimeBox extends ValueBox<Date> {

        private TimeBox(TimeConverter converter) {
            super(Document.get().createElement(InputElement.TAG), converter, converter);
            addStyleName(DateTimeInputResources.INSTANCE.css().dateTimeInput());
        }

    }

    private static class TimeConverter extends AbstractRenderer<Date> implements Parser<Date> {

        private final DateTimeFormat timeFormat;

        private TimeConverter(Accuracy accuracy) {
            this.timeFormat = accuracy.getTimeFormat();
        }

        @Override
        public String render(Date object) {
            return timeFormat.format(object);
        }

        @Override
        public Date parse(CharSequence text) throws ParseException {
            if ("".equals(text.toString())) {
                return null;
            }

            try {
                return timeFormat.parse(text.toString());
            } catch (IllegalArgumentException e) {
                throw new ParseException(e.getMessage(), 0);
            }
        }

    }
}
