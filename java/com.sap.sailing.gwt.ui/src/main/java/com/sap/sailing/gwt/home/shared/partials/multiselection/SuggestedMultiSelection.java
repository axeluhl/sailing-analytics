package com.sap.sailing.gwt.home.shared.partials.multiselection;

import java.util.Collection;
import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractAsyncSuggestBoxFilter;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractFilterWidget;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractSuggestBoxFilter;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionPresenter.SuggestionItemsCallback;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.suggestion.AbstractSuggestOracle;

/**
 * UI component for multi-selection of entries via suggestion drop-down menu. Selected entries can be removed one by one
 * or in total. In addition, {@link #addNotificationToggle(Consumer, String) checkboxes} can be added optionally.
 * 
 * @param <T>
 *            actual class of selectable entries
 */
public final class SuggestedMultiSelection<T> extends Composite
        implements SuggestedMultiSelectionPresenter.Display<T> {

    private static SuggestedMultiSelectionUiBinder uiBinder = GWT.create(SuggestedMultiSelectionUiBinder.class);

    interface SuggestedMultiSelectionUiBinder extends UiBinder<Widget, SuggestedMultiSelection<?>> {
    }

    @UiField
    SpanElement headerTitleUi;
    @UiField
    FlowPanel notificationToggleContainerUi;
    @UiField
    DivElement contentSeparatorUi;
    @UiField(provided = true)
    AbstractFilterWidget<T, T> suggestionWidgetUi;
    @UiField
    Button removeAllButtonUi;
    @UiField
    FlowPanel itemContainerUi;
    private final SuggestedMultiSelectionPresenter<T, ?> presenter;
    private final WidgetProvider<T> widgetProvider;

    private SuggestedMultiSelection(SuggestedMultiSelectionPresenter<T, ?> presenter,
            WidgetProvider<T> widgetProvider, String title) {
        SuggestedMultiSelectionResources.INSTANCE.css().ensureInjected();
        this.presenter = presenter;
        this.widgetProvider = widgetProvider;
        this.suggestionWidgetUi = widgetProvider.getSuggestBoxFilter(selectedItem -> {
            presenter.addSelection(selectedItem);
            SuggestedMultiSelection.this.addSelectedItem(selectedItem);
        });
        initWidget(uiBinder.createAndBindUi(this));
        headerTitleUi.setInnerText(title);
        UIObject.setVisible(contentSeparatorUi, false);
        this.updateUiState();
    }

    /**
     * Adds a checkbox with the given label to toggle notifications by notifying the provided callback.
     * 
     * @param callback
     *            {@link Consumer Callback} which get notified on checkbox toggles
     * @param label
     *            {@link String Label} for the added checkbox
     * @return reference on the added checkbox
     */
    public HasValue<Boolean> addNotificationToggle(Consumer<Boolean> callback, String label) {
        SuggestedMultiSelectionNotificationToggle notification = new SuggestedMultiSelectionNotificationToggle(label);
        notification.addValueChangeHandler(event -> callback.accept(event.getValue()));
        notificationToggleContainerUi.add(notification);
        UIObject.setVisible(contentSeparatorUi, true);
        return notification;
    }

    @UiHandler("removeAllButtonUi")
    void onRemoveAllButtonClicked(ClickEvent event) {
        presenter.clearSelection();
        itemContainerUi.clear();
        this.updateUiState();
    }

    @Override
    public void setSelectedItems(Iterable<T> selectedItemsToSet) {
        itemContainerUi.clear();
        selectedItemsToSet.forEach(this::addSelectedItem);
    }

    private void addSelectedItem(final T selectedItem) {
        itemContainerUi.add(new SuggestedMultiSelectionItem() {

            @Override
            protected IsWidget getItemDescriptionWidget() {
                return widgetProvider.getItemDescriptionWidget(selectedItem);
            }

            @Override
            protected void onRemoveItemRequsted() {
                presenter.removeSelection(selectedItem);
                this.removeFromParent();
                updateUiState();
            }
        });
        this.updateUiState();
    }

    private void updateUiState() {
        removeAllButtonUi.setEnabled(itemContainerUi.getWidgetCount() > 0);
    }

    private static class SuggestedMultiSelectionFilter<T> extends AbstractAsyncSuggestBoxFilter<T, T> {
        private final Consumer<T> selectionCallback;

        private SuggestedMultiSelectionFilter(final SuggestedMultiSelectionPresenter<T, ?> presenter,
                Consumer<T> selectionCallback, String placeholderText) {
            super(new AbstractSuggestOracle<T>() {
                @Override
                protected void getSuggestions(final Request request, final Callback callback,
                        final Iterable<String> queryTokens) {
                    presenter.getSuggestionItems(queryTokens, request.getLimit(), new SuggestionItemsCallback<T>() {
                        @Override
                        public void setSuggestionItems(Collection<T> suggestionItems) {
                            setSuggestions(request, callback, suggestionItems, queryTokens);
                        }
                    });
                }

                @Override
                protected String createSuggestionKeyString(T value) {
                    return presenter.createSuggestionKeyString(value);
                }

                @Override
                protected String createSuggestionAdditionalDisplayString(T value) {
                    return presenter.createSuggestionAdditionalDisplayString(value);
                }
            }, placeholderText);
            this.selectionCallback = selectionCallback;
        }

        @Override
        protected final void onSuggestionSelected(T selectedItem) {
            SuggestedMultiSelectionFilter.this.clear();
            selectionCallback.accept(selectedItem);
        }
    }

    private interface WidgetProvider<T> {
        IsWidget getItemDescriptionWidget(T item);
        AbstractSuggestBoxFilter<T, T> getSuggestBoxFilter(Consumer<T> selectionCallback);
    }

    public static SuggestedMultiSelection<SimpleCompetitorWithIdDTO> forCompetitors(
            final SuggestedMultiSelectionPresenter<SimpleCompetitorWithIdDTO, ?> presenter, String headerTitle,
            FlagImageResolver flagImageResolver) {
        return new SuggestedMultiSelection<>(presenter, new WidgetProvider<SimpleCompetitorWithIdDTO>() {
            @Override
            public IsWidget getItemDescriptionWidget(SimpleCompetitorWithIdDTO item) {
                return new SuggestedMultiSelectionCompetitorItemDescription(item, flagImageResolver);
            }

            @Override
            public AbstractSuggestBoxFilter<SimpleCompetitorWithIdDTO, SimpleCompetitorWithIdDTO> getSuggestBoxFilter(
                    Consumer<SimpleCompetitorWithIdDTO> selectionCallback) {
                return new SuggestedMultiSelectionFilter<SimpleCompetitorWithIdDTO>(presenter, selectionCallback,
                        StringMessages.INSTANCE.add(StringMessages.INSTANCE.competitor()));
            }
        }, headerTitle);
    }

    public static SuggestedMultiSelection<BoatClassDTO> forBoatClasses(
            final SuggestedMultiSelectionPresenter<BoatClassDTO, ?> presenter, String headerTitle) {
        return new SuggestedMultiSelection<>(presenter, new WidgetProvider<BoatClassDTO>() {
            @Override
            public IsWidget getItemDescriptionWidget(BoatClassDTO item) {
                return new SuggestedMultiSelectionBoatClassItemDescription(item);
            }

            @Override
            public AbstractSuggestBoxFilter<BoatClassDTO, BoatClassDTO> getSuggestBoxFilter(
                    Consumer<BoatClassDTO> selectionCallback) {
                return new SuggestedMultiSelectionFilter<BoatClassDTO>(presenter, selectionCallback,
                        StringMessages.INSTANCE.add(StringMessages.INSTANCE.boatClass()));
            }
        }, headerTitle);
    }

}
