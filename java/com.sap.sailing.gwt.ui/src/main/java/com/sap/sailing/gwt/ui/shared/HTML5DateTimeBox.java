package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

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

    public HTML5DateTimeBox(Format format) {
        delegate = new DateTimeBoxFallbackStrategie(format);
        setWidget(delegate.getWidget());
        sinkEvents(Event.ONCHANGE | Event.ONBLUR | Event.ONKEYUP);
    }

    @Override
    public void onBrowserEvent(Event event) {
        ValueChangeEvent.fire(HTML5DateTimeBox.this, getValue());
    }

    public void setValue(Date newValue) {
        delegate.setValue(newValue);
    }

    public Date getValue() {
        return delegate.getValue();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

}
