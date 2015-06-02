package com.sap.sailing.gwt.home.client.place.error;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ErrorActivity extends AbstractActivity {

    private ErrorPlace currentPlace;
    private ErrorClientFactory clientFactory;

    public ErrorActivity(ErrorPlace place, ErrorClientFactory clientFactory) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        // TODO: the error place should get the error message from the place
        final TabletAndDesktopErrorView view;
        
        Command reloadCommand = null;
        if (currentPlace.getComingFrom() != null) {
            reloadCommand = new Command() {

                @Override
                public void execute() {
                    clientFactory.getPlaceController().goTo(currentPlace.getComingFrom());
                }
            };
        }

        if (currentPlace.hasCustomErrorMessages()) {
            view = new TabletAndDesktopErrorView(
                    currentPlace.getErrorMessage(),
                    currentPlace.getErrorMessageDetail(),
                    currentPlace.getException(), reloadCommand);
        } else {
            view = new TabletAndDesktopErrorView(currentPlace.getErrorMessageDetail(), currentPlace.getException(),
                    reloadCommand);
        }
        
        panel.setWidget(view);
    }

}
