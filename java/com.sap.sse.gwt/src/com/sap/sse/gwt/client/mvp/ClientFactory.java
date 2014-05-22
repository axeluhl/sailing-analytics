package com.sap.sse.gwt.client.mvp;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

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
    EventBus getEventBus();

    PlaceController getPlaceController();
}
