package com.sap.sailing.gwt.home.client.place.solutions;

import com.sap.sailing.gwt.ui.client.SailingClientFactory;

public interface SolutionsClientFactory extends SailingClientFactory {
    TabletAndDesktopSolutionsView createSolutionsView(SolutionsActivity activity);
}
