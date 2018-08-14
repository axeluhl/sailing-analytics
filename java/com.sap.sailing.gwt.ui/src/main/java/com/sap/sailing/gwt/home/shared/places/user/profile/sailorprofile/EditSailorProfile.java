package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfilesDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.SailorProfileDetailsView;
import com.sap.sailing.gwt.home.shared.partials.desktopaccordion.DesktopAccordion;
import com.sap.sailing.gwt.home.shared.partials.editable.EditableSuggestedMultiSelectionCompetitor;
import com.sap.sailing.gwt.home.shared.partials.editable.InlineEditLabel;
import com.sap.sailing.gwt.home.shared.partials.listview.BoatClassListView;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelection.SelectionChangeHandler;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.events.SailorProfileEventsTable;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;

/**
 * Implementation of {@link SharedSailorProfileView} where users can view the details of a SailorProfile and edit them.
 */
public class EditSailorProfile extends Composite implements SharedSailorProfileView {

    private static SharedSailorProfileUiBinder uiBinder = GWT.create(SharedSailorProfileUiBinder.class);

    interface SharedSailorProfileUiBinder extends UiBinder<Widget, EditSailorProfile> {
    }

    interface Style extends CssResource {
        String edgeToEdge();
    }

    @UiField
    Style style;
    @UiField
    SharedResources res;
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

    private final SharedSailorProfileView.Presenter presenter;

    private SailorProfileDTO entry;

    private boolean suppressEvents = false;

    public EditSailorProfile(SharedSailorProfileView.Presenter presenter, FlagImageResolver flagImageResolver,
            SailorProfileDetailsView parent) {
        this.presenter = presenter;
        competitorSelectionUi = new EditableSuggestedMultiSelectionCompetitor(presenter, flagImageResolver);
        initWidget(uiBinder.createAndBindUi(this));
        boatClassesUi.setText("Boatclasses");
        setupAccordions();
        setupTitleChangeListener();
        setupCompetitorChangeListener();
    }

    private void setupTitleChangeListener() {
        // setup title change handler
        titleUi.addTextChangeHandler((text) -> {
            entry.setName(text);
            onChange();
        });
    }

    private void onChange() {
        if (!suppressEvents) {
            this.presenter.getDataProvider().updateOrCreateSailorProfile(entry, new AsyncCallback<SailorProfilesDTO>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log(caught.getMessage(), caught);
                }

                @Override
                public void onSuccess(SailorProfilesDTO result) {
                    setEntry(result.getEntries().get(0));
                }
            });
        }
    }

    private void setupCompetitorChangeListener() {
        // setup competitor change handler
        competitorSelectionUi.addSelectionChangeHandler(new SelectionChangeHandler<SimpleCompetitorWithIdDTO>() {

            @Override
            public void onRemove(SimpleCompetitorWithIdDTO selectedItem) {
                entry.getCompetitors().remove(selectedItem);
                onChange();
            }

            @Override
            public void onClear() {
                final boolean fireEvent = entry.getCompetitors().size() == 0;
                entry.getCompetitors().clear();
                if (fireEvent) {
                    onChange();
                }
            }

            @Override
            public void onAdd(SimpleCompetitorWithIdDTO selectedItem) {
                entry.getCompetitors().add(selectedItem);
                onChange();
            }
        });
    }

    private void setupAccordions() {
        accordionEventsUi.setTitle("Events");
        accordionStatisticsUi.setTitle("Statistics");
        accordionPolarDiagramUi.setTitle("Polar Diagram");
    }

    public void setEdgeToEdge(boolean edgeToEdge) {
        competitorSelectionUi.setStyleName(style.edgeToEdge(), edgeToEdge);
        competitorSelectionUi.getElement().getParentElement().removeClassName(res.mediaCss().column());
    }

    public void setEntry(SailorProfileDTO entry) {
        suppressEvents = true;
        this.entry = entry;
        competitorSelectionUi.setSelectedItems(entry.getCompetitors());
        titleUi.setText(entry.getName());
        boatClassesUi.setItems(entry.getBoatclasses());
        suppressEvents = false;

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
