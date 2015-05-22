package com.sap.sailing.gwt.home.client.shared.stage;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.ui.shared.start.EventStageDTO;
import com.sap.sse.gwt.client.controls.carousel.WidgetCarousel;

public class Stage extends Composite {

    private List<StageTeaser> stageTeaserComposites;

    @UiField
    WidgetCarousel widgetCarousel;

    private StageTeaser stageTeaser;

    private final HomePlacesNavigator placeNavigator;

    interface StageUiBinder extends UiBinder<Widget, Stage> {
    }

    private static StageUiBinder uiBinder = GWT.create(StageUiBinder.class);

    public Stage(HomePlacesNavigator placeNavigator) {
        this.placeNavigator = placeNavigator;

        StageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        stageTeaserComposites = new ArrayList<StageTeaser>();
    }

    public void setFeaturedEvents(List<EventStageDTO> list) {

        for (EventStageDTO event : list) {
            switch (event.getStageType()) {
            case POPULAR:
                stageTeaser = new PopularEventStageTeaser(event, placeNavigator);
                break;
            case RUNNING:
                stageTeaser = new LiveEventStageTeaser(event, placeNavigator);
                break;
            case UPCOMING_SOON:
                stageTeaser = new UpcomingEventStageTeaser(event, placeNavigator);
                break;
            }
            widgetCarousel.addWidget(stageTeaser);
            stageTeaserComposites.add(stageTeaser);
        }

    }
}
