package com.sap.sailing.gwt.home.shared.partials.multiselection;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractAsyncSuggestBoxFilter;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractFilterWidget;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractSuggestBoxFilter;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionDataProvider.SuggestionItemsCallback;
import com.sap.sailing.gwt.ui.client.StringMessages;

public final class SuggestedMultiSelection<T> extends Composite implements SuggestedMultiSelectionDataProvider.Display<T> {

    private static SuggestedMultiSelectionUiBinder uiBinder = GWT.create(SuggestedMultiSelectionUiBinder.class);

    interface SuggestedMultiSelectionUiBinder extends UiBinder<Widget, SuggestedMultiSelection<?>> {
    }
    
    @UiField SpanElement headerTitleUi;
    @UiField FlowPanel notificationToggleContainerUi;
    @UiField(provided = true) AbstractFilterWidget<T, T> suggestionWidgetUi;
    @UiField Button removeAllButtonUi;
    @UiField FlowPanel itemContainerUi;
    private final SuggestedMultiSelectionDataProvider<T, ?> dataProvider;
    private final WidgetProvider<T> widgetProvider;

    private SuggestedMultiSelection(SuggestedMultiSelectionDataProvider<T, ?> dataProvider,
            WidgetProvider<T> widgetProvider, String title) {
        SuggestedMultiSelectionResources.INSTANCE.css().ensureInjected();
        this.dataProvider = dataProvider;
        this.widgetProvider = widgetProvider;
        this.suggestionWidgetUi = widgetProvider.getSuggestBoxFilter(new SelectionCallback<T>() {
            @Override
            public void onSuggestionSelected(T selectedItem) {
                SuggestedMultiSelection.this.dataProvider.addSelection(selectedItem);
                SuggestedMultiSelection.this.addSelectedItem(selectedItem);
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
        headerTitleUi.setInnerText(title);
        this.updateUiState();
    }
    
    public SuggestedMultiSelectionNotificationToggle addNotificationToggle(final NotificationCallback callback,
            String label) {
        final SuggestedMultiSelectionNotificationToggle notification =
                new SuggestedMultiSelectionNotificationToggle(label);
        notification.toggleButtonUi.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final boolean enabled = notification.isEnabled();
                callback.onNotificationToggled(enabled);
            }
        });
        notificationToggleContainerUi.add(notification);
        return notification;
    }
    
    @UiHandler("removeAllButtonUi")
    void onRemoveAllButtonClicked(ClickEvent event) {
        dataProvider.clearSelection();
        itemContainerUi.clear();
        this.updateUiState();
    }
    
    @Override
    public void setSelectedItems(Collection<T> selectedItemsToSet) {
        itemContainerUi.clear();
        for (final T item : selectedItemsToSet) {
            this.addSelectedItem(item);
        }
    }
    
    private void addSelectedItem(final T selectedItem) {
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
    
    private static abstract class AbstractSuggestedMultiSelectionFilter<T> extends AbstractAsyncSuggestBoxFilter<T, T> {
        private final SuggestedMultiSelectionDataProvider<T, ?> dataProvider;
        private final SelectionCallback<T> selectionCallback;

        protected AbstractSuggestedMultiSelectionFilter(SuggestedMultiSelectionDataProvider<T, ?> dataProvider,
                SelectionCallback<T> selectionCallback, String placeholderText) {
            super(placeholderText);
            this.dataProvider = dataProvider;
            this.selectionCallback = selectionCallback;
        }
        
        @Override
        protected void getSuggestions(final Request request, final Callback callback,
                final Iterable<String> queryTokens) {
            dataProvider.getSuggestionItems(queryTokens, request.getLimit(), new SuggestionItemsCallback<T>() {
                @Override
                public void setSuggestionItems(Collection<T> suggestionItems) {
                    setSuggestions(request, callback, suggestionItems, queryTokens);
                }
            });
        }
        
        @Override
        protected final void onSuggestionSelected(T selectedItem) {
            AbstractSuggestedMultiSelectionFilter.this.clear();
            selectionCallback.onSuggestionSelected(selectedItem);
        }
    }
    
    public interface NotificationCallback {
        void onNotificationToggled(boolean enabled);
    }
    
    private interface SelectionCallback<T> {
        void onSuggestionSelected(T selectedItem);
    }
    
    private interface WidgetProvider<T> {
        IsWidget getItemDescriptionWidget(T item);
        AbstractSuggestBoxFilter<T, T> getSuggestBoxFilter(SelectionCallback<T> selectionCallback);
    }
    
    public static SuggestedMultiSelection<SimpleCompetitorWithIdDTO> forCompetitors(
            final SuggestedMultiSelectionDataProvider<SimpleCompetitorWithIdDTO, ?> dataProvider, String headerTitle) {
        return new SuggestedMultiSelection<>(dataProvider, new WidgetProvider<SimpleCompetitorWithIdDTO>() {
            @Override
            public IsWidget getItemDescriptionWidget(SimpleCompetitorWithIdDTO item) {
                return new SuggestedMultiSelectionCompetitorItemDescription(item);
            }

            @Override
            public AbstractSuggestBoxFilter<SimpleCompetitorWithIdDTO, SimpleCompetitorWithIdDTO> getSuggestBoxFilter(
                    SelectionCallback<SimpleCompetitorWithIdDTO> selectionCallback) {
                return new AbstractSuggestedMultiSelectionFilter<SimpleCompetitorWithIdDTO>(dataProvider,
                        selectionCallback, StringMessages.INSTANCE.add(StringMessages.INSTANCE.competitor())) {
                    @Override
                    protected String createSuggestionKeyString(SimpleCompetitorWithIdDTO value) {
                        return value.getSailID();
                    }

                    @Override
                    protected String createSuggestionAdditionalDisplayString(SimpleCompetitorWithIdDTO value) {
                        return value.getName();
                    }
                };
            }
        }, headerTitle);
    }
    
    public static SuggestedMultiSelection<BoatClassDTO> forBoatClasses(
            final SuggestedMultiSelectionDataProvider<BoatClassDTO, ?> dataProvider, String headerTitle) {
        return new SuggestedMultiSelection<>(dataProvider, new WidgetProvider<BoatClassDTO>() {
            @Override
            public IsWidget getItemDescriptionWidget(BoatClassDTO item) {
                return new SuggestedMultiSelectionBoatClassItemDescription(item);
            }

            @Override
            public AbstractSuggestBoxFilter<BoatClassDTO, BoatClassDTO> getSuggestBoxFilter(
                    SelectionCallback<BoatClassDTO> selectionCallback) {
                return new AbstractSuggestedMultiSelectionFilter<BoatClassDTO>(dataProvider, selectionCallback,
                        StringMessages.INSTANCE.add(StringMessages.INSTANCE.boatClass())) {
                    @Override
                    protected String createSuggestionKeyString(BoatClassDTO value) {
                        return value.getName();
                    }

                    @Override
                    protected String createSuggestionAdditionalDisplayString(BoatClassDTO value) {
                        return null;
                    }
                };
            }
        }, headerTitle);
    }

}
