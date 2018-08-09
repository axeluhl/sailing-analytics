package com.sap.sailing.gwt.home.shared.partials.listview;

import java.util.List;
import java.util.function.Function;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatClassDTO;

public class ListView<T> extends Composite implements HasText {

    private static SuggestedMultiSelectionUiBinder uiBinder = GWT.create(SuggestedMultiSelectionUiBinder.class);

    interface SuggestedMultiSelectionUiBinder extends UiBinder<Widget, ListView<?>> {
    }

    @UiField
    SpanElement headerTitleUi;

    @UiField
    FlowPanel itemContainerUi;

    @UiField
    DivElement listContainerUi;

    private final Function<T, IsWidget> itemProducer;

    public ListView(Function<T, IsWidget> itemProducer) {
        this.itemProducer = itemProducer;
        initWidget(uiBinder.createAndBindUi(this));
        headerTitleUi.setInnerText("Title");
    }

    public void addListItem(T val) {
        GWT.log("adding list item: " + ((BoatClassDTO) val).getName());
        Widget item = itemProducer.apply(val).asWidget();

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

        if (DOM.getChild(item.getElement(), 2) != null) {
            DOM.getChild(item.getElement(), 2).getStyle().setPosition(Position.RELATIVE);
            DOM.getChild(item.getElement(), 2).getStyle().setTop(-0.333333333333333333, Unit.EM);
        }
    }

    public void setItems(List<T> competitors) {
        itemContainerUi.clear();
        competitors.forEach(c -> addListItem(c));
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
