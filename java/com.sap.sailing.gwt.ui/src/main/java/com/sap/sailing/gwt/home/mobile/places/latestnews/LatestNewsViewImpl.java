package com.sap.sailing.gwt.home.mobile.places.latestnews;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.UpdatesBox;

public class LatestNewsViewImpl extends Composite implements LatestNewsView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, LatestNewsViewImpl> {
    }

    private Presenter currentPresenter;

    @UiField(provided = true)
    protected UpdatesBox updatesBox;

    public LatestNewsViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        updatesBox = new UpdatesBox(presenter);
        initWidget(uiBinder.createAndBindUi(this));
    }

    

}
