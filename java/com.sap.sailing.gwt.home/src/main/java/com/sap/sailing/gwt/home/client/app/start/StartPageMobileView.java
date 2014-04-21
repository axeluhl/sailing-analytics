package com.sap.sailing.gwt.home.client.app.start;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.dto.EventDTO;

public class StartPageMobileView extends Composite implements StartPagePresenter.MyView {
    private static StartPageMobileViewUiBinder uiBinder = GWT.create(StartPageMobileViewUiBinder.class);

    interface StartPageMobileViewUiBinder extends UiBinder<Widget, StartPageMobileView> {
    }

    public StartPageMobileView() {
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
	public void setEvents(List<EventDTO> events) {
	}

}

