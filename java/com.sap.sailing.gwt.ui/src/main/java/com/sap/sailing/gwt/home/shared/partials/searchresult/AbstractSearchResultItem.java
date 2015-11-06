package com.sap.sailing.gwt.home.shared.partials.searchresult;

import static com.sap.sailing.gwt.home.shared.utils.EventDatesFormatterUtil.formatDateRangeWithYear;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.search.SearchResultDTO;
import com.sap.sailing.gwt.home.communication.search.SearchResultDTO.EventInfoDTO;

public abstract class AbstractSearchResultItem extends Widget {

    protected final void init(Element element, SearchResultDTO item) {
        setElement(element);
        getResultTitleUi().setInnerText(item.getDisplayName());
        EventInfoDTO event = item.getEvents().get(0);
        getEventNameUi().setInnerText(event.getName());
        getEventVenueUi().setInnerText(event.getVenueName());
        if (event.getStartDate() != null && event.getEndDate() != null) {
            getEventDateUi().setInnerText(formatDateRangeWithYear(event.getStartDate(), event.getEndDate()));
        }
    }
    
    protected abstract Element getResultTitleUi();
    
    protected abstract Element getEventNameUi();
    
    protected abstract Element getEventVenueUi();
    
    protected abstract Element getEventDateUi();
}
