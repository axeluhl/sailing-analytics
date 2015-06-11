package com.sap.sailing.gwt.home.mobile.places.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.simpleinfoblock.SimpleInfoBlock;

public class EventViewImpl extends Composite implements EventView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, EventViewImpl> {
    }

    @UiField
    SimpleInfoBlock sailorInfoUi;

    private Presenter currentPresenter;


    public EventViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setSailorInfos(String description, String buttonLabel, String url) {
        sailorInfoUi.setDescription(SafeHtmlUtils.fromString(description.replace("\n", " ")));
        sailorInfoUi.setAction(buttonLabel, url);
    }

    
}
