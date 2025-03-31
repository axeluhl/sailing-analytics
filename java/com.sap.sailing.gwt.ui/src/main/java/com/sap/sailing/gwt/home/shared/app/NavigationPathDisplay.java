package com.sap.sailing.gwt.home.shared.app;

/**
 * Abstraction of a hierarchy regarding navigation. This concept is introduced to allow activities to define its
 * navigation path/hierarchy using a UI element that is not part of the activity itself. This allows the central
 * definition of the navigation UI without losing the ability to control the concrete navigation path for an activity in
 * a fine grained way.
 * 
 * {@link NavigationPathDisplay} uses a async mechanism to let activities set their concrete navigation path because
 * some parts of the navigation path can rely on dynamic information (e.g. event/series names) that isn't available on
 * construction of the activity.
 * 
 * The abtraction also allows different implementations/visualizations. So it's possible to render breadcrumbs on
 * desktop, while on mobile devices a popup menu is used.
 */
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
