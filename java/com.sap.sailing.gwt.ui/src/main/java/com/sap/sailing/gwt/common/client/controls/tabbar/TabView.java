package com.sap.sailing.gwt.common.client.controls.tabbar;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Defines lifecycle handling for the Activity.
 * <p/>
 * <p/>
 * Created by pgtaboada on 25.11.14.
 */
public interface TabView<PLACE extends Place, PLACECONTEXT, PRESENTER> {

    /**
     * The place type that activates this tab.
     *
     * @return
     */
    Class<PLACE> getPlaceClassForActivation();

    /**
     * Initiates the tab lifecycle.
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

    PLACE placeToFire(PLACECONTEXT ctx);
}
