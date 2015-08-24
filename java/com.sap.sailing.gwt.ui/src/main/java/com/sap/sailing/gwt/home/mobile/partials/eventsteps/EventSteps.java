package com.sap.sailing.gwt.home.mobile.partials.eventsteps;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressSeriesDTO;

public class EventSteps extends Composite {

    private static EventStepsUiBinder uiBinder = GWT.create(EventStepsUiBinder.class);

    interface EventStepsUiBinder extends UiBinder<MobileSection, EventSteps> {
    }
    
    @UiField SectionHeaderContent sectionHeaderUi;
    private final MobileSection regattaProgessUi;

    public EventSteps(RegattaProgressDTO regattaProgress) {
        initWidget(regattaProgessUi = uiBinder.createAndBindUi(this));
        sectionHeaderUi.setTitle(StringMessages.INSTANCE.progress());
        for (RegattaProgressSeriesDTO seriesProgress : regattaProgress.getSeries()) {
            regattaProgessUi.addContent(new EventStepsPhase(seriesProgress));
        }
    }
    
}
