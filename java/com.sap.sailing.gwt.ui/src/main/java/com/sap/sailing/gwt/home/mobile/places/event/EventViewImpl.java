package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.mobile.partials.eventheader.EventHeader;
import com.sap.sailing.gwt.home.mobile.partials.impressions.Impressions;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.regattaStatus.RegattaStatus;
import com.sap.sailing.gwt.home.mobile.partials.simpleinfoblock.SimpleInfoBlock;
import com.sap.sailing.gwt.home.mobile.partials.statisticsBox.StatisticsBox;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.UpdatesBox;
import com.sap.sailing.gwt.home.mobile.places.event.overview.EventOverviewStage;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewNewsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewStageAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventStatisticsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattasAndLiveRacesForEventAction;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;

public class EventViewImpl extends Composite implements EventView {
    private static final StringMessages MSG = StringMessages.INSTANCE;
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, EventViewImpl> {
    }

    @UiField(provided = true) EventHeader eventHeaderUi;
    @UiField(provided = true) Quickfinder quickFinderUi;
    @UiField SimpleInfoBlock simpleInfoUi;
    @UiField(provided = true) EventOverviewStage overviewStageUi;
    @UiField(provided = true) RegattaStatus regattaStatusUi;
    @UiField Impressions impressionsUi;
    @UiField(provided = true) StatisticsBox statisticsBoxUi;
    @UiField(provided = true) UpdatesBox updatesBoxUi;

    private Presenter currentPresenter;

    public EventViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        eventHeaderUi = new EventHeader(presenter.getCtx().getEventDTO());
        quickFinderUi = new Quickfinder(currentPresenter);
        overviewStageUi = new EventOverviewStage(currentPresenter);
        regattaStatusUi = new RegattaStatus(currentPresenter);
        statisticsBoxUi = new StatisticsBox(presenter.getCtx().getEventDTO().getType() == EventType.MULTI_REGATTA);
        updatesBoxUi = new UpdatesBox(presenter);
        initWidget(uiBinder.createAndBindUi(this));
        impressionsUi.getElement().getStyle().setDisplay(Display.NONE);
        setupRefresh();
    }
    
    private void setupRefresh() {
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        UUID eventId = currentPresenter.getCtx().getEventDTO() .getId();
        refreshManager.add(overviewStageUi, new GetEventOverviewStageAction(eventId));
        refreshManager.add(regattaStatusUi, new GetRegattasAndLiveRacesForEventAction(eventId));
        if (currentPresenter.getCtx().getEventDTO().getState() == EventState.RUNNING) {
            refreshManager.add(updatesBoxUi, new GetEventOverviewNewsAction(currentPresenter.getCtx().getEventDTO().getId(), 2));
        } else {
            updatesBoxUi.removeFromParent();
        }
        refreshManager.add(statisticsBoxUi, new GetEventStatisticsAction(currentPresenter.getCtx().getEventDTO().getId()));
    }

    @Override
    public void setSailorInfos(String description, String buttonLabel, String url) {
        simpleInfoUi.setDescription(SafeHtmlUtils.fromString(description.replace("\n", " ")));
        simpleInfoUi.setAction(buttonLabel, url);
    }
    
    @Override
    public void setSeriesNavigation(String buttonLabel, PlaceNavigation<?> placeNavigation) {
        simpleInfoUi.setAction(buttonLabel, placeNavigation);
    }
    
    @Override
    public void setQuickFinderValues(Collection<RegattaMetadataDTO> regattaMetadatas) {
        if (regattaMetadatas == null) {
            quickFinderUi.removeFromParent();
            return;
        }
        quickFinderUi.addPlaceholderItem(MSG.resultsQuickfinder(), null);
        for (RegattaMetadataDTO regattaMetadata : regattaMetadatas) {
            quickFinderUi.addItemToGroup(regattaMetadata.getBoatCategory(), regattaMetadata.getDisplayName(), regattaMetadata.getId());
        }
    }
    
    @Override
    public void setMediaForImpressions(int nrOfImages, int nrOfVideos, List<SailingImageDTO> images) {
        impressionsUi.getElement().getStyle().setDisplay(Display.BLOCK);
        impressionsUi.setStatistis(nrOfImages, nrOfVideos);
        impressionsUi.addImages(images);
        // TODO: desktop media navigation
        impressionsUi.setClickDestinaton(currentPresenter.getMediaPageNavigation());
    }
}
