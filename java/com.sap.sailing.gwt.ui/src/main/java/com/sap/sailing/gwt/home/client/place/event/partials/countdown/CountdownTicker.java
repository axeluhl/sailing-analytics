package com.sap.sailing.gwt.home.client.place.event.partials.countdown;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.Countdown;
import com.sap.sailing.gwt.home.client.shared.Countdown.CountdownListener;
import com.sap.sailing.gwt.home.client.shared.Countdown.RemainingTime;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class CountdownTicker extends Composite {

    private static CountdownTickerUiBinder uiBinder = GWT.create(CountdownTickerUiBinder.class);

    interface CountdownTickerUiBinder extends UiBinder<Widget, CountdownTicker> {
    }
    
    @UiField DivElement majorContainer, majorValue, majorUnit;
    @UiField DivElement minorContainer, minorValue, minorUnit;

    private final Countdown countdown;

    public CountdownTicker(Date startTime) {
        initWidget(uiBinder.createAndBindUi(this));
        this.countdown = new Countdown(new MillisecondsTimePoint(startTime), new CountdownTickerListener());
    }

    private class CountdownTickerListener implements CountdownListener {
        @Override
        public void changed(RemainingTime major, RemainingTime minor) {
            update(major, majorContainer, majorValue, majorUnit);
            update(minor, minorContainer, minorValue, minorUnit);
        }

        private void update(RemainingTime time, DivElement container, DivElement value, DivElement unit) {
            if (time == null) {
                container.getStyle().setDisplay(Display.NONE);
                value.setInnerText(null);
                unit.setInnerText(null);
            } else {
                container.getStyle().clearDisplay();
                value.setInnerText(String.valueOf(time.value));
                unit.setInnerText(time.unitI18n());
            }
        }
    }

    @Override
    protected void onUnload() {
        countdown.cancel();
        super.onUnload();
    }

}
