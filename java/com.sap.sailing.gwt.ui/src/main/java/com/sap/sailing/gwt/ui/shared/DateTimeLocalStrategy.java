package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.HTML5DateTimeBox.DateTimeBoxStrategy;
import com.sap.sailing.gwt.ui.shared.HTML5DateTimeBox.Format;

public class DateTimeLocalStrategy implements DateTimeBoxStrategy {

    private FlowPanel div;
    private InputElement input;
    private DateTimeFormat combiFormat;

    public DateTimeLocalStrategy(Format format) {
        // if (format == Format.YEAR_TO_DAY) {
        // combiFormat = DateTimeFormat.getFormat("yyyy-MM-dd");
        // } else if (format == Format.YEAR_TO_MINUTE) {
        // combiFormat = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm");
        // } else {
        combiFormat = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss");
        // }
        div = new FlowPanel();
        input = DOM.createElement("input").cast();
        input.setAttribute("type", "datetime-local");
        // use a style that looks similar to the DateBox
        input.getStyle().setPadding(3, Unit.PX);
        input.getStyle().setPaddingBottom(2, Unit.PX);
        input.getStyle().setBorderColor("#ccc");
        input.getStyle().setBorderWidth(1, Unit.PX);
        input.getStyle().setBorderStyle(BorderStyle.SOLID);
        input.getStyle().setFontSize(100, Unit.PCT);

        if (format == Format.YEAR_TO_SECOND) {
            input.setAttribute("step", "1");
        } else {
            input.setAttribute("step", "60");
        }
        // used to parse time and date in one pass, to workaround parsing time only formats containing the current
        // day
        div.getElement().appendChild(input);
    }

    @Override
    public Widget getWidget() {
        return div;
    }

    @Override
    public void setValue(Date newValue) {
        String stringNewValue = combiFormat.format(newValue);
        GWT.log("Raw value " + stringNewValue);
        input.setValue(stringNewValue);
    }

    @Override
    public Date getValue() {
        Date result = null;
        try {
            String stringValue = input.getValue();
            GWT.log("DateTime is " + stringValue);
            result = combiFormat.parse(stringValue);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return result;
    }

}
