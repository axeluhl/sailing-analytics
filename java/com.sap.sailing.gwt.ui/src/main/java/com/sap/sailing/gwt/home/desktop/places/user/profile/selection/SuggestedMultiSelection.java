package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractFilterWidget;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractSuggestBoxFilter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.filter.Filter;

public class SuggestedMultiSelection<T> extends Composite {

    private static SuggestedMultiSelectionUiBinder uiBinder = GWT.create(SuggestedMultiSelectionUiBinder.class);

    interface SuggestedMultiSelectionUiBinder extends UiBinder<Widget, SuggestedMultiSelection<?>> {
    }
    
    @UiField SpanElement headerTitleUi;
    @UiField(provided = true) AbstractFilterWidget<T, T> suggestionWidgetUi;
    @UiField Button removeAllButtonUi;
    @UiField FlowPanel itemContainerUi;
    private final Map<Object, T> selectedItems = new HashMap<>();
    private final ProvidesKey<T> keyProvider;

    public SuggestedMultiSelection(ProvidesKey<T> keyProvider, String title) {
        SuggestedMultiSelectionResources.INSTANCE.css().ensureInjected();
        this.keyProvider = keyProvider;
        suggestionWidgetUi = new AbstractSuggestBoxFilter<T, T>(StringMessages.INSTANCE.add()) {
            @Override
            protected String createSuggestionKeyString(T value) {
                return null;
            }

            @Override
            protected String createSuggestionAdditionalDisplayString(T value) {
                return null;
            }

            @Override
            protected Iterable<String> getMatchingStrings(T value) {
                return null;
            }

            @Override
            protected Filter<T> getFilter(String searchString) {
                return null;
            }
        };
        initWidget(uiBinder.createAndBindUi(this));
        headerTitleUi.setInnerText(title);
    }
    
    public void setSelectedItems(List<T> selectedItemsToSet) {
        itemContainerUi.clear();
        selectedItems.clear();
        for (T item : selectedItemsToSet) {
            Object key = keyProvider.getKey(item);
            selectedItems.put(key, item);
            itemContainerUi.add(new SuggestedMultiSelectionItem<T>(item));
        }
    }

}
