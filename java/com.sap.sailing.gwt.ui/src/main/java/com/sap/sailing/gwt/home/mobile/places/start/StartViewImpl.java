package com.sap.sailing.gwt.home.mobile.places.start;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.stage.Stage;

public class StartViewImpl extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, StartViewImpl> {
    }

    private Presenter currentPresenter;

    @UiField(provided = true)
    Stage stage;

    public StartViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        stage = new Stage(presenter.getNavigator());
        initWidget(uiBinder.createAndBindUi(this));
    }
}
