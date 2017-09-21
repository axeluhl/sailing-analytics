package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class HTML5DateTimeBox extends FocusPanel implements HasAllKeyHandlers, HasValueChangeHandlers<Date> {
    public static enum ViewMode {
        HOUR, DAY, MONTH, YEAR, DECADE
    };

    public static enum Format {
        YEAR_TO_MINUTE, YEAR_TO_DAY, YEAR_TO_SECOND
    };

    interface DateTimeBoxStrategy {

        Widget getWidget();

        void setValue(Date newValue);

        Date getValue();

    }

    private final DateTimeBoxStrategy delegate;
    private DateTimeFormat dateFormat;
    private DateBox datePart;
    private InputElement timePart;
    private DateTimeFormat timeFormat;
    private DateTimeFormat combiFormat;

    public HTML5DateTimeBox(Format format) {
        boolean supportsDateTime = supportsType("datetime-locale");
        GWT.log("datetime-local " + supportsDateTime);

        // TODO deactivated, because currently the detection is not reliable in different browsers and a format cannot
        // be set yet
        // if (supportsDateTime) {
        // delegate = new DateTimeLocalStrategy(format);
        // setWidget(delegate.getWidget());
        // } else {
        delegate = new DateTimeBoxFallbackStrategie(format);
        setWidget(delegate.getWidget());
        // }
        sinkEvents(Event.ONCHANGE | Event.ONBLUR | Event.ONKEYUP);
    }

    @Override
    public void onBrowserEvent(Event event) {
        ValueChangeEvent.fire(HTML5DateTimeBox.this, getValue());
    }

    public void setValue(Date newValue) {
        delegate.setValue(newValue);
    }

    public Element getPicker() {
        return datePart.getElement();
    }

    public Date getValue() {
        return delegate.getValue();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public native boolean supportsType(String type) /*-{
		var input = document.createElement("input");
		input.setAttribute("type", type);
		var desiredType = input.getAttribute('type');
		console.log("desiredtype " + desiredType + " actual " + input.type);
		var supported = false;
		if (input.type === desiredType) {
			supported = true;
		}
		input.value = 'Hello world';
		console.log("invalue" + input.value);
		var helloWorldAccepted = (input.value === 'Hello world');
		if (helloWorldAccepted) {
			supported = false;
		}
		input.value = '';
		return supported;
    }-*/;
}
