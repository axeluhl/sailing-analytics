package com.sap.sailing.gwt.home.shared.partials.searchresult;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.search.SearchResultDTO;
import com.sap.sailing.gwt.home.communication.search.SearchResultEventInfoDTO;

public abstract class AbstractSearchResultItem extends Widget {

    protected final void init(Element element, SearchResultDTO item) {
        setElement(element);
        getResultTitleUi().setInnerText(item.getDisplayName());
        for (SearchResultEventInfoDTO event : item.getEvents()) {
            addEventInfo(event);
        }
    }
    
    protected abstract Element getResultTitleUi();
    
    protected abstract void addEventInfo(SearchResultEventInfoDTO event);
    
}
