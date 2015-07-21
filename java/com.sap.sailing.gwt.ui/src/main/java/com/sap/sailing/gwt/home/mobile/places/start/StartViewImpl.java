package com.sap.sailing.gwt.home.mobile.places.start;

import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
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
import com.sap.sailing.gwt.ui.shared.start.EventStageDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class StartViewImpl extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, StartViewImpl> {
    }
    
    private final StringMessages i18n = StringMessages.INSTANCE;

    private Presenter currentPresenter;

    @UiField(provided = true)
    Stage stage;

    @UiField Quickfinder quickFinderUi;

    public StartViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        stage = new Stage(presenter.getNavigator());
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setFeaturedEvents(List<EventStageDTO> list) {
        stage.setFeaturedEvents(list);
    }

    @Override
    public void setQuickFinderValues(Collection<EventQuickfinderDTO> events) {
        // TODO change message: recent events
        quickFinderUi.addPlaceholderItem(i18n.events());
        
        TimePoint now = MillisecondsTimePoint.now();
        for (EventQuickfinderDTO event : events) {
            String group;
            if(now.before(event.getStartTimePoint())) {
                group = TextMessages.INSTANCE.seriesHeaderUpcoming();
            } else {
                group = TextMessages.INSTANCE.searchResultSortNewest();
            }
            PlaceNavigation<?> eventPlaceNavigation = currentPresenter.getEventNavigation(event);
            quickFinderUi.addItemToGroup(group, event.getDisplayName(), eventPlaceNavigation);
        }
    }
}
