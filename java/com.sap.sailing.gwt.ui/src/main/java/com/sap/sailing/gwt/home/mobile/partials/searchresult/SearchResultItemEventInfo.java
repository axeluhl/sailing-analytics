package com.sap.sailing.gwt.home.mobile.partials.searchresult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.sap.sailing.gwt.home.communication.search.SearchResultEventInfoDTO;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.partials.searchresult.AbstractSearchResultItemEventInfo;

public class SearchResultItemEventInfo extends AbstractSearchResultItemEventInfo {

    private static SearchResultItemEventInfoUiBinder uiBinder = GWT.create(SearchResultItemEventInfoUiBinder.class);

    interface SearchResultItemEventInfoUiBinder extends UiBinder<AnchorElement, SearchResultItemEventInfo> {
    }
    
    @UiField DivElement eventNameUi;
    @UiField SpanElement eventVenueUi;
    @UiField SpanElement eventDateUi;
    private final AnchorElement anchorUi;

    SearchResultItemEventInfo(SearchResultEventInfoDTO event, PlaceNavigation<?> navigation) {
        init(anchorUi = uiBinder.createAndBindUi(this), event, navigation);
    }

    @Override
    protected Element getEventNameUi() {
        return eventNameUi;
    }

    @Override
    protected Element getEventVenueUi() {
        return eventVenueUi;
    }

    @Override
    protected Element getEventDateUi() {
        return eventDateUi;
    }
    
    @Override
    protected AnchorElement getNavigationUi() {
        return anchorUi;
    }

}
