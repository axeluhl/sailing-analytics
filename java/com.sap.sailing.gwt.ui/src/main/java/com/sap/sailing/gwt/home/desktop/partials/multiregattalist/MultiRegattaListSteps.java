package com.sap.sailing.gwt.home.desktop.partials.multiregattalist;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressSeriesDTO;
import com.sap.sailing.gwt.ui.shared.eventview.HasRegattaMetadata.RegattaState;

public class MultiRegattaListSteps extends Composite {

    private static MultiRegattaListStepsUiBinder uiBinder = GWT.create(MultiRegattaListStepsUiBinder.class);

    interface MultiRegattaListStepsUiBinder extends UiBinder<Widget, MultiRegattaListSteps> {
    }
    
    @UiField DivElement progressContainerUi;
    @UiField DivElement stepsContainerUi;
    @UiField DivElement noRacesMessageUi;
    @UiField DivElement leaderboardButtonContainerUi;
    @UiField AnchorElement leaderboardButtonUi;
    
    private HandlerRegistration windowResizeHandlerRegistration;
    private final List<MultiRegattaListStepsBody> allMultiregattaSteps = new ArrayList<MultiRegattaListStepsBody>();

    public MultiRegattaListSteps(RegattaProgressDTO regattaProgress) {
        MultiRegattaListResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        double sumParts = 0;
        for(RegattaProgressSeriesDTO seriesProgress : regattaProgress.getSeries()) {
            double parts = Math.pow(seriesProgress.getTotalRaceCount(), 0.4);
            sumParts += parts;
        }
        for(RegattaProgressSeriesDTO seriesProgress : regattaProgress.getSeries()) {
            double parts = Math.pow(seriesProgress.getTotalRaceCount(), 0.4);
            double percentage = 100.0 * parts / sumParts;
            MultiRegattaListStepsBody stepsBody = new MultiRegattaListStepsBody(seriesProgress);
            allMultiregattaSteps.add(stepsBody);
            stepsBody.getElement().getStyle().setWidth(percentage, Unit.PCT);
            stepsContainerUi.appendChild(stepsBody.getElement());
        }
        
        if(regattaProgress.getSeries().isEmpty()) {
            noRacesMessageUi.getStyle().clearDisplay();
            stepsContainerUi.removeFromParent();
        }
    }

    @Override
    protected void onLoad() {
        windowResizeHandlerRegistration = Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                for (MultiRegattaListStepsBody widget : allMultiregattaSteps) {
                    widget.onResize();
                }
            }
        });
    }

    @Override
    protected void onUnload() {
        windowResizeHandlerRegistration.removeHandler();
    }
    
    void setLeaderboardNavigation(RegattaState regattaState, PlaceNavigation<?> placeNavigation) {
        placeNavigation.configureAnchorElement(leaderboardButtonUi);
        leaderboardButtonContainerUi.getStyle().clearDisplay();
        leaderboardButtonUi.addClassName(getLeaderboardButtonStyle(regattaState));
        stepsContainerUi.getStyle().setDisplay(Display.NONE);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                stepsContainerUi.getStyle().clearDisplay();
                stepsContainerUi.getStyle().setMarginRight(leaderboardButtonUi.getOffsetWidth() + 20, Unit.PX);
            }
        });
    }

    private String getLeaderboardButtonStyle(RegattaState regattaState) {
        switch (regattaState) {
            case UPCOMING: return SharedResources.INSTANCE.mainCss().buttoninactive();
            case RUNNING: return SharedResources.INSTANCE.mainCss().buttonred();
            default: return SharedResources.INSTANCE.mainCss().buttonprimary();
        }
    }

}
