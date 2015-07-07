package com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList;

import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressSeriesDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;

public class MultiRegattaListStepsBody extends UIObject {

    private static MultiRegattaListStepsBodyUiBinder uiBinder = GWT.create(MultiRegattaListStepsBodyUiBinder.class);

    interface MultiRegattaListStepsBodyUiBinder extends UiBinder<Element, MultiRegattaListStepsBody> {
    }

    public MultiRegattaListStepsBody(RegattaProgressSeriesDTO seriesProgress) {
        Element main = uiBinder.createAndBindUi(this);
        main.appendChild(getDetailsItem(seriesProgress.getName()));
        main.appendChild(getDetailsItem("Race count:" + seriesProgress.getTotalRaceCount()));
        for (Entry<FleetMetadataDTO, Integer> fleetState : seriesProgress.getFleetState().entrySet()) {
            DivElement div = getDetailsItem(fleetState.getKey().getFleetName() + " - " + fleetState.getValue());
            div.getStyle().setColor(fleetState.getKey().getFleetColor());
            main.appendChild(div);
        }
        setElement(main);
    }
    
    private DivElement getDetailsItem(String text) {
        DivElement detailsItem = DOM.createDiv().cast();
        detailsItem.setInnerText(text);
        return detailsItem;
    }
    
}
