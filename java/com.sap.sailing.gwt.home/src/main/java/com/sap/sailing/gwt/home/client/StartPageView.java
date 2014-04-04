package com.sap.sailing.gwt.home.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class StartPageView extends Composite implements StartPagePresenter.MyView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, StartPageView> {
    }

    @UiField TextBox testBox;
    @UiField Button gotoEventsPageButton;

    public StartPageView() {
        super();

        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void addToSlot(Object slot, IsWidget content) {
    }

    @Override
    public void removeFromSlot(Object slot, IsWidget content) {
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
    }

	@Override
	public TextBox getTestBox() {
		return testBox;
	}

	@Override
	public HasClickHandlers getGotoEventsPageButton() {
		return gotoEventsPageButton;
	}
}

