package com.sap.sailing.gwt.home.client.place.solutions;

import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace.SolutionsNavigationTabs;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;

public interface SolutionsClientFactory extends SailingClientFactory {
    SolutionsView createSolutionsView(SolutionsNavigationTabs navigationTab);
}
