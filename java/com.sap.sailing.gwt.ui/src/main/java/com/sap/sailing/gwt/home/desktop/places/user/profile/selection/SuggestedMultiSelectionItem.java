package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SuggestedMultiSelectionItem<T> extends Composite {

    private static SuggestedMultiSelectionItemUiBinder uiBinder = GWT.create(SuggestedMultiSelectionItemUiBinder.class);

    interface SuggestedMultiSelectionItemUiBinder extends UiBinder<Widget, SuggestedMultiSelectionItem<?>> {
    }
    
    @UiField SimplePanel itemDescriptionContainerUi;
    @UiField Button removeItemButtonUi;
    
    public SuggestedMultiSelectionItem(T item) {
        initWidget(uiBinder.createAndBindUi(this));
        itemDescriptionContainerUi.setWidget(getItemDescriptionWidget(item));
    }
    
    protected IsWidget getItemDescriptionWidget(T item) {
        return new Label(item.toString());
    }

}
