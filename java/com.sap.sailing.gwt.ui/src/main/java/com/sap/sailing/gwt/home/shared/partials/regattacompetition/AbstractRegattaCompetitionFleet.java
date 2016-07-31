package com.sap.sailing.gwt.home.shared.partials.regattacompetition;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.home.communication.race.FleetMetadataDTO;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionView.RegattaCompetitionFleetView;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.RGBColor;

public abstract class AbstractRegattaCompetitionFleet extends Composite implements RegattaCompetitionFleetView {
    
    protected AbstractRegattaCompetitionFleet(RaceCompetitionFormatFleetDTO fleet) {
        initWidget(getMainUiElement());
        getFleetNameUiElement().setInnerText(fleet.getFleet().getFleetName());
        getFleetCornerUiElement().getStyle().setProperty("borderTopColor", fleet.getFleet().getFleetColor());
        if (fleet.getFleet().isDefaultFleet()) {
            onDefaultFleetName();
        } else {
            getElement().getStyle().setBackgroundColor(getBackgroundColor(fleet.getFleet()));
        }
    }

    private String getBackgroundColor(FleetMetadataDTO fleet) {
        Triple<Integer, Integer, Integer> rgbValues = new RGBColor(fleet.getFleetColor()).getAsRGB();
        return "rgba(" + rgbValues.getA() + "," + rgbValues.getB() + "," + rgbValues.getC() + ", 0.1)";
    }
    
    @Override
    public void doFilter(boolean filter) {
        setVisible(!filter);
    }
    
    protected abstract void onDefaultFleetName();
    
    protected abstract Widget getMainUiElement();
    
    protected abstract Element getFleetNameUiElement();

    protected abstract Element getFleetCornerUiElement();

}
