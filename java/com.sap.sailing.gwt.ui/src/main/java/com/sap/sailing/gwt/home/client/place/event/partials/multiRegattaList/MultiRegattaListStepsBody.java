package com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList.MultiRegattaListResources.LocalCss;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressSeriesDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;

public class MultiRegattaListStepsBody extends UIObject {

    private static final LocalCss CSS = MultiRegattaListResources.INSTANCE.css();
    private static MultiRegattaListStepsBodyUiBinder uiBinder = GWT.create(MultiRegattaListStepsBodyUiBinder.class);

    interface MultiRegattaListStepsBodyUiBinder extends UiBinder<Element, MultiRegattaListStepsBody> {
    }
    
    @UiField DivElement nameUi;
    @UiField DivElement checkUi;
    @UiField DivElement progressUi;
    @UiField DivElement fleetsContainerUi;

    public MultiRegattaListStepsBody(RegattaProgressSeriesDTO seriesProgress) {
        Element main = uiBinder.createAndBindUi(this);
        nameUi.setInnerText(seriesProgress.getName());
        if (seriesProgress.isCompleted()) {
            progressUi.setInnerText(String.valueOf(seriesProgress.getTotalRaceCount()));
        } else {
            checkUi.removeFromParent();
            progressUi.setInnerText(seriesProgress.getProgressRaceCount() + " of " + seriesProgress.getTotalRaceCount());
        }
        Map<FleetMetadataDTO, Integer> fleetStates = seriesProgress.getFleetState();
        double fleetHeight = fleetStates.isEmpty() ? 100.0 : 100.0 / fleetStates.size();
        for (Entry<FleetMetadataDTO, Integer> fleetState : seriesProgress.getFleetState().entrySet()) {
            double fleetWidth = (fleetState.getValue() * 100.0) / seriesProgress.getTotalRaceCount();
            fleetsContainerUi.appendChild(getFleetElement(fleetWidth, fleetHeight, fleetState.getKey().getFleetColor()));
        }
        setElement(main);
    }
    
    private DivElement getFleetElement(double width, double height, String color) {
        DivElement fleetProgressUi = createDiv(CSS.regattalistitem_steps_step_fleets_fleet());
        fleetProgressUi.getStyle().setHeight(height, Unit.PCT);
        DivElement fleetProgressBarUi = createDiv(CSS.regattalistitem_steps_step_fleets_fleet_progress());
        fleetProgressBarUi.getStyle().setBackgroundColor(color);
        fleetProgressBarUi.getStyle().setWidth(width, Unit.PCT);
        fleetProgressUi.appendChild(fleetProgressBarUi);
        return fleetProgressUi;
    }
    
    private DivElement createDiv(String cssClassName) {
        DivElement div = DOM.createDiv().cast();
        div.addClassName(cssClassName);
        return div;
    }
    
}
