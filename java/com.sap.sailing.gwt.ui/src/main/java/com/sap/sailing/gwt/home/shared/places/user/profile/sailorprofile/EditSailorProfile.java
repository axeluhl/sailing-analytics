package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEventsDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO.SingleEntry;
import com.sap.sailing.gwt.home.desktop.partials.desktopaccordion.DesktopAccordion;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.SailorProfileDetailsView;
import com.sap.sailing.gwt.home.shared.partials.editable.EditableSuggestedMultiSelectionCompetitor;
import com.sap.sailing.gwt.home.shared.partials.editable.InlineEditLabel;
import com.sap.sailing.gwt.home.shared.partials.listview.BoatClassListView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.events.SailorProfileEventsTable;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.statistic.SailorProfileStatisticTable;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util.Pair;

/**
 * Implementation of {@link EditSailorProfileView} where users can view the details of a SailorProfile and edit them.
 */
public class EditSailorProfile extends Composite implements EditSailorProfileView {

    private static SharedSailorProfileUiBinder uiBinder = GWT.create(SharedSailorProfileUiBinder.class);

    interface SharedSailorProfileUiBinder extends UiBinder<Widget, EditSailorProfile> {
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

    private final EditSailorProfileView.Presenter presenter;

    private FlagImageResolver flagImageResolver;

    public EditSailorProfile(EditSailorProfileView.Presenter presenter, FlagImageResolver flagImageResolver,
            SailorProfileDetailsView parent) {
        this.presenter = presenter;
        this.flagImageResolver = flagImageResolver;
        presenter.getDataProvider().setView(this);
        competitorSelectionUi = new EditableSuggestedMultiSelectionCompetitor(presenter, flagImageResolver);
        initWidget(uiBinder.createAndBindUi(this));
        SailorProfileResources.INSTANCE.css().ensureInjected();
        boatClassesUi.setText(i18n.boatClasses());
        setupTitleChangeListener();
        accordionPolarDiagramUi.setVisible(false);
    }

    private void setupTitleChangeListener() {
        // setup title change handler
        titleUi.addTextChangeHandler((text) -> {
            presenter.getDataProvider().updateTitle(text);
        });
    }

    @Override
    public void setEntry(SailorProfileDTO entry) {
        competitorSelectionUi.setSelectedItems(entry.getCompetitors());
        titleUi.setText(entry.getName());
        boatClassesUi.setItems(entry.getBoatclasses());
        accordionEventsUi.clear();

        // Get events
        presenter.getDataProvider().getEvents(entry.getKey(), new AsyncCallback<SailorProfileEventsDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(SailorProfileEventsDTO result) {
                for (ParticipatedEventDTO dto : result.getParticipatedEvents()) {
                    SailorProfileEventsTable table = new SailorProfileEventsTable(flagImageResolver,
                            presenter.getPlaceController(), dto);
                    accordionEventsUi.addWidget(table);
                }
            }
        });

        accordionStatisticsUi.clear();
        for (SailorProfileNumericStatisticType type : SailorProfileNumericStatisticType.values()) {
            SailorProfileStatisticTable table = new SailorProfileStatisticTable(flagImageResolver, type, i18n);
            accordionStatisticsUi.addWidget(table);
            presenter.getDataProvider().getStatisticFor(entry.getKey(), type,
                    new AsyncCallback<SailorProfileStatisticDTO>() {

                @Override
                public void onFailure(Throwable caught) {
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
}
