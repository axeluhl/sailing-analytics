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
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util.Pair;

public class Stage extends Composite {
    
    @SuppressWarnings("unused")
    private List<Pair<StageEventType, EventDTO>> featuredEvents;

    private List<StageTeaser> stageTeaserComposites;
    private int indexOfVisibleStageTeaser;

    @UiField HTMLPanel stageElementsPanel;
    @UiField Anchor nextStageTeaserLink;
    @UiField Anchor prevStageTeaserLink;

    private StageTeaser stageTeaser;

    private final PlaceNavigator placeNavigator;
    
    interface StageUiBinder extends UiBinder<Widget, Stage> {
    }
    
    private static StageUiBinder uiBinder = GWT.create(StageUiBinder.class);

    public Stage(PlaceNavigator placeNavigator) {
        this.placeNavigator = placeNavigator;
        
        StageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        stageTeaserComposites = new ArrayList<StageTeaser>();
        indexOfVisibleStageTeaser = -1;
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
            stageTeaser.setVisible(false);
            stageTeaserComposites.add(stageTeaser);
            
            indexOfVisibleStageTeaser = 0;
            stageTeaserComposites.get(indexOfVisibleStageTeaser).setVisible(true);
        }
    }

    @UiHandler("nextStageTeaserLink")
    public void nextStageTeaserLinkClicked(ClickEvent e) {
        StageTeaser oldStageTeaser = stageTeaserComposites.get(indexOfVisibleStageTeaser);
        indexOfVisibleStageTeaser = indexOfVisibleStageTeaser+1;
        if(indexOfVisibleStageTeaser >= stageTeaserComposites.size()) {
            indexOfVisibleStageTeaser = 0;
        }
        StageTeaser currentStageTeaser = stageTeaserComposites.get(indexOfVisibleStageTeaser);
        oldStageTeaser.setVisible(false);
        currentStageTeaser.setVisible(true);
    }

    @UiHandler("prevStageTeaserLink")
    public void prevStageTeaserLinkClicked(ClickEvent e) {
        StageTeaser oldStageTeaser = stageTeaserComposites.get(indexOfVisibleStageTeaser);
        indexOfVisibleStageTeaser = indexOfVisibleStageTeaser-1;
        if(indexOfVisibleStageTeaser < 0) {
            indexOfVisibleStageTeaser = stageTeaserComposites.size()-1;
        }
        StageTeaser currentStageTeaser = stageTeaserComposites.get(indexOfVisibleStageTeaser);
        oldStageTeaser.setVisible(false);
        currentStageTeaser.setVisible(true);
    }
}
