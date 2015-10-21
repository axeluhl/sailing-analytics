package com.sap.sailing.gwt.home.shared.partials.searchresult;

import static com.sap.sailing.gwt.home.shared.utils.EventDatesFormatterUtil.formatDateRangeWithYear;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.dispatch.search.SearchResultDTO;

public abstract class AbstractSearchResultItem extends Widget {

    protected final void init(Element element, SearchResultDTO item) {
        setElement(element);
        getResultTitleUi().setInnerText(item.getDisplayName());
        getEventNameUi().setInnerText(item.getEventName());
        getEventVenueUi().setInnerText(item.getEventVenueName());
        if (item.getEventStartDate() != null && item.getEventEndDate() != null) {
            getEventDateUi().setInnerText(formatDateRangeWithYear(item.getEventStartDate(), item.getEventEndDate()));
        }
    }
    
    protected abstract Element getResultTitleUi();
    
    protected abstract Element getEventNameUi();
    
    protected abstract Element getEventVenueUi();
    
    protected abstract Element getEventDateUi();
}
