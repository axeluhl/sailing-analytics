package com.sap.sailing.gwt.home.mobile.places.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class EventViewImpl extends Composite implements EventView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, EventViewImpl> {
    }

    private Presenter currentPresenter;


    public EventViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
    }


    
}
