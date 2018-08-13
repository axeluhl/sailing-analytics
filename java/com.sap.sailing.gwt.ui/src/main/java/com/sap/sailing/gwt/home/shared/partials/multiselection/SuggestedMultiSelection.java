package com.sap.sailing.gwt.home.shared.partials.multiselection;

import java.util.ArrayList;
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
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractAsyncSuggestBoxFilter;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractFilterWidget;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractSuggestBoxFilter;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionDataProvider.SuggestionItemsCallback;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.suggestion.AbstractSuggestOracle;

public final class SuggestedMultiSelection<T> extends Composite
        implements SuggestedMultiSelectionDataProvider.Display<T> {

    private static SuggestedMultiSelectionUiBinder uiBinder = GWT.create(SuggestedMultiSelectionUiBinder.class);

    interface SuggestedMultiSelectionUiBinder extends UiBinder<Widget, SuggestedMultiSelection<?>> {
    }

    @UiField
    SpanElement headerTitleUi;
    @UiField
    FlowPanel notificationToggleContainerUi;
    @UiField(provided = true)
    AbstractFilterWidget<T, T> suggestionWidgetUi;
    @UiField
    Button removeAllButtonUi;
    @UiField
    FlowPanel itemContainerUi;
    private final WidgetProvider<T> widgetProvider;
    private final Collection<SelectionChangeHandler<T>> selectionChangeHandlers = new ArrayList<>();

    private SuggestedMultiSelection(SuggestedMultiSelectionDataProvider<T, ?> dataProvider,
            WidgetProvider<T> widgetProvider, String title) {
        SuggestedMultiSelectionResources.INSTANCE.css().ensureInjected();
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
        selectionChangeHandlers.add(new SelectionChangeHandler<T>() {

            @Override
            public void onAdd(T selectedItem) {
                dataProvider.addSelection(selectedItem);
            }

            @Override
            public void onRemove(T selectedItem) {
                dataProvider.removeSelection(selectedItem);
            }

            @Override
            public void onClear() {
                dataProvider.clearSelection();
            }
        });
    }

    public SuggestedMultiSelectionNotificationToggle addNotificationToggle(final NotificationCallback callback,
            String label) {
        final SuggestedMultiSelectionNotificationToggle notification = new SuggestedMultiSelectionNotificationToggle(
                label);
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

    public void addSelectionChangeHandler(SelectionChangeHandler<T> handler) {
        this.selectionChangeHandlers.add(handler);
    }

    @UiHandler("removeAllButtonUi")
    void onRemoveAllButtonClicked(ClickEvent event) {
        itemContainerUi.clear();
        selectionChangeHandlers.forEach(h -> h.onClear());
        this.updateUiState();
    }

    @Override
    public void setSelectedItems(Iterable<T> selectedItemsToSet) {
        itemContainerUi.clear();
        for (final T item : selectedItemsToSet) {
            this.addSelectedItem(item, true);
        }
    }

    private void addSelectedItem(final T selectedItem) {
        addSelectedItem(selectedItem, false);
    }

    private void addSelectedItem(final T selectedItem, boolean suppressEvents) {

        itemContainerUi.add(new SuggestedMultiSelectionItem() {

            @Override
            protected IsWidget getItemDescriptionWidget() {
                return widgetProvider.getItemDescriptionWidget(selectedItem);
            }

            @Override
            protected void onRemoveItemRequsted() {
                this.removeFromParent();
                updateUiState();
                selectionChangeHandlers.forEach(h -> h.onRemove(selectedItem));
            }
        });
        if (!suppressEvents) {
            selectionChangeHandlers.forEach(h -> h.onAdd(selectedItem));
        }
        this.updateUiState();
    }

    private void updateUiState() {
        removeAllButtonUi.setEnabled(itemContainerUi.getWidgetCount() > 0);
    }

    private static class SuggestedMultiSelectionFilter<T> extends AbstractAsyncSuggestBoxFilter<T, T> {
        private final SelectionCallback<T> selectionCallback;

        private SuggestedMultiSelectionFilter(final SuggestedMultiSelectionDataProvider<T, ?> dataProvider,
                SelectionCallback<T> selectionCallback, String placeholderText) {
            super(new AbstractSuggestOracle<T>() {
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
                protected String createSuggestionKeyString(T value) {
                    return dataProvider.createSuggestionKeyString(value);
                }

                @Override
                protected String createSuggestionAdditionalDisplayString(T value) {
                    return dataProvider.createSuggestionAdditionalDisplayString(value);
                }
            }, placeholderText);
            this.selectionCallback = selectionCallback;
        }

        @Override
        protected final void onSuggestionSelected(T selectedItem) {
            SuggestedMultiSelectionFilter.this.clear();
            selectionCallback.onSuggestionSelected(selectedItem);
        }
    }

    public interface SelectionChangeHandler<S> {
        void onAdd(S selectedItems);

        void onRemove(S selectedItem);

        void onClear();
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
            final SuggestedMultiSelectionDataProvider<SimpleCompetitorWithIdDTO, ?> dataProvider, String headerTitle,
            FlagImageResolver flagImageResolver) {
        return new SuggestedMultiSelection<>(dataProvider, new WidgetProvider<SimpleCompetitorWithIdDTO>() {
            @Override
            public IsWidget getItemDescriptionWidget(SimpleCompetitorWithIdDTO item) {
                return new SuggestedMultiSelectionCompetitorItemDescription(item, flagImageResolver);
            }

            @Override
            public AbstractSuggestBoxFilter<SimpleCompetitorWithIdDTO, SimpleCompetitorWithIdDTO> getSuggestBoxFilter(
                    SelectionCallback<SimpleCompetitorWithIdDTO> selectionCallback) {
                return new SuggestedMultiSelectionFilter<SimpleCompetitorWithIdDTO>(dataProvider, selectionCallback,
                        StringMessages.INSTANCE.add(StringMessages.INSTANCE.competitor()));
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
                return new SuggestedMultiSelectionFilter<BoatClassDTO>(dataProvider, selectionCallback,
                        StringMessages.INSTANCE.add(StringMessages.INSTANCE.boatClass()));
            }
        }, headerTitle);
    }

}
