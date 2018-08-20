package com.sap.sailing.gwt.home.desktop.partials.stage;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.start.EventStageDTO;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sse.gwt.client.controls.carousel.WidgetCarousel;

public class Stage extends Composite {

    interface StageUiBinder extends UiBinder<Widget, Stage> {
    }

    private static StageUiBinder uiBinder = GWT.create(StageUiBinder.class);
    
    @UiField
    SimplePanel widgetCarouselContainerUi;
    
    private StageTeaser stageTeaser;
    private List<StageTeaser> stageTeaserComposites;
    private final DesktopPlacesNavigator placeNavigator;

    public Stage(DesktopPlacesNavigator placeNavigator) {
        this.placeNavigator = placeNavigator;
        StageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        stageTeaserComposites = new ArrayList<StageTeaser>();
    }

    public void setFeaturedEvents(List<EventStageDTO> list) {
        final WidgetCarousel widgetCarousel = new WidgetCarousel();
        widgetCarousel.setShowDots(false);
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
        this.widgetCarouselContainerUi.setWidget(widgetCarousel);
    }
}
