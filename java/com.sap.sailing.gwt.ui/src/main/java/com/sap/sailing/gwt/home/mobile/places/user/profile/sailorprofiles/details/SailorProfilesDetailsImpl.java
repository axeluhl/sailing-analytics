package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.details;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.common.client.BoatClassImageResolver;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEventsDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailingProfileOverviewPresenter;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailorProfileView;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfileResources;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsView;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

/**
 * Implementation of {@link UserSettingsView} where users can change their preferred selections and notifications.
 */
public class SailorProfilesDetailsImpl extends Composite implements SailorProfileView, EditSailorProfileView {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, SailorProfilesDetailsImpl> {
    }

    @UiField
    FlowPanel contentUi;

    @UiField
    SectionHeaderContent competitorsUi;

    @UiField
    HTMLPanel contentContainerCompetitorsUi;
    @UiField
    SectionHeaderContent imagesUi;

    @UiField
    HTMLPanel contentContainerImagesUi;
    @UiField
    SectionHeaderContent boatclassesUi;

    @UiField
    HTMLPanel contentContainerBoatclassesUi;
    @UiField
    SectionHeaderContent eventsUi;

    @UiField
    HTMLPanel contentContainerEventsUi;
    @UiField
    SectionHeaderContent statisticsUi;

    @UiField
    HTMLPanel contentContainerStatisticsUi;
    @UiField
    SectionHeaderContent polarDiagramUi;

    @UiField
    HTMLPanel contentContainerPolarDiagramUi;

    private SailingProfileOverviewPresenter presenter;

    public SailorProfilesDetailsImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        competitorsUi.initCollapsibility(contentContainerCompetitorsUi.getElement(), false);
        imagesUi.initCollapsibility(contentContainerImagesUi.getElement(), false);
        boatclassesUi.initCollapsibility(contentContainerBoatclassesUi.getElement(), false);
        eventsUi.initCollapsibility(contentContainerEventsUi.getElement(), false);
        statisticsUi.initCollapsibility(contentContainerStatisticsUi.getElement(), false);
        polarDiagramUi.initCollapsibility(contentContainerPolarDiagramUi.getElement(), false);
    }

    @Override
    public void setPresenter(SailingProfileOverviewPresenter presenter) {
        this.presenter = presenter;
        presenter.getSharedSailorProfilePresenter().getDataProvider().setView(this);

    }

    @Override
    public NeedsAuthenticationContext getAuthenticationContext() {
        return null;
    }

    @Override
    public void setEntry(SailorProfileDTO entry) {
        setCompetitors(entry.getCompetitors());
        setBoatclasses(entry.getBoatclasses());

        // Get events
        presenter.getSharedSailorProfilePresenter().getDataProvider().getEvents(entry.getKey(),
                new AsyncCallback<SailorProfileEventsDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log(caught.getMessage(), caught);
                    }

                    @Override
                    public void onSuccess(SailorProfileEventsDTO result) {
                        setEvents(result.getParticipatedEvents());
                    }

                });
    }

    private void setCompetitors(Iterable<SimpleCompetitorWithIdDTO> competitors) {
        for (SimpleCompetitorWithIdDTO competitor : competitors) {
            IsWidget competitorWidget = new CompetitorWithClubnameItemDescription(competitor,
                    presenter.getFlagImageResolver());
            contentContainerCompetitorsUi.add(competitorWidget);
        }
    }

    private void setBoatclasses(Iterable<BoatClassDTO> boatclasses) {
        for (BoatClassDTO boatclass : boatclasses) {
            Element elem = DOM.createDiv();
            elem.setInnerSafeHtml(SailorProfileResources.TEMPLATES.buildBoatclassIcon(
                    BoatClassImageResolver.getBoatClassIconResource(boatclass.getName()).getSafeUri().asString()));
            elem.getStyle().setDisplay(Display.INLINE_BLOCK);
            contentContainerBoatclassesUi.getElement().appendChild(elem);
        }
    }

    private void setEvents(Iterable<ParticipatedEventDTO> participatedEvents) {
        // TODO
        // for (ParticipatedEventDTO event : participatedEvents) {
        // }
    }
}
