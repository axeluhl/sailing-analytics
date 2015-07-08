package com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList;

import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
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
        SpanElement nameElement = getDetailsItem(seriesProgress.getName());
        nameElement.getStyle().setFontWeight(FontWeight.BOLD);
        main.appendChild(nameElement);
        main.appendChild(getDetailsItem("Total - " + seriesProgress.getTotalRaceCount()));
        for (Entry<FleetMetadataDTO, Integer> fleetState : seriesProgress.getFleetState().entrySet()) {
            SpanElement element = getDetailsItem(fleetState.getKey().getFleetName() + " - " + fleetState.getValue());
            element.getStyle().setColor(fleetState.getKey().getFleetColor());
            main.appendChild(element);
        }
        setElement(main);
    }
    
    private SpanElement getDetailsItem(String text) {
        SpanElement detailsItem = DOM.createSpan().cast();
        detailsItem.getStyle().setPaddingRight(0.75, Unit.EM);
        detailsItem.setInnerText(text);
        return detailsItem;
    }
    
}
