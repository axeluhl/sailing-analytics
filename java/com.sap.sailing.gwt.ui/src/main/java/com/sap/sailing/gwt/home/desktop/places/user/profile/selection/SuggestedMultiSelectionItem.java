package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
    
    protected abstract IsWidget getItemDescriptionWidget();

}
