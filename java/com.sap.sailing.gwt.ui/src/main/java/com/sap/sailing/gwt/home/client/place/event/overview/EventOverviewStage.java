package com.sap.sailing.gwt.home.client.place.event.overview;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.partials.countdown.Countdown;
import com.sap.sailing.gwt.home.client.place.event.partials.livestream.Livestream;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewStageDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewTickerStageDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewVideoStageDTO;

public class EventOverviewStage extends Composite implements RefreshableWidget<EventOverviewStageDTO> {
    
    private final SimplePanel content = new SimplePanel();
    
    private Widget lastContent;
    
    public EventOverviewStage() {
        initWidget(content);
    }

    @Override
    public void setData(EventOverviewStageDTO data, long nextUpdate, int updateNo) {
        if(data instanceof EventOverviewVideoStageDTO) {
            if(!(lastContent instanceof Livestream)) {
                lastContent = new Livestream();
            }
            ((Livestream)lastContent).setData((EventOverviewVideoStageDTO) data);
        }
        if (data instanceof EventOverviewTickerStageDTO) {
            if (!(lastContent instanceof Countdown)) {
                lastContent = new Countdown();
            }
            ((Countdown) lastContent).setData((EventOverviewTickerStageDTO) data);
        }
        // TODO placeholder?
        content.setWidget(lastContent);
    }

}
