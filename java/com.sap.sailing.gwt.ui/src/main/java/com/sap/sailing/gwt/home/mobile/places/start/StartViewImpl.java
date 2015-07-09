package com.sap.sailing.gwt.home.mobile.places.start;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class StartViewImpl extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, StartViewImpl> {
    }

    private Presenter currentPresenter;

    @UiField
    protected Anchor eventsLinkUi;

    public StartViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));

    }

    @UiHandler("eventsLinkUi")
    public void gotoEvents(ClickEvent e) {
        currentPresenter.gotoEvents();
    }
    
}
