package com.sap.sailing.gwt.home.shared.partials.searchresult;

import static com.sap.sailing.gwt.home.shared.utils.EventDatesFormatterUtil.formatDateRangeWithYear;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.home.communication.search.SearchResultEventInfoDTO;

public abstract class AbstractSearchResultItemEventInfo extends UIObject {
    
    protected final void init(Element element, SearchResultEventInfoDTO event) {
        setElement(element);
        getEventNameUi().setInnerText(event.getName());
        getEventVenueUi().setInnerText(event.getVenueName());
        if (event.getStartDate() != null && event.getEndDate() != null) {
            getEventDateUi().setInnerText(formatDateRangeWithYear(event.getStartDate(), event.getEndDate()));
        }
    }
    
    protected abstract Element getEventNameUi();
    
    protected abstract Element getEventVenueUi();
    
    protected abstract Element getEventDateUi();

}
