package com.sap.sailing.gwt.managementconsole.places.eventseries.create;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.places.UiUtils;

public class CreateEventSeriesViewImpl extends Composite implements CreateEventSeriesView {

    interface CreateEventSeriesViewUiBinder extends UiBinder<Widget, CreateEventSeriesViewImpl> {}
    private static CreateEventSeriesViewUiBinder uiBinder = GWT.create(CreateEventSeriesViewUiBinder.class);

    @UiField
    Button createEventSeriesButton;
    @UiField
    Anchor back;
    @UiField
    InputElement nameInput;
    
    private Presenter presenter;
    
    public CreateEventSeriesViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        back.addClickHandler(e -> presenter.cancelCreateEventSeries());
        createEventSeriesButton.addClickHandler(e -> validateAndCreateEventSeries());
    }

    private void validateAndCreateEventSeries() {
        if (validate()) {
            presenter.createEventSeries(nameInput.getValue());
        }
    }
    
    private boolean validate() {
        return UiUtils.isNotBlank(nameInput.getValue());
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