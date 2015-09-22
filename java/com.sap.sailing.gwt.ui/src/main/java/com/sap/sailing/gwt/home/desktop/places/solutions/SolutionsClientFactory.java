package com.sap.sailing.gwt.home.desktop.places.solutions;

import com.sap.sailing.gwt.home.shared.places.solutions.SolutionsPlace.SolutionsNavigationTabs;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;

public interface SolutionsClientFactory extends SailingClientFactory {
    SolutionsView createSolutionsView(SolutionsNavigationTabs navigationTab);
}
