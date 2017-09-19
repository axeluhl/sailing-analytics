package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

public class HTML5DateTimeBox extends FocusPanel implements HasAllKeyHandlers, HasValueChangeHandlers<Date> {
    public static enum ViewMode {
        HOUR, DAY, MONTH, YEAR, DECADE
    };

    public static enum Format {
        YEAR_TO_MINUTE, YEAR_TO_DAY, YEAR_TO_SECOND
    };

    private DateTimeFormat dateFormat;
    private DateBox datePart;
    private InputElement timePart;
    private DateTimeFormat timeFormat;
    private DateTimeFormat combiFormat;

    public HTML5DateTimeBox(Format format) {
        //parser for low level, RFC compatible format
        dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd");

        FlowPanel div = new FlowPanel();
        datePart = new DateBox();
        datePart.setFormat(new DefaultFormat(dateFormat));
        div.add(datePart);

        if (format != Format.YEAR_TO_DAY) {
            timeFormat = DateTimeFormat.getFormat(format == Format.YEAR_TO_MINUTE ? "HH:mm" : "HH:mm:ss");
            timePart = DOM.createElement("input").cast();
            timePart.setAttribute("type", "time");
            if (format == Format.YEAR_TO_SECOND) {
                timePart.setAttribute("step", "1");
            } else {
                timePart.setAttribute("step", "60");
            }
            // used to parse time and date in one pass, to workaround parsing time only formats containing the current
            // day
            combiFormat = DateTimeFormat
                    .getFormat("yyyy-MM-dd " + (format == Format.YEAR_TO_MINUTE ? "HH:mm" : "HH:mm:ss"));
            div.getElement().appendChild(timePart);
        }

        sinkEvents(Event.ONCHANGE | Event.ONBLUR | Event.ONKEYUP);

        setWidget(div);
    }

    @Override
    public void onBrowserEvent(Event event) {
        ValueChangeEvent.fire(HTML5DateTimeBox.this, getValue());
    }

    public void setValue(Date initialValue) {
        // remove the time part from the datepart, to reduce issus with browser compability
        Date dateFormated = dateFormat.parse(dateFormat.format(initialValue));
        datePart.setValue(dateFormated);
        if (timeFormat != null) {
            String timeFormated = timeFormat.format(initialValue);
            com.google.gwt.core.shared.GWT.log(timeFormated);
            timePart.setAttribute("value", timeFormated);
        }
    }

    public Element getPicker() {
        return datePart.getElement();
    }

    public Date getValue() {
        Date result = null;
        try {

            String rawDateValue = dateFormat.format(datePart.getValue());
            String rawTimeValue = timePart.getValue();
            if (timeFormat != null) {
                // it is not possible to parse the timeFormat, as it will add the current day, so we will parse a
                // combined format with the correct day and time
                result = combiFormat.parse(rawDateValue + " " + rawTimeValue);
            } else {
                result = dateFormat.parse(rawDateValue);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

}
