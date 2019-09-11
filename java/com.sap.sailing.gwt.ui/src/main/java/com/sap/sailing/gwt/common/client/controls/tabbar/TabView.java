package com.sap.sailing.gwt.common.client.controls.tabbar;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Defines lifecycle handling for the tab view.
 * <p/>
 * <p/>
 * Created by pgtaboada on 25.11.14.
 */
public interface TabView<PLACE extends Place, PRESENTER> {
    
    public enum State {
        VISIBLE, INVISIBLE, NOT_AVAILABLE_REDIRECT, NOT_AVAILABLE_SHOW_NEXT_AVAILABLE
    }

    /**
     * The place type that activates this tab.
     *
     * @return
     */
    Class<PLACE> getPlaceClassForActivation();

    /**
     * Lifecycle method that triggers tab view start
     *
     * @param placeToGo
     * @param contentArea
     */
    void start(PLACE requestedPlace, AcceptsOneWidget contentArea);
    
    void setPresenter(PRESENTER presenter);

    /**
     * Tells the tab that it is going to be deactiviated (not visible anymore), because of tab switch
     */
    void stop();

    PLACE placeToFire();
    
    default State getState() {
        return State.VISIBLE;
    };
}
