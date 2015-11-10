package com.sap.sse.gwt.client.mvp;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.gwt.client.ErrorReporter;

/**
 * Applications can use this as a default implementation of the {@link ClientFactory} interface
 * and extend the class to add their specific view  factory methods.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class ClientFactoryImpl<TLV extends TopLevelView> implements ClientFactory {
    private final EventBus eventBus;
    private final PlaceController placeController;
    private final TLV root;
    
    public ClientFactoryImpl(TLV root) {
        this(root, new SimpleEventBus());
    }
    
    protected ClientFactoryImpl(TLV root, EventBus eventBus) {
        this(root, eventBus, new PlaceController(eventBus));
    }
    
    protected ClientFactoryImpl(TLV root, EventBus eventBus, PlaceController placeController) {
        this.root = root;
        this.eventBus = eventBus;
        this.placeController = placeController;
    }
    
    protected TLV getTopLevelView() {
        return root;
    }

    @Override
    public ErrorReporter getErrorReporter() {
        return root.getErrorReporter();
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
    public AcceptsOneWidget getContent() {
        return root.getContent();
    }
}
