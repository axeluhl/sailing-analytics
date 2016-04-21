package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import java.util.Arrays;
import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionDataProvider.SuggestionItemsCallback;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractFilterWidget;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractSuggestBoxFilter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.filter.Filter;

public final class SuggestedMultiSelection<T> extends Composite {

    private static SuggestedMultiSelectionUiBinder uiBinder = GWT.create(SuggestedMultiSelectionUiBinder.class);

    interface SuggestedMultiSelectionUiBinder extends UiBinder<Widget, SuggestedMultiSelection<?>> {
    }
    
    @UiField SpanElement headerTitleUi;
    @UiField SuggestedMultiSelectionNotificationToggle noticationToggleUi;
    @UiField(provided = true) AbstractFilterWidget<T, T> suggestionWidgetUi;
    @UiField Button removeAllButtonUi;
    @UiField FlowPanel itemContainerUi;
    private final SuggestedMultiSelectionDataProvider<T> dataProvider;
    private final WidgetProvider<T> widgetProvider;

    private SuggestedMultiSelection(SuggestedMultiSelectionDataProvider<T> dataProvider,
            WidgetProvider<T> widgetProvider, String title) {
        SuggestedMultiSelectionResources.INSTANCE.css().ensureInjected();
        this.dataProvider = dataProvider;
        this.widgetProvider = widgetProvider;
        this.suggestionWidgetUi = widgetProvider.getSuggestBoxFilter(new SelectionCallback<T>() {
            @Override
            public void onSuggestionSelected(T selectedItem) {
                SuggestedMultiSelection.this.addSelectedItem(selectedItem);
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
        headerTitleUi.setInnerText(title);
        this.updateUiState();
    }
    
    @UiHandler("removeAllButtonUi")
    void onRemoveAllButtonClicked(ClickEvent event) {
        dataProvider.clearSelection();
        itemContainerUi.clear();
        this.updateUiState();
    }
    
    public void setSelectedItems(Collection<T> selectedItemsToSet) {
        dataProvider.clearSelection();
        itemContainerUi.clear();
        for (final T item : selectedItemsToSet) {
            this.addSelectedItem(item);
        }
    }
    
    private void addSelectedItem(final T selectedItem) {
        dataProvider.addSelection(selectedItem);
        itemContainerUi.add(new SuggestedMultiSelectionItem() {
            @Override
            protected IsWidget getItemDescriptionWidget() {
                return widgetProvider.getItemDescriptionWidget(selectedItem);
            }
            
            @Override
            protected void onRemoveItemRequsted() {
                dataProvider.removeSelection(selectedItem);
                this.removeFromParent();
                updateUiState();
            }
        });
        this.updateUiState();
    }
    
    private void updateUiState() {
        removeAllButtonUi.setEnabled(itemContainerUi.getWidgetCount() > 0);
    }
    
    private static abstract class AbstractSuggestedMultiSelectionFilter<T> extends AbstractSuggestBoxFilter<T, T> {
        protected final SuggestedMultiSelectionDataProvider<T> dataProvider;
        private final SelectionCallback<T> selectionCallback;

        protected AbstractSuggestedMultiSelectionFilter(SuggestedMultiSelectionDataProvider<T> dataProvider,
                SelectionCallback<T> selectionCallback, String placeholderText) {
            super(placeholderText);
            this.dataProvider = dataProvider;
            this.selectionCallback = selectionCallback;
        }
        
        @Override
        protected final String createSuggestionKeyString(T value) {
            return String.valueOf(dataProvider.getKey(value));
        }
        
        @Override
        protected final void onSuggestionSelected(T selectedItem) {
            AbstractSuggestedMultiSelectionFilter.this.clear();
            selectionCallback.onSuggestionSelected(selectedItem);
        }
        
        @Override
        protected final Filter<T> getFilter(String searchString) {
            dataProvider.getSuggestionItems(searchString, new SuggestionItemsCallback<T>() {
                @Override
                public void setSuggestionItems(Collection<T> suggestionItems) {
                    AbstractSuggestedMultiSelectionFilter.this.setSelectableValues(suggestionItems);
                }
            });
            return new Filter<T>() {
                @Override public boolean matches(T object) { return true; }
                @Override public String getName() { return "alwaysMatchingFilter"; }
            };
        }
    }
    
    private interface SelectionCallback<T> {
        void onSuggestionSelected(T selectedItem);
    }
    
    private interface WidgetProvider<T> {
        IsWidget getItemDescriptionWidget(T item);
        AbstractSuggestBoxFilter<T, T> getSuggestBoxFilter(SelectionCallback<T> selectionCallback);
    }
    
    public static SuggestedMultiSelection<SimpleCompetitorDTO> forCompetitors(
            final SuggestedMultiSelectionDataProvider<SimpleCompetitorDTO> dataProvider, String headerTitle) {
        return new SuggestedMultiSelection<>(dataProvider, new WidgetProvider<SimpleCompetitorDTO>() {
            @Override
            public IsWidget getItemDescriptionWidget(SimpleCompetitorDTO item) {
                return new SuggestedMultiSelectionCompetitorItemDescription(item);
            }
            
            @Override
            public AbstractSuggestBoxFilter<SimpleCompetitorDTO, SimpleCompetitorDTO> getSuggestBoxFilter(
                    SelectionCallback<SimpleCompetitorDTO> selectionCallback) {
                return new AbstractSuggestedMultiSelectionFilter<SimpleCompetitorDTO>(dataProvider, selectionCallback,
                        StringMessages.INSTANCE.add(StringMessages.INSTANCE.competitor())) {
                    @Override
                    protected String createSuggestionAdditionalDisplayString(SimpleCompetitorDTO value) {
                        return value.getName();
                    }
                    
                    @Override
                    protected Iterable<String> getMatchingStrings(SimpleCompetitorDTO value) {
                        return Arrays.asList(value.getSailID(), value.getName());
                    }
                };
            }
        }, headerTitle);
    }
    
    public static SuggestedMultiSelection<BoatClassMasterdata> forBoatClasses(
            final SuggestedMultiSelectionDataProvider<BoatClassMasterdata> dataProvider, String headerTitle) {
        return new SuggestedMultiSelection<>(dataProvider, new WidgetProvider<BoatClassMasterdata>() {
            @Override
            public IsWidget getItemDescriptionWidget(BoatClassMasterdata item) {
                return new SuggestedMultiSelectionBoatClassItemDescription(item);
            }
            
            @Override
            public AbstractSuggestBoxFilter<BoatClassMasterdata, BoatClassMasterdata> getSuggestBoxFilter(
                    SelectionCallback<BoatClassMasterdata> selectionCallback) {
                return new AbstractSuggestedMultiSelectionFilter<BoatClassMasterdata>(dataProvider, selectionCallback,
                        StringMessages.INSTANCE.add(StringMessages.INSTANCE.boatClass())) {
                    @Override
                    protected String createSuggestionAdditionalDisplayString(BoatClassMasterdata value) {
                        return null;
                    }

                    @Override
                    protected Iterable<String> getMatchingStrings(BoatClassMasterdata value) {
                        return Arrays.asList(value.getAlternativeNames());
                    }
                };
            }
        }, headerTitle);
    }

}
