package com.sap.sse.gwt.client.controls.datetime;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.ui.ValueBox;

/**
 * Extension of {@link AbstractInput} for time inputs, providing a native input field of type "time", if supported.
 * Otherwise a {@link ValueBox} for {@link Date dates} is used as fallback widget using a
 * {@link PredefinedFormat#TIME_SHORT short} or a {@link PredefinedFormat#TIME_MEDIUM medium} time format.
 */
public class TimeInput extends AbstractInput {

    /**
     * Created a new {@link TimeInput} instance which the provided {@link Accuracy accuracy}.
     * 
     * @param accuracy
     *            {@link Accuracy} for the new input
     */
    public TimeInput(Accuracy accuracy) {
        super(DateTimeInputType.TIME.isSupported() ? NativeDateTimeInput.time(accuracy)
                : new TimeBox(new TimeConverter(accuracy)));
    }

    private static class TimeBox extends ValueBox<Date> {

        private TimeBox(TimeConverter converter) {
            super(Document.get().createElement(InputElement.TAG), converter, converter);
            addStyleName(DateTimeInputResources.INSTANCE.css().dateTimeInput());
            getElement().setAttribute("placeholder", converter.viewFormat.getPattern());
        }

    }

    private static class TimeConverter extends AbstractRenderer<Date> implements Parser<Date> {

        private static final DateTimeFormat VIEW_FORMAT_SHORT = DateTimeFormat.getFormat(PredefinedFormat.TIME_SHORT);
        private static final DateTimeFormat VIEW_FORMAT_MEDIUM = DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM);
        private static final String EMPTY_VALUE = "";

        private final DateTimeFormat viewFormat;

        private TimeConverter(Accuracy accuracy) {
            this.viewFormat = accuracy == Accuracy.SECONDS ? VIEW_FORMAT_MEDIUM : VIEW_FORMAT_SHORT;
        }

        @Override
        public String render(Date object) {
            return Objects.isNull(object) ? EMPTY_VALUE : viewFormat.format(object);
        }

        @Override
        public Date parse(CharSequence text) throws ParseException {
            if (EMPTY_VALUE.equals(text.toString())) {
                return null;
            }

            try {
                return viewFormat.parse(text.toString());
            } catch (IllegalArgumentException e) {
                throw new ParseException(e.getMessage(), 0);
            }
        }

    }
}
