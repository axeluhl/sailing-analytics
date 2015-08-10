package com.sap.sailing.gwt.home.mobile.places.start;

import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.stage.Stage;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.start.EventQuickfinderDTO;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;

public class StartViewImpl extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, StartViewImpl> {
    }
    
    private final StringMessages i18n = StringMessages.INSTANCE;

    private Presenter currentPresenter;

    @UiField(provided = true)
    Stage stage;

    @UiField Quickfinder quickFinderUi;
    @UiField AnchorElement showAllEventsUi;

    public StartViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        stage = new Stage(presenter.getNavigator());
        initWidget(uiBinder.createAndBindUi(this));
        
        presenter.getNavigator().getEventsNavigation().configureAnchorElement(showAllEventsUi);
    }
    
    @Override
    public void setFeaturedEvents(List<? extends EventListEventDTO> list) {
        stage.setFeaturedEvents(list);
    }

    @Override
    public void setQuickFinderValues(Collection<EventQuickfinderDTO> events) {
        // TODO change message: recent events
        quickFinderUi.addPlaceholderItem(i18n.events());
        
        for (EventQuickfinderDTO event : events) {
            String group;
            EventState state = event.getState();
            if(state == EventState.FINISHED) {
                group = TextMessages.INSTANCE.searchResultSortNewest();
            } else if(state == EventState.RUNNING) {
                group = TextMessages.INSTANCE.live();
            } else {
                group = TextMessages.INSTANCE.seriesHeaderUpcoming();
            }
            PlaceNavigation<?> eventPlaceNavigation = currentPresenter.getEventNavigation(event);
            quickFinderUi.addItemToGroup(group, event.getDisplayName(), eventPlaceNavigation);
        }
    }
}
