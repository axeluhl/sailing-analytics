package com.sap.sse.gwt.client.controls.tabbar;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Defines lifecycle handling for the Activity.
 * <p/>
 * <p/>
 * Created by pgtaboada on 25.11.14.
 */
public interface TabActivity<PLACE extends Place, PLACECONTEXT> {

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

    /**
     * Tells the tab that it is going to be deactiviated (not visible anymore), because of tab switch
     */
    void stop();

    PLACE placeToFire(PLACECONTEXT ctx);
}
