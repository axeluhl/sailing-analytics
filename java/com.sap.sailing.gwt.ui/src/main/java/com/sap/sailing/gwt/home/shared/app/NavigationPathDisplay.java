package com.sap.sailing.gwt.home.shared.app;

public interface NavigationPathDisplay {
    
    void showNavigationPath(NavigationItem... navigationPath);
    
    public class NavigationItem implements Runnable {
        private final String displayName;
        private final PlaceNavigation<?> destination;
        public NavigationItem(String displayName, PlaceNavigation<?> destination) {
            super();
            this.displayName = displayName;
            this.destination = destination;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getTargetUrl() {
            return destination.getTargetUrl();
        }
        
        @Override
        public void run() {
            destination.goToPlace();
        }
    }

}
