package com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
    @UiField DivElement leaderboardButtonContainerUi;
    @UiField AnchorElement leaderboardButtonUi;
    
    public MultiRegattaListSteps(RegattaProgressDTO regattaProgress) {
        MultiRegattaListResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        for(RegattaProgressSeriesDTO seriesProgress : regattaProgress.getSeries()) {
            stepsContainerUi.appendChild(new MultiRegattaListStepsBody(seriesProgress).getElement());
        }
    }
    
    void setLeaderboardNavigation(RegattaState regattaState, PlaceNavigation<?> placeNavigation) {
        placeNavigation.configureAnchorElement(leaderboardButtonUi);
        leaderboardButtonUi.getStyle().clearDisplay();
        leaderboardButtonUi.addClassName(getLeaderboardButtonStyle(regattaState));
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
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
