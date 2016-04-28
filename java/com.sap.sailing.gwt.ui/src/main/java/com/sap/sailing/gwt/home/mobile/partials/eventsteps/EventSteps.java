package com.sap.sailing.gwt.home.mobile.partials.eventsteps;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.communication.regatta.RegattaProgressSeriesDTO;
import com.sap.sailing.gwt.home.communication.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EventSteps extends Composite implements RefreshableWidget<RegattaWithProgressDTO> {

    private static EventStepsUiBinder uiBinder = GWT.create(EventStepsUiBinder.class);

    interface EventStepsUiBinder extends UiBinder<MobileSection, EventSteps> {
    }
    
    @UiField SectionHeaderContent sectionHeaderUi;
    private final MobileSection regattaProgessUi;

    public EventSteps() {
        EventStepsResources.INSTANCE.css().ensureInjected();
        initWidget(regattaProgessUi = uiBinder.createAndBindUi(this));
        sectionHeaderUi.setSectionTitle(StringMessages.INSTANCE.progress());
        regattaProgessUi.setEdgeToEdgeContent(true);
    }
    
    @Override
    public void setData(RegattaWithProgressDTO data) {
        regattaProgessUi.clearContent();
        for (RegattaProgressSeriesDTO seriesProgress : data.getProgress().getSeries()) {
            regattaProgessUi.addContent(new EventStepsPhase(seriesProgress));
        }
    }
    
}
