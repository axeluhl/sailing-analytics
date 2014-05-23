package com.sap.sse.gwt.client.mvp;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Applications can use this as a default implementation of the {@link ClientFactory} interface
 * and extend the class to add their specific view  factory methods.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class ClientFactoryImpl implements ClientFactory {
    private static final EventBus eventBus = new SimpleEventBus();
    private static final PlaceController placeController = new PlaceController(eventBus);
    private final TopLevelView root;
    
    public ClientFactoryImpl(TopLevelView root) {
        this.root = root;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public PlaceController getPlaceController() {
        return placeController;
    }
    
    @Override
    public Widget getRoot() {
        return root.asWidget();
    }

    @Override
    public AcceptsOneWidget getStage() {
        return root.getStage();
    }
}
