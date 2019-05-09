package com.sap.sailing.gwt.home.communication.start;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.server.EventHolder;
import com.sap.sailing.gwt.server.EventStageCandidateCalculator;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.ListResult;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * <p>
 * {@link SailingAction} implementation to load a list of stage events to be shown on mobile start page.
 * </p>
 */
public class GetStagedEventsAction implements SailingAction<ListResult<EventStageDTO>>, IsClientCacheable {
    private boolean useTeaserImage;

    protected GetStagedEventsAction() {
    }

    /**
     * Creates a {@link GetStagedEventsAction} instance, where the use of stage or teaser images can be specified.
     * 
     * @param useTeaserImage
     *            <code>true</code> to use a lower resolution teaser image for the event, <code>false</code> to use a
     *            high resolution stage image. Can be useful to safe data traffic on mobile connections.
     */
    public GetStagedEventsAction(boolean useTeaserImage) {
        this.useTeaserImage = useTeaserImage;
    }

    @GwtIncompatible
    public ListResult<EventStageDTO> execute(final SailingDispatchContext context) throws DispatchException {
        EventStageCandidateCalculator stageCandidateCalculator = new EventStageCandidateCalculator();
        HomeServiceUtil.forAllPublicEventsWithReadPermission(context.getRacingEventService(), context.getRequest(),
                context.getSecurityService(),
                stageCandidateCalculator);
        ListResult<EventStageDTO> result = new ListResult<>();
        int count = 0;
        for (Pair<StageEventType, EventHolder> pair : stageCandidateCalculator.getFeaturedEvents()) {
            count++;
            if (count > EventStageCandidateCalculator.MAX_STAGE_EVENTS) {
                break;
            }
            StageEventType stageType = pair.getA();
            EventHolder holder = pair.getB();
            result.addValue(HomeServiceUtil.convertToEventStageDTO(holder.event, holder.baseURL, holder.onRemoteServer,
                    stageType, context.getRacingEventService(), useTeaserImage));
        }
        return result;
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(useTeaserImage);
    }
}
