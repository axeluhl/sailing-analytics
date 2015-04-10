package com.sap.sailing.gwt.home.client;

import java.util.ArrayList;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;
import com.sap.sailing.gwt.ui.shared.start.StartViewDTO;


/**
 * The async counterpart of {@link HomeService}
 */
public interface HomeServiceAsync {
    void getEventViewById(UUID id, AsyncCallback<EventViewDTO> callback);

    void getEventSeriesViewById(UUID id, AsyncCallback<EventSeriesViewDTO> asyncCallback);
    
    void getMediaForEvent(UUID eventId, AsyncCallback<MediaDTO> callback); 

    void getMediaForEventSeries(UUID seriesId, AsyncCallback<MediaDTO> callback);

    void getEventListView(AsyncCallback<EventListViewDTO> callback);

    void getLeaderboardGroupsByEventId(UUID id, AsyncCallback<ArrayList<LeaderboardGroupDTO>> callback);

    void getStartView(AsyncCallback<StartViewDTO> callback);
}
