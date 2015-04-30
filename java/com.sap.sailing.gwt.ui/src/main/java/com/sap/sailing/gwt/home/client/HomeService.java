package com.sap.sailing.gwt.home.client;

import java.util.UUID;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;
import com.sap.sailing.gwt.ui.shared.start.StartViewDTO;

public interface HomeService extends RemoteService {
    EventViewDTO getEventViewById(UUID id);
    
    EventSeriesViewDTO getEventSeriesViewById(UUID id);
    
    MediaDTO getMediaForEvent(UUID eventId);
    
    MediaDTO getMediaForEventSeries(UUID seriesId);
    
    EventListViewDTO getEventListView() throws Exception;
    
    StartViewDTO getStartView() throws Exception;
}
