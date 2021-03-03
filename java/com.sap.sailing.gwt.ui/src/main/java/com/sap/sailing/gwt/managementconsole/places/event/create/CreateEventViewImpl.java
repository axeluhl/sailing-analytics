package com.sap.sailing.gwt.managementconsole.places.event.create;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.partials.inputs.listofstrings.ListOfStringsInput;
import com.sap.sailing.gwt.managementconsole.places.UiUtils;
import com.sap.sse.gwt.client.controls.datetime.DateInput;

public class CreateEventViewImpl extends Composite implements CreateEventView {

    interface CreateEventViewUiBinder extends UiBinder<Widget, CreateEventViewImpl> {}
    private static CreateEventViewUiBinder uiBinder = GWT.create(CreateEventViewUiBinder.class);

    @UiField
    Button createEventButton;
    @UiField
    Anchor back;
    @UiField
    DateInput dateInput;
    @UiField
    ListOfStringsInput courseAreasInput;
    @UiField
    InputElement venueInput, nameInput;
    
    private Presenter presenter;
    
    public CreateEventViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        back.addClickHandler(e -> presenter.cancelCreateEvent());
        createEventButton.addClickHandler(e -> validateAndCreateEvent());
    }

    private void validateAndCreateEvent() {
        if (validate()) {
            presenter.createEvent(nameInput.getValue(), venueInput.getValue(), dateInput.getValue(), courseAreasInput.getNotEmptyValues());
        }
    }
    
    private boolean validate() {
        return UiUtils.isNotBlank(nameInput.getValue())
                && UiUtils.isNotBlank(venueInput.getValue())
                && !courseAreasInput.getNotEmptyValues().isEmpty();
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