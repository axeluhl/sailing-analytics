package com.sap.sailing.gwt.common.client.datetime;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.datepicker.client.DateBox;

public class DateInput extends AbstractInput {

    public DateInput() {
        super(DateTimeInputType.TIME.isSupported() ? NativeDateTimeInput.date() : new CustomDateBox());
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
