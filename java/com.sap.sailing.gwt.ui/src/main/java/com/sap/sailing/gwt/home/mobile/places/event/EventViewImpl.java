package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.Collection;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.mobile.partials.eventheader.EventHeader;
import com.sap.sailing.gwt.home.mobile.partials.impressions.Impressions;
import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.regattaStatus.RegattaStatus;
import com.sap.sailing.gwt.home.mobile.partials.simpleinfoblock.SimpleInfoBlock;
import com.sap.sailing.gwt.home.mobile.partials.statisticsBox.StatisticsBox;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.UpdatesBox;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.home.mobile.places.event.overview.EventOverviewStage;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewNewsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewStageAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventStatisticsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattasAndLiveRacesForEventAction;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;

public class EventViewImpl extends Composite implements EventView {
    private static final StringMessages MSG = StringMessages.INSTANCE;
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, EventViewImpl> {
    }

    @UiField(provided = true) EventHeader eventHeaderUi;
    @UiField Quickfinder quickFinderUi;
    @UiField SimpleInfoBlock simpleInfoUi;
    @UiField(provided = true) EventOverviewStage overviewStageUi;
    @UiField SimplePanel listContentUi;
    @UiField Impressions impressionsUi;
    @UiField(provided = true) StatisticsBox statisticsBoxUi;
    @UiField(provided = true) UpdatesBox updatesBoxUi;

    private final Presenter currentPresenter;
    private final RefreshManager refreshManager;

    public EventViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        this.refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        EventViewDTO event = currentPresenter.getCtx().getEventDTO();
        eventHeaderUi = new EventHeader(event);
        this.setupOverviewStage(event.getId());
        this.setupUpdateBox(event.getId());
        this.setupStatisticsBox(event);
        initWidget(uiBinder.createAndBindUi(this));
        this.setupListContent(event);
        impressionsUi.getElement().getStyle().setDisplay(Display.NONE);
    }
    
    private void setupOverviewStage(UUID eventId) {
        overviewStageUi = new EventOverviewStage(currentPresenter);
        refreshManager.add(overviewStageUi, new GetEventOverviewStageAction(eventId));
    }
    
    private void setupListContent(EventViewDTO event) {
        String regattaId = currentPresenter.getCtx().getRegattaId();
        if (event.getType() == EventType.MULTI_REGATTA) {
            RegattaStatus regattaStatus = new RegattaStatus(currentPresenter);
            listContentUi.add(regattaStatus);
            refreshManager.add(regattaStatus, new GetRegattasAndLiveRacesForEventAction(event.getId()));
        } else {
            MinileaderboardBox miniLeaderboard = new MinileaderboardBox(false);
            miniLeaderboard.setAction(MSG.showAll(), currentPresenter.getRegattaMiniLeaderboardNavigation(regattaId));
            listContentUi.add(miniLeaderboard);
            refreshManager.add(miniLeaderboard, new GetMiniLeaderbordAction(event.getId(), regattaId, 3));
        }
    }
    
    private void setupUpdateBox(UUID eventId) {
        updatesBoxUi = new UpdatesBox(currentPresenter, refreshManager);
        if (currentPresenter.getCtx().getEventDTO().getState() == EventState.RUNNING) {
            refreshManager.add(updatesBoxUi, new GetEventOverviewNewsAction(eventId, 2));
        } else {
            updatesBoxUi.removeFromParent();
        }
    }
    
    private void setupStatisticsBox(EventViewDTO event) {
        statisticsBoxUi = new StatisticsBox(event.getType() == EventType.MULTI_REGATTA);
        refreshManager.add(statisticsBoxUi, new GetEventStatisticsAction(event.getId()));
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
        new QuickfinderPresenter(quickFinderUi, currentPresenter, regattaMetadatas);
    }
    
    @Override
    public void setQuickFinderValues(String seriesName, Collection<EventReferenceDTO> eventsOfSeries) {
        new QuickfinderPresenter(quickFinderUi, currentPresenter, seriesName, eventsOfSeries);
    }
    
    @Override
    public void hideQuickfinder() {
        quickFinderUi.removeFromParent();
    }
    
    @Override
    public void setMediaForImpressions(int nrOfImages, int nrOfVideos, Collection<SailingImageDTO> images) {
        impressionsUi.getElement().getStyle().setDisplay(Display.BLOCK);
        impressionsUi.setStatistis(nrOfImages, nrOfVideos);
        impressionsUi.addImages(images);
        // TODO: desktop media navigation
        impressionsUi.setClickDestinaton(currentPresenter.getMediaPageNavigation());
    }
}
