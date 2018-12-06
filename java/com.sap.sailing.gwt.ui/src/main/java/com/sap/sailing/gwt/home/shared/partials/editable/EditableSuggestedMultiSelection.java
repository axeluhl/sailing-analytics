package com.sap.sailing.gwt.home.shared.partials.editable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.desktop.partials.desktopaccordion.DesktopAccordionResources;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionResources;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;

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
    DivElement parentPanelUi;

    @UiField(provided = true)
    SuggestedMultiSelectionView<T> multiSelect;

    @UiField
    DivElement headerElementUi;

    @UiField
    SpanElement headerTitleUi;

    @UiField
    FlowPanel itemContainerUi;

    @UiField
    InlineEditButton editButtonUi;

    @UiField
    Label competitorsEmptyUi;

    @UiField
    DivElement headerPanelUi;

    private final Map<T, IsWidget> tableElements = new HashMap<>();
    private final Function<T, IsWidget> itemProducer;

    private boolean editMode = false;
    private EditModeChangeHandler editModeChangeHandler;
    private boolean isEmpty = false;

    public EditableSuggestedMultiSelection(Function<T, IsWidget> itemProducer, SuggestedMultiSelectionView<T> multis,
            EditModeChangeHandler editModeChangeHandler, boolean isHeadless) {
        this.itemProducer = itemProducer;
        multiSelect = multis;
        this.editModeChangeHandler = editModeChangeHandler;
        DesktopAccordionResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        multiSelect.getElement().removeFromParent();
        if (isHeadless) {
            headerTitleUi.removeFromParent();
            headerPanelUi.removeClassName(SuggestedMultiSelectionResources.INSTANCE.css().suggestions());
        }
    }

    public void setEditMode(boolean state) {
        this.editMode = state;
        parentPanelUi.removeAllChildren();
        editModeChangeHandler.onEditModeChanged(state);
        if (state) {
            parentPanelUi.appendChild(multiSelect.getElement());
            competitorsEmptyUi.setVisible(false);
        } else {
            parentPanelUi.appendChild(itemContainerUi.getElement());
            competitorsEmptyUi.setVisible(isEmpty);
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

        if (itemContainerUi.getElement().getChild(0) != item.getElement()) {
            item.addStyleName(EditableResources.INSTANCE.css().listItemBorder());
        }
        item.addStyleName(EditableResources.INSTANCE.css().listItem());
        DOM.getChild(item.getElement(), 1).addClassName(EditableResources.INSTANCE.css().listItemOffset());
        DOM.getChild(item.getElement(), 2).addClassName(EditableResources.INSTANCE.css().listItemOffset());
        tableElements.put(listItem, item);
    }

    public void setSelectedItems(Iterable<T> competitors) {
        itemContainerUi.clear();
        competitors.forEach(c -> addListItem(c));
        multiSelect.setSelectedItems(competitors);

        isEmpty = Util.isEmpty(competitors);
        competitorsEmptyUi.setVisible(isEmpty);
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
