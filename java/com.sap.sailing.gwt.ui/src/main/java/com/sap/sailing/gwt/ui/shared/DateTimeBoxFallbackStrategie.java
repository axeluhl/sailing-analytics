package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import com.sap.sailing.gwt.ui.shared.HTML5DateTimeBox.DateTimeBoxStrategy;
import com.sap.sailing.gwt.ui.shared.HTML5DateTimeBox.Format;

public class DateTimeBoxFallbackStrategie extends Composite implements DateTimeBoxStrategy {

    private static DateTimeBoxFallbackUiBinder uiBinder = GWT.create(DateTimeBoxFallbackUiBinder.class);

    interface DateTimeBoxFallbackUiBinder extends UiBinder<Widget, DateTimeBoxFallbackStrategie> {
    }

    private final DateTimeFormat dateFormat;
    private final DateTimeFormat timeFormat;
    private final DateTimeFormat combiFormat;

    @UiField
    InputElement timebox;
    @UiField
    DateBox datebox;

    public DateTimeBoxFallbackStrategie(Format format) {
        initWidget(uiBinder.createAndBindUi(this));
        // parser for low level, RFC compatible format
        dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd");
        datebox.setFormat(new DefaultFormat(dateFormat));
        datebox.getElement().setAttribute("placeholder", dateFormat.getPattern());

        datebox.ensureDebugId("datebox");
        UIObject.ensureDebugId(timebox, "timebox");
        
        if (format != Format.YEAR_TO_DAY) {
            timeFormat = DateTimeFormat.getFormat(format == Format.YEAR_TO_MINUTE ? "HH:mm" : "HH:mm:ss");
            timebox.setAttribute("placeholder", timeFormat.getPattern());
            if (format == Format.YEAR_TO_SECOND) {
                timebox.setAttribute("step", "1");
            } else {
                timebox.setAttribute("step", "60");
            }
            // used to parse time and date in one pass, to workaround parsing time only formats containing the current
            // day
            combiFormat = DateTimeFormat
                    .getFormat("yyyy-MM-dd " + (format == Format.YEAR_TO_MINUTE ? "HH:mm" : "HH:mm:ss"));
        } else {
            timebox.getStyle().setDisplay(Display.NONE);
            timeFormat = null;
            combiFormat = null;
        }

    }

    public Widget getWidget() {
        return this;
    }

    public Date getValue() {
        Date result = null;
        try {
            String rawDateValue = dateFormat.format(datebox.getValue());
            String rawTimeValue = timebox.getValue();
            if (timeFormat != null) {
                // it is not possible to parse the timeFormat, as it will add the current day, so we will parse a
                // combined format with the correct day and time
                result = combiFormat.parse(rawDateValue + " " + rawTimeValue);
            } else {
                result = dateFormat.parse(rawDateValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void setValue(Date newValue) {
        if(newValue != null){
            // remove the time part from the datepart, to reduce issus with browser compability
            Date dateFormated = dateFormat.parse(dateFormat.format(newValue));
            datebox.setValue(dateFormated);
            if (timeFormat != null) {
                String timeFormated = timeFormat.format(newValue);
                com.google.gwt.core.shared.GWT.log(timeFormated);
                timebox.setValue(timeFormated);
            }
        }else{
            datebox.setValue(null);
            if(timeFormat != null){
                timebox.setValue(null);
            }
        }
    }

}
