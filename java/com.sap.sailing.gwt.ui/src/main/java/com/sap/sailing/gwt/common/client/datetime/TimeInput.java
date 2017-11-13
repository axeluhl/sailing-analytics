package com.sap.sailing.gwt.common.client.datetime;

import java.text.ParseException;
import java.util.Date;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ValueBox;

public class TimeInput extends Composite implements DateTimeInput {

    private final HasValue<Date> delegate;

    public TimeInput(Accuracy accuracy) {
        if (DateTimeInputType.TIME.isSupported()) {
            final NativeTimeInput input = new NativeTimeInput(accuracy);
            initWidget(input);
            this.delegate = input;
        } else {
            final TimeConverter conv = new TimeConverter(accuracy);
            final ValueBox<Date> input = ValueBox.wrap(Document.get().createElement(InputElement.TAG), conv, conv);
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

    private class TimeConverter extends AbstractRenderer<Date> implements Parser<Date> {

        private final DateTimeFormat dateTimeFormat;

        private TimeConverter(Accuracy accuracy) {
            this.dateTimeFormat = DateTimeFormat.getFormat(accuracy.getTimeFormat());
        }

        @Override
        public String render(Date object) {
            return dateTimeFormat.format(object);
        }

        @Override
        public Date parse(CharSequence text) throws ParseException {
            if ("".equals(text.toString())) {
                return null;
            }

            try {
                return dateTimeFormat.parse(text.toString());
            } catch (IllegalArgumentException e) {
                throw new ParseException(e.getMessage(), 0);
            }
        }

    }
}
