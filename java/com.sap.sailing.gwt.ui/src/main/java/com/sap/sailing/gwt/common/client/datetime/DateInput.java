package com.sap.sailing.gwt.common.client.datetime;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.datepicker.client.DateBox;

public class DateInput extends AbstractInput {

    static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd");

    public DateInput() {
        super(DateTimeInputType.TIME.isSupported() ? NativeDateTimeInput.date(DATE_FORMAT) : new CustomDateBox());
    }

    private static class CustomDateBox extends DateBox {

        private CustomDateBox() {
            setFormat(new DefaultFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM)));
            addStyleName(DateTimeInputResources.INSTANCE.css().dateTimeInput());
        }

    }

}
