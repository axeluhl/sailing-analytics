package com.sap.sailing.gwt.managementconsole.places.event.create;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class CreateEventViewImpl extends Composite implements CreateEventView{

    interface CreateEventViewUiBinder extends UiBinder<Widget, CreateEventViewImpl> {
    }

    private static CreateEventViewUiBinder uiBinder = GWT.create(CreateEventViewUiBinder.class);

    @UiField
    Button createEventButton;
    @UiField
    Anchor back;
    
    private Presenter presenter;
    
    public CreateEventViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        back.addClickHandler(e -> presenter.cancelCreateEvent());
        createEventButton.addClickHandler(e -> presenter.createEvent());
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onResize() {
        // TODO Auto-generated method stub
    }

}