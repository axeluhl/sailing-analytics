package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.details.events;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedRegattaDTO;
import com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.SailorProfileMobileResources;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;

public class SailorProfileEventEntry extends Composite {

    private static SailorProfileOverviewEntryUiBinder uiBinder = GWT.create(SailorProfileOverviewEntryUiBinder.class);

    interface SailorProfileOverviewEntryUiBinder extends UiBinder<Widget, SailorProfileEventEntry> {
    }

    @UiField
    DivElement sectionTitleUi;

    @UiField
    HTMLPanel contentContainerRegattasUi;

    private final String eventId;

    private final PlaceController placeController;

    public SailorProfileEventEntry(ParticipatedEventDTO event, PlaceController placeController,
            FlagImageResolver flagImageResolver) {
        initWidget(uiBinder.createAndBindUi(this));
        SailorProfileMobileResources.INSTANCE.css().ensureInjected();
        this.placeController = placeController;
        this.eventId = event.getEventId();
        this.sectionTitleUi.setInnerText(event.getEventName());
        Button gotoEventButton = new Button(">");
        gotoEventButton.addStyleName(SharedResources.INSTANCE.mainCss().spacermarginbottomsmall());
        gotoEventButton.addStyleName(SharedResources.INSTANCE.mainCss().spacermargintopsmall());
        gotoEventButton.addClickHandler((e) -> placeController.goTo(new EventDefaultPlace(eventId)));

        sectionTitleUi.appendChild(gotoEventButton.getElement());
        buildRegattaUis(event.getParticipatedRegattas(), flagImageResolver);
    }

    private void buildRegattaUis(Iterable<ParticipatedRegattaDTO> regattas, FlagImageResolver flagImageResolver) {
        this.contentContainerRegattasUi.clear();
        for (ParticipatedRegattaDTO regatta : regattas) {
            this.contentContainerRegattasUi
                    .add(new SailorProfileRegattaEntry(regatta, placeController, flagImageResolver));
        }
    }

}
