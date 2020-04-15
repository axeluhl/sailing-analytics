package com.sap.sse.gwt.client.controls.datetime;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * Extension of {@link AbstractInput} for date inputs, providing a native input field of type "date", if supported.
 * Otherwise a {@link DateBox} with {@link PredefinedFormat#DATE_MEDIUM medium date format} is used as fallback widget.
 */
public class DateInput extends AbstractInput {

    /**
     * Created a new {@link DateInput} instance.
     */
    public DateInput() {
        super(DateTimeInputType.DATE.isSupported() ? NativeDateTimeInput.date() : new CustomDateBox());
    }

    private static class CustomDateBox extends DateBox {

        private final DateTimeFormat viewFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);

        private CustomDateBox() {
            setFormat(new DefaultFormat(viewFormat));
            addStyleName(DateTimeInputResources.INSTANCE.css().dateTimeInput());
            getElement().setAttribute("placeholder", viewFormat.getPattern());
        }

    }

}
