package com.sap.sailing.gwt.ui.adminconsole.places;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class AdminConsolePlace extends Place {

    private final String menu;
    private final String tab;
    
    public AdminConsolePlace() {
        this.menu = "Leaderboards";
        this.tab = "Leaderboard groups";
    }
    
    public AdminConsolePlace(final String menu, final String tab) {
        this.menu = menu;
        this.tab = tab;
    }
    
    public String getTab() {
        return tab;
    }
    
    public String getMenu() {
        return menu;
    }
    
    public static class Tokenizer implements PlaceTokenizer<AdminConsolePlace> {
        @Override
        public String getToken(final AdminConsolePlace place) {
            return place.getMenu() + ":" + place.getTab();
        }

        @Override
        public AdminConsolePlace getPlace(final String token) {
            if (token != null && token.contains(":")) {
                final String[] tabAndMenu = token.split(":");
                return new AdminConsolePlace(tabAndMenu[0], tabAndMenu[1]);
            }
            
            return new AdminConsolePlace();
        }
    }
    
}
