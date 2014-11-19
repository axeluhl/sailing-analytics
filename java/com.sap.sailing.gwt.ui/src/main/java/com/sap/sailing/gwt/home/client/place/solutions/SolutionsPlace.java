package com.sap.sailing.gwt.home.client.place.solutions;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;

public class SolutionsPlace extends AbstractBasePlace {

    public enum SolutionsNavigationTabs { SailingAnalytics, RaceCommiteeApp, PostRaceAnalytics, TrainingDiary, SailingSimulator };
    private final SolutionsNavigationTabs navigationTab;
    private final static String PARAM_NAVIGATION_TAB = "navigationTab"; 

    public SolutionsPlace(String url) {
        super(url);
        String paramNavTab = getParameter(PARAM_NAVIGATION_TAB);
        navigationTab = paramNavTab != null ? SolutionsNavigationTabs.valueOf(paramNavTab) : null;
    }

    public SolutionsPlace(SolutionsNavigationTabs navigationTab) {
        super(PARAM_NAVIGATION_TAB, navigationTab.name());
        this.navigationTab = navigationTab;
    }

    public SolutionsNavigationTabs getNavigationTab() {
        return navigationTab;
    }

    public static class Tokenizer implements PlaceTokenizer<SolutionsPlace> {
        @Override
        public String getToken(SolutionsPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public SolutionsPlace getPlace(String url) {
            return new SolutionsPlace(url);
        }
    }
    
}
