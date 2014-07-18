package com.sap.sailing.gwt.home.client.shared.stage;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.idangerous.Swiper;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util.Pair;

public class Stage extends Composite {
    
    @SuppressWarnings("unused")
    private List<Pair<StageEventType, EventDTO>> featuredEvents;

    private List<StageTeaser> stageTeaserComposites;

    @UiField HTMLPanel stageElementsPanel;
    @UiField Anchor nextStageTeaserLink;
    @UiField Anchor prevStageTeaserLink;

    private StageTeaser stageTeaser;
    
    private Swiper swiper;

    private final PlaceNavigator placeNavigator;
    
    interface StageUiBinder extends UiBinder<Widget, Stage> {
    }
    
    private static StageUiBinder uiBinder = GWT.create(StageUiBinder.class);

    public Stage(PlaceNavigator placeNavigator) {
        this.placeNavigator = placeNavigator;
        
        StageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        stageTeaserComposites = new ArrayList<StageTeaser>();
    }
    
    public void setFeaturedEvents(List<Pair<StageEventType, EventDTO>> featuredEvents) {
        this.featuredEvents = featuredEvents;
        
        for(Pair<StageEventType, EventDTO> typeAndEvent: featuredEvents) {
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
            stageElementsPanel.add(stageTeaser);
            stageTeaserComposites.add(stageTeaser);
        }
        
        this.swiper = Swiper.createWithDefaultOptions(StageResources.INSTANCE.css().stage_teasers(), 
                StageResources.INSTANCE.css().swiperwrapper(),
                StageResources.INSTANCE.css().swiperslide(), false);
    }

    @UiHandler("nextStageTeaserLink")
    public void nextStageTeaserLinkClicked(ClickEvent e) {
        if (this.swiper != null) {
            this.swiper.swipeNext();
        }
    }

    @UiHandler("prevStageTeaserLink")
    public void prevStageTeaserLinkClicked(ClickEvent e) {
        if (this.swiper != null) {
            this.swiper.swipePrev();
        }
    }
}
