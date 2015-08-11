package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.regattacompetition.RegattaCompetitionResources.LocalCss;

public class RegattaCompetitionFleet extends Composite {

    private static RegattaCompetitionFleetUiBinder uiBinder = GWT.create(RegattaCompetitionFleetUiBinder.class);

    interface RegattaCompetitionFleetUiBinder extends UiBinder<Widget, RegattaCompetitionFleet> {
    }
    
    private static final LocalCss CSS = RegattaCompetitionResources.INSTANCE.css();
    
    @UiField HTMLPanel raceContainerUi;
    @UiField DivElement fleetCornerUi;
    @UiField DivElement fleetNameUi;

    public RegattaCompetitionFleet() {
        initWidget(uiBinder.createAndBindUi(this));
        
        raceContainerUi.getElement().getStyle().setBackgroundColor("rgba("+ "" + ", .1)");
        raceContainerUi.getElement().getStyle().setWidth(1337, Unit.PCT);
        // if count < 2
        raceContainerUi.addStyleName(CSS.regattacompetition_phase_fleetfullwidth());
        
        fleetCornerUi.getStyle().setProperty("borderTopColor", "");
        fleetNameUi.setInnerText("Name");
    }
    
}
