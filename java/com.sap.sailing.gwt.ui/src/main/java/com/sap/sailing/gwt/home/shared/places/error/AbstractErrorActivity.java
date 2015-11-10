package com.sap.sailing.gwt.home.shared.places.error;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.gwt.client.mvp.ErrorView;

public abstract class AbstractErrorActivity extends AbstractActivity {

    private final ErrorPlace currentPlace;
    private final PlaceController placeController;

    public AbstractErrorActivity(ErrorPlace place, PlaceController placeController) {
        this.currentPlace = place;
        this.placeController = placeController;
    }

    @Override
    public final void start(AcceptsOneWidget panel, EventBus eventBus) {
        Command reloadCommand = currentPlace.getComingFrom() == null ? null : new Command() {
            @Override
            public void execute() {
                placeController.goTo(currentPlace.getComingFrom());
            }
        };
        String message = currentPlace.getErrorMessage(), details = currentPlace.getErrorMessageDetail();
        panel.setWidget(currentPlace.hasCustomErrorMessages() ? 
                createView(message, details, currentPlace.getException(), reloadCommand)
                : createView(message, currentPlace.getException(), reloadCommand));
    }
    
    protected abstract ErrorView createView(String errorMsg, Throwable reason, Command reloadCommand);
    
    protected abstract ErrorView createView(String customMsg, String errorMsg, Throwable reason, Command reloadCommand);
}
