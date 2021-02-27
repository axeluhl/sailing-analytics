package com.sap.sailing.gwt.managementconsole.places.regatta.create;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class AddRegattaViewImpl extends Composite implements AddRegattaView {

    interface AddRegattaViewUiBinder extends UiBinder<Widget, AddRegattaViewImpl> {
    }

    private static AddRegattaViewUiBinder uiBinder = GWT.create(AddRegattaViewUiBinder.class);

    @UiField
    Button addRegattaButton;
    @UiField
    Anchor back;
    
    private Presenter presenter;
    
    public AddRegattaViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        back.addClickHandler(e -> presenter.cancelAddRegatta());
        addRegattaButton.addClickHandler(e -> presenter.addRegatta());
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