package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;

public class RegattaCompetitionSeries extends Composite {

    private static RegattaCompetitionSeriesUiBinder uiBinder = GWT.create(RegattaCompetitionSeriesUiBinder.class);

    interface RegattaCompetitionSeriesUiBinder extends UiBinder<Widget, RegattaCompetitionSeries> {
    }
    
    @UiField MobileSection sectionHeaderUi;
    @UiField DivElement fleetContainerUi;

    public RegattaCompetitionSeries() {
        initWidget(uiBinder.createAndBindUi(this));
    }

}
