package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.List;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.mobile.partials.eventheader.EventHeader;
import com.sap.sailing.gwt.home.mobile.partials.impressions.Impressions;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.regattaStatus.RegattaStatus;
import com.sap.sailing.gwt.home.mobile.partials.simpleinfoblock.SimpleInfoBlock;
import com.sap.sailing.gwt.home.mobile.partials.statisticsBox.StatisticsBox;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.UpdatesBox;
import com.sap.sailing.gwt.home.mobile.places.event.overview.EventOverviewStage;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewNewsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewStageAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventStatisticsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattasAndLiveRacesForEventAction;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;

public class EventViewImpl extends Composite implements EventView {
    private static final StringMessages MSG = StringMessages.INSTANCE;
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, EventViewImpl> {
    }

    @UiField(provided = true) EventHeader eventHeaderUi;
    @UiField Quickfinder quickFinderUi;
    @UiField SimpleInfoBlock sailorInfoUi;
    @UiField(provided = true) EventOverviewStage overviewStageUi;
    @UiField RegattaStatus regattaStatusUi;
    @UiField Impressions impressionsUi;
    @UiField StatisticsBox statisticsBoxUi;
    @UiField(provided = true) UpdatesBox updatesBoxUi;

    private Presenter currentPresenter;
    private MobilePlacesNavigator navigator;

    public EventViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        eventHeaderUi = new EventHeader(presenter.getCtx().getEventDTO());
        overviewStageUi = new EventOverviewStage(currentPresenter);
        updatesBoxUi = new UpdatesBox(presenter);
        initWidget(uiBinder.createAndBindUi(this));
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        UUID eventId = currentPresenter.getCtx().getEventDTO() .getId();
        refreshManager.add(overviewStageUi, new GetEventOverviewStageAction(eventId));
        refreshManager.add(regattaStatusUi, new GetRegattasAndLiveRacesForEventAction(eventId));
        refreshManager.add(updatesBoxUi, new GetEventOverviewNewsAction(presenter.getCtx().getEventDTO().getId()));
        refreshManager.add(statisticsBoxUi, new GetEventStatisticsAction(currentPresenter.getCtx().getEventDTO().getId()));
        impressionsUi.getElement().getStyle().setDisplay(Display.NONE);
    }

    @Override
    public void setSailorInfos(String description, String buttonLabel, String url) {
        sailorInfoUi.setDescription(SafeHtmlUtils.fromString(description.replace("\n", " ")));
        sailorInfoUi.setAction(buttonLabel, url);
    }
    
    @Override
    public void setQuickFinderValues(List<RegattaMetadataDTO> regattaMetadatas) {
        quickFinderUi.addPlaceholderItem(MSG.resultsQuickfinder(), null);
        for (RegattaMetadataDTO regattaMetadata : regattaMetadatas) {
            quickFinderUi.addItemToGroup(regattaMetadata.getBoatCategory(), regattaMetadata.getDisplayName(), regattaMetadata.getId());
        }
    }
    
    @Override
    public HasSelectionHandlers<String> getQuickfinder() {
        return quickFinderUi;
    }

    @Override
    public void setMediaForImpressions(int nrOfImages, int nrOfVideos, List<SailingImageDTO> images) {
        impressionsUi.getElement().getStyle().setDisplay(Display.BLOCK);
        impressionsUi.setStatistis(nrOfImages, nrOfVideos);
        impressionsUi.addImages(images);
        // TODO: desktop media navigation
        impressionsUi.setClickDestinaton(navigator.getHomeNavigation());
    }

    @Override
    public void setNavigator(MobilePlacesNavigator navigator) {
        this.navigator = navigator;
    }

}
