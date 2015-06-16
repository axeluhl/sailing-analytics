package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.mobile.partials.eventheader.EventHeader;
import com.sap.sailing.gwt.home.mobile.partials.impressions.Impressions;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.simpleinfoblock.SimpleInfoBlock;
import com.sap.sailing.gwt.home.mobile.partials.statisticsBox.StatisticsBox;
import com.sap.sailing.gwt.home.mobile.partials.statisticsBox.StatisticsDTO;
import com.sap.sailing.gwt.home.mobile.places.event.overview.EventOverviewStage;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewStageAction;
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
    @UiField Impressions impressionsUi;
    @UiField StatisticsBox statisticsBoxUi;
    
    private Presenter currentPresenter;

    public EventViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        eventHeaderUi = new EventHeader(presenter.getCtx().getEventDTO());
        overviewStageUi = new EventOverviewStage(currentPresenter);
        initWidget(uiBinder.createAndBindUi(this));
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        refreshManager.add(overviewStageUi, new GetEventOverviewStageAction(currentPresenter.getCtx().getEventDTO()
                .getId()));
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
    public void setStatistics(StatisticsDTO statistics) {
        statisticsBoxUi.addItem(StatisticsBox.ICON_REGATTAS_FOUGHT, MSG.regattas(), statistics.getRegattasFoughtCount());
        statisticsBoxUi.addItem(StatisticsBox.ICON_COMPATITORS_COUNT, MSG.competitors(), statistics.getCompetitorsCount());
        statisticsBoxUi.addItem(StatisticsBox.ICON_RACES_COUNT, MSG.races(), statistics.getRacesRunCount());
        statisticsBoxUi.addItem(StatisticsBox.ICON_TRACKED_COUNT, MSG.trackedRaces(), statistics.getTrackedRacesCount());
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
    }
}
