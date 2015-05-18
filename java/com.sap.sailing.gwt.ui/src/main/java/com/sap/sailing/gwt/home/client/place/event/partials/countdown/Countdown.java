package com.sap.sailing.gwt.home.client.place.event.partials.countdown;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.partials.countdown.CountdownResources.LocalCss;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewTickerStageDTO;

public class Countdown extends Composite {

    private static CountdownUiBinder uiBinder = GWT.create(CountdownUiBinder.class);

    interface CountdownUiBinder extends UiBinder<Widget, Countdown> {
    }

    private static final LocalCss CSS = CountdownResources.INSTANCE.css();
    
    @UiField SimplePanel tickerContainer;

    public Countdown() {
        CSS.ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setData(EventOverviewTickerStageDTO data) {
        this.tickerContainer.setWidget(new CountdownTicker(data.getStartTime()));
    }
}
