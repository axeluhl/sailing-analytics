package com.sap.sailing.gwt.ui.adminconsole.desktop.app.places;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class EventsPlace extends Place {

    private final String menu;
    private final String tab;
    
    public EventsPlace() {
        this.menu = "Events";
        this.tab = "";
    }
    
    public EventsPlace(final String menu, final String tab) {
        this.menu = menu;
        this.tab = tab;
    }
    
    public String getTab() {
        return tab;
    }
    
    public String getMenu() {
        return menu;
    }
    
    public static class Tokenizer implements PlaceTokenizer<EventsPlace> {
        @Override
        public String getToken(final EventsPlace place) {
            return place.getMenu() + ":" + place.getTab();
        }

        @Override
        public EventsPlace getPlace(final String token) {
            if (token != null && token.contains(":")) {
                final String[] tabAndMenu = token.split(":");
                final String tab = tabAndMenu.length < 2 ? "" : tabAndMenu[1];
                return new EventsPlace(tabAndMenu[0], tab);
            }
            
            return new EventsPlace();
        }
    }
    
}
