package com.sap.sailing.gwt.home.shared.partials.editable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionResources;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionView;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EditableSuggestedMultiSelection<T> extends Composite implements HasText {

    private static SuggestedMultiSelectionUiBinder uiBinder = GWT.create(SuggestedMultiSelectionUiBinder.class);

    interface SuggestedMultiSelectionUiBinder extends UiBinder<Widget, EditableSuggestedMultiSelection<?>> {
    }

    public static interface EditModeChangeHandler {
        void onEditModeChanged(boolean edit);
    }

    @UiField
    StringMessages i18n;

    @UiField
    DivElement parentPanel;

    @UiField(provided = true)
    SuggestedMultiSelectionView<T> multiSelect;

    @UiField
    DivElement headerElementUi;

    @UiField
    SpanElement headerTitleUi;

    @UiField
    FlowPanel itemContainerUi;

    @UiField
    DivElement listContainerUi;

    @UiField
    InlineEditButton editButtonUi;

    private final Map<T, IsWidget> tableElements = new HashMap<>();
    private final Function<T, IsWidget> itemProducer;

    private boolean editMode = false;
    private EditModeChangeHandler editModeChangeHandler;

    public EditableSuggestedMultiSelection(Function<T, IsWidget> itemProducer, SuggestedMultiSelectionView<T> multis) {
        this(itemProducer, multis, null, false);
    }

    public EditableSuggestedMultiSelection(Function<T, IsWidget> itemProducer, SuggestedMultiSelectionView<T> multis,
            EditModeChangeHandler editModeChangeHandler, boolean headless) {
        this.itemProducer = itemProducer;
        multiSelect = multis;
        this.editModeChangeHandler = editModeChangeHandler;
        initWidget(uiBinder.createAndBindUi(this));

        multiSelect.getElement().removeFromParent();
        if (headless) {
            listContainerUi.removeClassName(SuggestedMultiSelectionResources.INSTANCE.css().suggestions());
            headerElementUi.removeFromParent();
            editButtonUi.removeFromParent();
        }
    }

    public void setEditMode(boolean state) {
        this.editMode = state;
        listContainerUi.removeFromParent();
        multiSelect.getElement().removeFromParent();
        editModeChangeHandler.onEditModeChanged(state);
        if (state) {
            parentPanel.appendChild(multiSelect.getElement());
        } else {
            parentPanel.appendChild(listContainerUi);
        }
    }

    @UiHandler("editButtonUi")
    void onEditButtonClicked(ClickEvent event) {
        setEditMode(!editMode);
    }

    public InlineEditButton getEditButton() {
        return editButtonUi;
    }

    public void addListItem(T listItem) {
        Widget item = itemProducer.apply(listItem).asWidget();
        itemContainerUi.add(item);

        // TODO: move to CSS file
        if (itemContainerUi.getElement().getChild(0) != item.getElement()) {
            item.getElement().getStyle().setProperty("borderTop", "1px solid #ddd");
        }
        item.getElement().getStyle().setPaddingTop(0.333333333333333333, Unit.EM);
        item.getElement().getStyle().setPaddingBottom(0.333333333333333333, Unit.EM);
        item.getElement().getStyle().setPaddingLeft(0.333333333333333333, Unit.EM);
        DOM.getChild(item.getElement(), 1).getStyle().setPosition(Position.RELATIVE);
        DOM.getChild(item.getElement(), 1).getStyle().setTop(-0.333333333333333333, Unit.EM);
        DOM.getChild(item.getElement(), 2).getStyle().setPosition(Position.RELATIVE);
        DOM.getChild(item.getElement(), 2).getStyle().setTop(-0.333333333333333333, Unit.EM);
        tableElements.put(listItem, item);
    }

    public void setSelectedItems(Iterable<T> competitors) {
        itemContainerUi.clear();
        competitors.forEach(c -> addListItem(c));
        multiSelect.setSelectedItems(competitors);
    }

    @Override
    public String getText() {
        return headerTitleUi.getInnerText();
    }

    @Override
    public void setText(String text) {
        headerTitleUi.setInnerText(text);
    }
}
