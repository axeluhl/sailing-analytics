package com.sap.sailing.gwt.home.client.shared.stage;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.controls.carousel.WidgetCarousel;

public class Stage extends Composite {

    @SuppressWarnings("unused")
    private List<Pair<StageEventType, EventBaseDTO>> featuredEvents;

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

    public void setFeaturedEvents(List<Pair<StageEventType, EventBaseDTO>> featuredEvents) {
        this.featuredEvents = featuredEvents;

        for (Pair<StageEventType, EventBaseDTO> typeAndEvent : featuredEvents) {
            switch (typeAndEvent.getA()) {
            case POPULAR:
                stageTeaser = new PopularEventStageTeaser(typeAndEvent.getB(), placeNavigator);
                break;
            case RUNNING:
                stageTeaser = new LiveEventStageTeaser(typeAndEvent.getB(), placeNavigator);
                break;
            case UPCOMING_SOON:
                stageTeaser = new UpcomingEventStageTeaser(typeAndEvent.getB(), placeNavigator);
                break;
            }
            widgetCarousel.addWidget(stageTeaser);
            stageTeaserComposites.add(stageTeaser);
        }

    }

    public void adjustSize() {

    }

}
