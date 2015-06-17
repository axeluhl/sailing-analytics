package com.sap.sailing.gwt.home.mobile.places.notmobile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class NotMobileViewImpl extends Widget implements NotMobileView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<DivElement, NotMobileViewImpl> {
    }

    private Presenter currentPresenter;
    @UiField
    protected StringMessages i18n;
    @UiField
    protected AnchorElement gotoDesktopUi;
    @UiField
    protected ButtonElement goBackUi;
    @UiField
    protected Element notAvailableMessageUi;

    public NotMobileViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        setElement(uiBinder.createAndBindUi(this));
        String htmlMessage = i18n.notAvailableOnMobileMessage().replace("\n", "<br/>");
        notAvailableMessageUi.setInnerHTML(htmlMessage);
        DOM.sinkEvents(goBackUi, Event.ONCLICK);
        DOM.setEventListener(goBackUi, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (event.getTypeInt() == Event.ONCLICK) {
                    event.preventDefault();
                    currentPresenter.goBack();
                }
            }
        });
    }

    @Override
    public void setGotoDesktopUrl(String url) {
        gotoDesktopUi.setHref(url);
    }
    
}
