package com.sap.sailing.gwt.home.client.place.solutions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.shared.solutions.Solutions;

public class TabletAndDesktopSolutionsView extends Composite implements SolutionsView {
    private static SolutionsPageViewUiBinder uiBinder = GWT.create(SolutionsPageViewUiBinder.class);

    @UiField(provided=true) Solutions solutions;
    
    interface SolutionsPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopSolutionsView> {
    }

    public TabletAndDesktopSolutionsView(HomePlacesNavigator placeNavigator) {
        super();
        
        solutions = new Solutions(placeNavigator);
        initWidget(uiBinder.createAndBindUi(this));
    }
}
