package com.sap.sse.gwt.client.mvp;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.gwt.client.ErrorReporter;

/**
 * Provides GWT modules a way to bind specific configurations for which event bus
 * and which place controller to use. Furthermore, applications specialize this
 * interface to provide factory methods for the views their activities want to
 * produce and work with. Delegating this through this factory makes the set of
 * view implementations easily re-configurable, e.g., for responsive designs.<p>
 * 
 * The choice of implementation of this interface and its sub-interfaces may as well
 * be taken programmatically, e.g., after detecting specific properties of the
 * device running the application.<p>
 * 
 * {@link ClientFactoryImpl} is offered as a default implementations that applications
 * may subclass.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ClientFactory {
    ErrorReporter getErrorReporter();

    EventBus getEventBus();

    PlaceController getPlaceController();

    /**
     * The root widget to use as the entry point's root panel contents
     */
    Widget getRoot();
    
    /**
     * The content area in which to display the activity views when places are switched. Expected to be equal to or
     * contained by {@link #getRoot()}.
     */
    AcceptsOneWidget getContent();

    /**
     * In case the URL doesn't specify a place to navigate to, this place will be used as the initial place.
     */
    Place getDefaultPlace();
}
