package com.sap.sailing.gwt.home.client.place.start;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.stage.StageEventType;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sse.common.Util.Pair;

public interface StartView {
    Widget asWidget();
    
    void setFeaturedEvents(List<Pair<StageEventType, EventBaseDTO>> featuredEvents);

    void setRecentEvents(List<EventBaseDTO> recentEvents);
}
