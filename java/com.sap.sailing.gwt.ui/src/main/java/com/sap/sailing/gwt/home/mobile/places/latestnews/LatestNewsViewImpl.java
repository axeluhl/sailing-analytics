package com.sap.sailing.gwt.home.mobile.places.latestnews;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LatestNewsViewImpl extends Composite implements LatestNewsView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, LatestNewsViewImpl> {
    }

    private Presenter currentPresenter;

    @UiField
    protected Anchor eventsLinkUi;

    public LatestNewsViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));

    }

    @UiHandler("eventsLinkUi")
    public void gotoEvents(ClickEvent e) {
        currentPresenter.gotoEvents();
    }
    
}
