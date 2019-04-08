package com.sap.sailing.gwt.home.desktop.places.whatsnew;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;

public class WhatsNewPlace extends AbstractBasePlace {
    public enum WhatsNewNavigationTabs { SailingAnalytics, RaceManagerApp, InSightApp, BuoyPingerApp, PostRaceAnalytics, TrainingDiary, SailingSimulator };
    private final WhatsNewNavigationTabs navigationTab;
    private final static String PARAM_NAVIGATION_TAB = "navigationTab"; 

    public WhatsNewPlace(String url) {
        super(url);
        String paramNavTab = getParameter(PARAM_NAVIGATION_TAB);
        navigationTab = paramNavTab != null ? WhatsNewNavigationTabs.valueOf(paramNavTab) : null;
    }

    public WhatsNewPlace(WhatsNewNavigationTabs navigationTab) {
        super(PARAM_NAVIGATION_TAB, navigationTab.name());
        this.navigationTab = navigationTab;
    }

    public WhatsNewNavigationTabs getNavigationTab() {
        return navigationTab;
    }

    public static class Tokenizer implements PlaceTokenizer<WhatsNewPlace> {
        @Override
        public String getToken(WhatsNewPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public WhatsNewPlace getPlace(String url) {
            return new WhatsNewPlace(url);
        }
    }
   
}
