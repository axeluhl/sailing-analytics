package com.sap.sailing.gwt.home.desktop.partials.eventstage;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.client.SharedResources.MediaCss;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.desktop.partials.updates.UpdatesBox;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.partials.countdown.Countdown;
import com.sap.sailing.gwt.home.shared.partials.countdown.Countdown.CountdownNavigationProvider;
import com.sap.sailing.gwt.home.shared.partials.message.Message;
import com.sap.sailing.gwt.home.shared.partials.video.Video;
import com.sap.sailing.gwt.home.shared.refresh.RefreshManager;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewStageContentDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewStageDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewTickerStageDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewVideoStageDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewNewsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewStageAction;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;

public class EventOverviewStage extends Composite {
    
    private final RefreshableWidget<EventOverviewStageDTO> refreshable = new RefreshableWidget<EventOverviewStageDTO>() {
        @Override
        public void setData(EventOverviewStageDTO data) {
            setStageData(data);
        }
    };
    private final RefreshableWidget<ListResult<NewsEntryDTO>> newsRefreshable = new RefreshableWidget<ListResult<NewsEntryDTO>>() {
        @Override
        public void setData(ListResult<NewsEntryDTO> data) {
            setNews(data.getValues());
        }
    };
    
    private static StageUiBinder uiBinder = GWT.create(StageUiBinder.class);

    interface StageUiBinder extends UiBinder<Widget, EventOverviewStage> {
    }
    
    private final MediaCss mediaCss = SharedResources.INSTANCE.mediaCss();
    
    @UiField SimplePanel stage;
    @UiField Message message;
    @UiField DivElement updatesWrapperUi;
    @UiField(provided = true) UpdatesBox updatesUi;
    
    private Widget lastContent;

    private final EventView.Presenter presenter;
    private final StageCountdownNavigationProvider countdownNavigationProvider = new StageCountdownNavigationProvider();
    private RefreshManager refreshManager;
    
    public EventOverviewStage(EventView.Presenter presenter) {
        this.presenter = presenter;
        
        updatesUi = new UpdatesBox(presenter);
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setupRefresh(RefreshManager refreshManager) {
        this.refreshManager = refreshManager;
        refreshManager.add(refreshable, new GetEventOverviewStageAction(presenter.getCtx().getEventDTO().getId(), false));
        
        if(presenter.getCtx().getEventDTO().getState() == EventState.RUNNING) {
            refreshManager.add(newsRefreshable, new GetEventOverviewNewsAction(presenter.getCtx().getEventDTO().getId(), 15));
        } else {
            updatesUi.removeFromParent();
            updatesWrapperUi.removeFromParent();
        }
    }

    private void setStageData(EventOverviewStageDTO stageData) {
        message.setMessage(stageData.getEventMessage());
        
        EventOverviewStageContentDTO data = stageData.getStageContent();
        if(data instanceof EventOverviewVideoStageDTO) {
            if(!(lastContent instanceof Video) || ((Video) lastContent).shouldBeReplaced(((EventOverviewVideoStageDTO) data).getVideo().getSourceRef())) {
                lastContent = new Video();
                ((Video)lastContent).setData((EventOverviewVideoStageDTO) data);
            } 
        } else if (data instanceof EventOverviewTickerStageDTO) {
            if (!(lastContent instanceof Countdown)) {
                lastContent = new Countdown(countdownNavigationProvider);
            }
            ((Countdown) lastContent).setData((EventOverviewTickerStageDTO) data);
        } else {
            lastContent = null;
        }
        stage.setWidget(lastContent);
    }
    
    private void setNews(List<NewsEntryDTO> news) {
        if(lastContent == null) {
            setStageData(new EventOverviewStageDTO(null, new EventOverviewTickerStageDTO(null, null, null)));
        }
        
        if(news == null || news.isEmpty()) {
            updatesWrapperUi.getStyle().setDisplay(Display.NONE);
            stage.removeStyleName(mediaCss.medium7());
            stage.removeStyleName(mediaCss.large8());
        } else {
            updatesWrapperUi.getStyle().clearDisplay();
            updatesUi.setData(news, refreshManager.getDispatchSystem().getCurrentServerTime());
            stage.addStyleName(mediaCss.medium7());
            stage.addStyleName(mediaCss.large8());
        }
    }
    
    private class StageCountdownNavigationProvider implements CountdownNavigationProvider {
        @Override
        public PlaceNavigation<?> getRegattaNavigation(String regattaName) {
            return presenter.getRegattaNavigation(regattaName);
        }
    }
}
