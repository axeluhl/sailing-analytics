package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.details.events;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedRegattaDTO;
import com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.SailorProfileMobileResources;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.shared.resources.SharedHomeResources;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;

public class SailorProfileEventEntry extends Composite {

    private static SailorProfileOverviewEntryUiBinder uiBinder = GWT.create(SailorProfileOverviewEntryUiBinder.class);

    interface SailorProfileOverviewEntryUiBinder extends UiBinder<Widget, SailorProfileEventEntry> {
    }

    @UiField
    DivElement sectionTitleUi;

    @UiField
    HTMLPanel sectionTitleContainerUi;

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
        Image img = new Image(SharedHomeResources.INSTANCE.arrowDownWhite());
        img.addStyleName(SailorProfileMobileResources.INSTANCE.css().gotoEventButton());
        img.addClickHandler((e) -> placeController.goTo(new EventDefaultPlace(eventId)));

        sectionTitleContainerUi.add(img);
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
