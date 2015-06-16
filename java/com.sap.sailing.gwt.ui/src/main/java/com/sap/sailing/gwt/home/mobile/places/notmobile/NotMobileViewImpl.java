package com.sap.sailing.gwt.home.mobile.places.notmobile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class NotMobileViewImpl extends Composite implements NotMobileView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, NotMobileViewImpl> {
    }

    private Presenter currentPresenter;

    @UiField
    protected Anchor gotoDesktopUi;

    @UiField
    protected Anchor goBackUi;

    public NotMobileViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("gotoDesktopUi")
    public void gotoEvents(ClickEvent e) {
        currentPresenter.gotoDesktopVersion();
    }

    @UiHandler("goBackUi")
    public void goBack(ClickEvent e) {
        currentPresenter.goBack();
    }
    
}
