package com.sap.sailing.gwt.home.client.place.event.overview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.partials.countdown.Countdown;
import com.sap.sailing.gwt.home.client.place.event.partials.message.Message;
import com.sap.sailing.gwt.home.client.place.event.partials.video.Video;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewStageContentDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewStageDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewTickerStageDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewVideoStageDTO;

public class EventOverviewStage extends Composite implements RefreshableWidget<EventOverviewStageDTO> {
    
    private static StageUiBinder uiBinder = GWT.create(StageUiBinder.class);

    interface StageUiBinder extends UiBinder<Widget, EventOverviewStage> {
    }
    
    @UiField SimplePanel stage;
    @UiField Message message;
    
    private Widget lastContent;

    private final EventView.Presenter presenter;
    
    public EventOverviewStage(EventView.Presenter presenter) {
        this.presenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setData(EventOverviewStageDTO stageData, long nextUpdate, int updateNo) {
        message.setMessage(stageData.getEventMessage());
        
        EventOverviewStageContentDTO data = stageData.getStageContent();
        if(data instanceof EventOverviewVideoStageDTO) {
            if(!(lastContent instanceof Video) || ((Video) lastContent).shouldBeReplaced(((EventOverviewVideoStageDTO) data).getSource())) {
                lastContent = new Video();
                ((Video)lastContent).setData((EventOverviewVideoStageDTO) data);
            } 
        } else if (data instanceof EventOverviewTickerStageDTO) {
            if (!(lastContent instanceof Countdown)) {
                lastContent = new Countdown(presenter);
            }
            ((Countdown) lastContent).setData((EventOverviewTickerStageDTO) data);
        } else {
            lastContent = null;
        }
        stage.setWidget(lastContent);
    }

}
