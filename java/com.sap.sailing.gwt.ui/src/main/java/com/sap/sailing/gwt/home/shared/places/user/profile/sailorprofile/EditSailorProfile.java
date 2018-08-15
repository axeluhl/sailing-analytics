package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.SailorProfileDetailsView;
import com.sap.sailing.gwt.home.shared.partials.desktopaccordion.DesktopAccordion;
import com.sap.sailing.gwt.home.shared.partials.editable.EditableSuggestedMultiSelectionCompetitor;
import com.sap.sailing.gwt.home.shared.partials.editable.InlineEditLabel;
import com.sap.sailing.gwt.home.shared.partials.listview.BoatClassListView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.events.SailorProfileEventsTable;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;

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

    public EditSailorProfile(EditSailorProfileView.Presenter presenter, FlagImageResolver flagImageResolver,
            SailorProfileDetailsView parent) {
        this.presenter = presenter;
        presenter.getDataProvider().setView(this);
        competitorSelectionUi = new EditableSuggestedMultiSelectionCompetitor(presenter, flagImageResolver);
        initWidget(uiBinder.createAndBindUi(this));
        boatClassesUi.setText(i18n.boatClasses());
        setupTitleChangeListener();
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

        // Get events
        presenter.getDataProvider().getEvents(entry.getKey(), new AsyncCallback<Iterable<ParticipatedEventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(Iterable<ParticipatedEventDTO> result) {
                for (ParticipatedEventDTO dto : result) {
                    SailorProfileEventsTable table = new SailorProfileEventsTable();
                    table.setController(presenter.getPlaceController());
                    table.setEvent(dto);
                    table.addStyleName(SharedResources.INSTANCE.mediaCss().column());
                    table.addStyleName(SharedResources.INSTANCE.mediaCss().small12());
                    table.addStyleName(SharedResources.INSTANCE.mainCss().spacermargintopmediumsmall());
                    accordionEventsUi.addWidget(table);
                }
            }
        });
    }
}
