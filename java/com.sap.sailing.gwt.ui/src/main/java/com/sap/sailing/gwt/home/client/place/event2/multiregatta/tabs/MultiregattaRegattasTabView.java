package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.model.EventDTO;
import com.sap.sailing.gwt.home.client.place.event2.model.RegattaDTO;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaView;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.MultiregattaTabView;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaOverviewPlace;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MultiregattaRegattasTabView extends Composite implements MultiregattaTabView<MultiregattaRegattasPlace> {
    
    @UiField FlowPanel content;
    private Presenter presenter;

    public MultiregattaRegattasTabView() {

    }

    @Override
    public Class<MultiregattaRegattasPlace> getPlaceClassForActivation() {
        return MultiregattaRegattasPlace.class;
    }
    
    @Override
    public void setPresenter(EventMultiregattaView.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void start(final MultiregattaRegattasPlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));
        
        EventDTO event = myPlace.getCtx().getEventDTO();
        for (final RegattaDTO regatta : event.getRegattas()) {
            PushButton button = new PushButton();
            button.setText(regatta.getName());
            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    presenter.navigateTo(new RegattaOverviewPlace(new EventContext(myPlace.getCtx().getEventDTO())
                            .withRegattaId(regatta.getName())));
                }
            });
            content.add(button);
        }
        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, MultiregattaRegattasTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public MultiregattaRegattasPlace placeToFire(EventContext ctx) {
        return new MultiregattaRegattasPlace(ctx);
    }

}