package com.sap.sse.security.ui.authentication;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.Event.Type;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;

// TODO Refactor to com.sap.sse.gwt if needed
/**
 * Controller class for a wrapped (independent) management of the GWT {@link Activity} and {@link Place}s framework,
 * which can be configured by providing a {@link PlaceManagementConfiguration} implementation.
 */
public class WrappedPlaceManagementController {

    /**
     * Interface to configure a {@link WrappedPlaceManagementController}.
     * 
     * @see ActivityMapper
     */
    public interface PlaceManagementConfiguration extends ActivityMapper {
        /**
         * Defines the {@link Place} where the internal {@link PlaceController} should go to, if the controllers
         * {@link WrappedPlaceManagementController#start() start method} is called.
         * 
         * @return an instance of the {@link Place} to go to
         * 
         * @see PlaceController#goTo(Place)
         */
        Place getStartPlace();

        /**
         * Defines the {@link AcceptsOneWidget display} for the wrapped framework, which is used within the internal
         * {@link ActivityManager}.
         * 
         * @return an instance of {@link AcceptsOneWidget}
         * 
         * @see ActivityManager#setDisplay(AcceptsOneWidget)
         */
        AcceptsOneWidget getDisplay();

        /**
         * Provides the internal {@link PlaceController} to implementing classes, so they are able to pass it to
         * {@link Activity} instances to which the respective {@link Place}s are
         * {@link ActivityMapper#getActivity(Place) mapped}.
         * 
         * @param placeController
         */
        void setPlaceController(PlaceController placeController);
    }

    private final PlaceManagementConfiguration wrappedActivityMapper;
    private final EventBus eventBus = new SimpleEventBus();
    private final PlaceController wrappedPlaceController = new PlaceController(eventBus);

    /**
     * Creates a new {@link WrappedPlaceManagementController} instance using the given
     * {@link PlaceManagementConfiguration}.
     * 
     * @param configuration
     *            the {@link PlaceManagementConfiguration} instance to use
     */
    public WrappedPlaceManagementController(PlaceManagementConfiguration configuration) {
        this.wrappedActivityMapper = configuration;
        this.wrappedActivityMapper.setPlaceController(this.wrappedPlaceController);
        ActivityManager wrappedActivityManager = new ActivityManager(this.wrappedActivityMapper, eventBus);
        wrappedActivityManager.setDisplay(configuration.getDisplay());
    }

    /**
     * Go to the {@link PlaceManagementConfiguration#getStartPlace() configured} start {@link Place}.
     */
    public void start() {
        this.wrappedPlaceController.goTo(this.wrappedActivityMapper.getStartPlace());
    }

    /**
     * Tells the wrapped framework to go to the given {@link Place} by passing it to the internal
     * {@link PlaceController}.
     * 
     * @param newPlace
     *            a {@link Place} instance to go to
     * 
     * @see PlaceController#goTo(Place)
     */
    public void goTo(Place newPlace) {
        this.wrappedPlaceController.goTo(newPlace);
    }

    /**
     * Registers the given handler at the framework's internal {@link EventBus}.
     * 
     * @param type
     *            the {@link Type} associated with the handler
     * @param handler
     *            the handler to register
     * @return the {@link HandlerRegistration}, which can be stored in order to remove the handler later
     * 
     * @see EventBus#addHandler(Type, Object)
     */
    public <H> HandlerRegistration addHandler(Type<H> type, H handler) {
        return eventBus.addHandler(type, handler);
    }

    /**
     * Fires the given {@link AuthenticationContextEvent} on the internal {@link EventBus}.
     * 
     * @param event
     *            the {@link AuthenticationContextEvent} instance to fire
     * 
     * @see EventBus#fireEvent(com.google.web.bindery.event.shared.Event)
     */
    public void fireEvent(AuthenticationContextEvent event) {
        eventBus.fireEvent(event);
    }

}
