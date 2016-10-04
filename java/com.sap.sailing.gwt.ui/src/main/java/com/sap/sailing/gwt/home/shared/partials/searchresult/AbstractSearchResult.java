package com.sap.sailing.gwt.home.shared.partials.searchresult;

import java.util.Collection;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.search.SearchResultDTO;
import com.sap.sailing.gwt.home.shared.app.AbstractPlaceNavigator;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class AbstractSearchResult extends Composite {
    
    private int resultsCount = 0;
    
    protected final void init(final AbstractPlaceNavigator navigator, Widget widget) {
        super.initWidget(widget);
        getSearchTextInputUi().getElement().setAttribute("placeholder", StringMessages.INSTANCE.searchResultHeaderPlaceholder());
        getSearchTextInputUi().addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    search(navigator);
                }                
            }
        });
        getSearchButtonUi().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                search(navigator);
            }
        });
    }
    
    private void search(AbstractPlaceNavigator navigator) {
        navigator.getSearchResultNavigation(getSearchTextInputUi().getValue()).goToPlace();
    }
    
    public void setSearchText(String searchText) {
        getSearchTextInputUi().setValue(searchText);
    }

    public void updateSearchResult(String searchText, Collection<SearchResultDTO> searchResultItems) {
        for (SearchResultDTO searchResult : searchResultItems) {
            addSearchResultItem(searchResult);
            resultsCount++;
        }
        getSearchResultAmountUi().setInnerText(StringMessages.INSTANCE.resultsFoundForSearch(resultsCount, searchText));
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        if (getSearchTextInputUi().getValue().isEmpty()) {
            getSearchTextInputUi().setFocus(true);
        }
    }
    
    protected abstract TextBox getSearchTextInputUi();
    
    protected abstract Button getSearchButtonUi();
    
    protected abstract Element getSearchResultAmountUi();
    
    protected abstract HasWidgets getSearchResultContainerUi();
    
    protected abstract void addSearchResultItem(SearchResultDTO searchResult);
}
