package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEventsDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO.SingleEntry;
import com.sap.sailing.gwt.home.desktop.partials.desktopaccordion.DesktopAccordion;
import com.sap.sailing.gwt.home.desktop.partials.desktopaccordion.DesktopAccordion.AccordionExpansionListener;
import com.sap.sailing.gwt.home.desktop.partials.desktopaccordion.DesktopAccordionResources;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailorProfileDesktopResources;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.events.SailorProfileEventsTable;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.statistic.SailorProfileStatisticTable;
import com.sap.sailing.gwt.home.shared.partials.editable.EditableSuggestedMultiSelectionCompetitor;
import com.sap.sailing.gwt.home.shared.partials.editable.InlineEditLabel;
import com.sap.sailing.gwt.home.shared.partials.listview.BoatClassListView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileDetailsView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileResources;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

/**
 * Implementation of {@link EditSailorProfileDetailsView} where users can view the details of a SailorProfile and edit them.
 * The data is loaded when the accordion is opened for the first time.
 */
public class ShowAndEditSailorProfile extends Composite implements EditSailorProfileDetailsView {

    private static SharedSailorProfileUiBinder uiBinder = GWT.create(SharedSailorProfileUiBinder.class);

    interface SharedSailorProfileUiBinder extends UiBinder<Widget, ShowAndEditSailorProfile> {
    }

    @UiField
    StringMessages i18n;

    @UiField(provided = true)
    EditableSuggestedMultiSelectionCompetitor competitorSelectionUi;
    @UiField
    InlineEditLabel titleUi;
    @UiField
    BoatClassListView boatClassesUi;

    @UiField
    DesktopAccordion accordionEventsUi;
    @UiField
    DesktopAccordion accordionStatisticsUi;
    @UiField
    DesktopAccordion accordionPolarDiagramUi;

    Label eventsEmptyUi;

    private final EditSailorProfileDetailsView.Presenter presenter;

    private FlagImageResolver flagImageResolver;

    public ShowAndEditSailorProfile(EditSailorProfileDetailsView.Presenter presenter, FlagImageResolver flagImageResolver,
            SailorProfileDetailsView parent) {
        this.presenter = presenter;
        this.flagImageResolver = flagImageResolver;
        presenter.getDataProvider().setView(this);
        competitorSelectionUi = new EditableSuggestedMultiSelectionCompetitor(presenter.getCompetitorPresenter(),
                flagImageResolver);
        initWidget(uiBinder.createAndBindUi(this));
        SharedSailorProfileResources.INSTANCE.css().ensureInjected();
        SailorProfileDesktopResources.INSTANCE.css().ensureInjected();
        boatClassesUi.setText(i18n.boatClasses());
        accordionPolarDiagramUi.setVisible(false);
    }

    private void setupTitleChangeListener(final UUID uuid) {
        // setup title change handler
        titleUi.clearTextChangeHandlers();
        titleUi.addTextChangeHandler((text) -> {
            presenter.getDataProvider().updateTitle(uuid, text);
        });
    }

    @Override
    public void setEntry(SailorProfileDTO entry) {
        competitorSelectionUi.setSelectedItems(entry.getCompetitors());
        titleUi.setText(entry.getName());
        boatClassesUi.setEmptyMessage(StringMessages.INSTANCE.pleaseSelectCompetitorFirst());
        boatClassesUi.setItems(entry.getBoatclasses());
        accordionEventsUi.clear();

        // Get events asynchronously
        presenter.getDataProvider().getEvents(entry.getKey(), new AsyncCallback<SailorProfileEventsDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(SailorProfileEventsDTO result) {
                appendEmptyMessageIfNecessary(result);
                for (ParticipatedEventDTO dto : result.getParticipatedEvents()) {
                    SailorProfileEventsTable table = new SailorProfileEventsTable(flagImageResolver,
                            presenter.getPlaceController(), dto);
                    accordionEventsUi.addWidget(table);
                }
            }

            /** adds empty widget */
            private void appendEmptyMessageIfNecessary(SailorProfileEventsDTO result) {
                if (Util.isEmpty(result.getParticipatedEvents())) {
                    createEventsEmptyUiIfNecessary();
                    accordionEventsUi.addWidget(eventsEmptyUi);
                } else if (eventsEmptyUi != null && eventsEmptyUi.isAttached()) {
                    eventsEmptyUi.removeFromParent();
                }
            }

            /** creates empty widget */
            private void createEventsEmptyUiIfNecessary() {
                if (eventsEmptyUi == null) {
                    eventsEmptyUi = new Label(StringMessages.INSTANCE.noEventsFoundForCompetitors());
                    DesktopAccordionResources.INSTANCE.css().ensureInjected();
                    eventsEmptyUi.addStyleName(DesktopAccordionResources.INSTANCE.css().accordionEmptyMessage());
                }
            }
        });

        accordionStatisticsUi.clear();

        setupTables(entry);
        setupTitleChangeListener(entry.getKey());

    }

    /** create tables for statistic types */
    private void setupTables(SailorProfileDTO entry) {
        for (SailorProfileNumericStatisticType type : SailorProfileNumericStatisticType.values()) {
            SailorProfileStatisticTable table = new SailorProfileStatisticTable(flagImageResolver, type, i18n);
            accordionStatisticsUi.addWidget(table);
            if (accordionStatisticsUi.isExpanded()) {
                updateStatistic(entry, type, table);
            } else {
                // load the statistic data when the accordion is expanded for the first time
                accordionStatisticsUi.addAccordionListener(new AccordionExpansionListener() {
                    @Override
                    public void onExpansion(boolean expanded) {
                        if (expanded) {
                            updateStatistic(entry, type, table);
                        }
                    }

                });
            }
        }
    }

    /** retrieve and update the statistic for a given type of a sailor profile entry */
    private void updateStatistic(SailorProfileDTO entry, SailorProfileNumericStatisticType type,
            SailorProfileStatisticTable table) {
        presenter.getDataProvider().getStatisticFor(entry.getKey(), type,
                new AsyncCallback<SailorProfileStatisticDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Notification.notify(i18n.couldNotDetermineStatistic(), NotificationType.WARNING);
                    }

                    @Override
                    public void onSuccess(SailorProfileStatisticDTO answer) {
                        ArrayList<Pair<SimpleCompetitorWithIdDTO, SingleEntry>> data = new ArrayList<>();
                        for (Entry<SimpleCompetitorWithIdDTO, ArrayList<SingleEntry>> entry : answer.getResult()
                                .entrySet()) {
                            for (SingleEntry value : entry.getValue()) {
                                data.add(new Pair<SimpleCompetitorWithIdDTO, SingleEntry>(entry.getKey(), value));
                            }
                        }
                        table.setData(data);
                    }
                });
    }
}
