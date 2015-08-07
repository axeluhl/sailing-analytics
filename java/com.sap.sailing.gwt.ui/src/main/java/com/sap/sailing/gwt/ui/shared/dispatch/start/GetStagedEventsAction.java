package com.sap.sailing.gwt.ui.shared.dispatch.start;

import java.net.MalformedURLException;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.server.EventHolder;
import com.sap.sailing.gwt.server.EventStageCandidateCalculator;
import com.sap.sailing.gwt.server.HomeServiceImpl;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sailing.gwt.ui.shared.start.EventStageDTO;
import com.sap.sailing.gwt.ui.shared.start.StageEventType;
import com.sap.sse.common.Util.Pair;

public class GetStagedEventsAction implements Action<ListResult<EventStageDTO>> {
    
    private boolean useTeaserImage;

    protected GetStagedEventsAction() {
    }
    
    public GetStagedEventsAction(boolean useTeaserImage) {
        this.useTeaserImage = useTeaserImage;
    }
    
    @GwtIncompatible
    public ListResult<EventStageDTO> execute(final DispatchContext context) throws MalformedURLException {
        EventStageCandidateCalculator stageCandidateCalculator = new EventStageCandidateCalculator();
        
        HomeServiceUtil.forAllPublicEvents(context.getRacingEventService(), context.getRequest(), stageCandidateCalculator);
        
        ListResult<EventStageDTO> result = new ListResult<>();
        
        int count = 0;
        for(Pair<StageEventType, EventHolder> pair : stageCandidateCalculator.getFeaturedEvents()) {
            count++;
            if(count > HomeServiceImpl.MAX_STAGE_EVENTS) {
                break;
            }
            
            StageEventType stageType = pair.getA();
            EventHolder holder = pair.getB();
            result.addValue(HomeServiceUtil.convertToEventStageDTO(holder.event, holder.baseURL, holder.onRemoteServer, stageType, context.getRacingEventService(), useTeaserImage));
        }
        return result;
    }
}
