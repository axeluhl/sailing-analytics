package com.sap.sailing.gwt.home.shared.partials.multiselection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

abstract class SuggestedMultiSelectionItem extends Composite {

    private static LocalUiBinder uiBinder = GWT.create(LocalUiBinder.class);

    interface LocalUiBinder extends UiBinder<Widget, SuggestedMultiSelectionItem> {
    }
    
    @UiField SimplePanel itemDescriptionContainerUi;
    @UiField Button removeItemButtonUi;
    
    SuggestedMultiSelectionItem() {
        initWidget(uiBinder.createAndBindUi(this));
        itemDescriptionContainerUi.setWidget(getItemDescriptionWidget());
    }
    
    @UiHandler("removeItemButtonUi")
    void onRemoveItemButtonClicked(ClickEvent event) {
        this.onRemoveItemRequsted();
    }
    
    protected abstract IsWidget getItemDescriptionWidget();
    
    protected abstract void onRemoveItemRequsted();

}
