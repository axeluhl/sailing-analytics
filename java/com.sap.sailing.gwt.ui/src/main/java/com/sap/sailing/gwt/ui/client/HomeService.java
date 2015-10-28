package com.sap.sailing.gwt.ui.client;

import java.util.UUID;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.gwt.home.communication.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;

public interface HomeService extends RemoteService {
    EventSeriesViewDTO getEventSeriesViewById(UUID id);
    
    MediaDTO getMediaForEvent(UUID eventId);
    
    MediaDTO getMediaForEventSeries(UUID seriesId);
}
