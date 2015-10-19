package com.sap.sailing.gwt.home.mobile.partials.searchresult;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.places.searchresult.SearchResultView.Presenter;
import com.sap.sailing.gwt.ui.shared.dispatch.search.SearchResultDTO;

public class SearchResult extends Composite {

    private static SearchResultUiBinder uiBinder = GWT.create(SearchResultUiBinder.class);

    interface SearchResultUiBinder extends UiBinder<Widget, SearchResult> {
    }

    @UiField TextBox searchInputUi;
    @UiField Button searchButtonUi;
    @UiField SpanElement searchResultCountUi;
    @UiField SpanElement searchResultForUi;
    @UiField FlowPanel searchResultContainerUi;
    
    private final Presenter presenter;
    
    public SearchResult(Presenter presenter) {
        SearchResultResources.INSTANCE.css().ensureInjected();
        this.presenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void updateSearchResult(String searchText, Collection<SearchResultDTO> searchResultItems) {
        searchResultContainerUi.clear();
        searchInputUi.setValue(searchText);
        searchResultCountUi.setInnerText(String.valueOf(searchResultItems.size()));
        searchResultForUi.setInnerText(searchText);
        for (SearchResultDTO searchResult : searchResultItems) {
            searchResultContainerUi.add(new SearchResultItem(presenter.getNavigator(), searchResult));
        }
    }
    
    @UiHandler("searchInputUi")
    void onSearchInputEnter(KeyPressEvent event) {
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
            presenter.doSearch(searchInputUi.getValue());
        }
    }
    
    @UiHandler("searchButtonUi")
    void onSearchButtonClicked(ClickEvent event) {
        presenter.doSearch(searchInputUi.getValue());
    }

}
