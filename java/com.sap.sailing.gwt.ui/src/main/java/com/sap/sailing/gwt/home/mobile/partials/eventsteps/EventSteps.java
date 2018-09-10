package com.sap.sailing.gwt.home.mobile.partials.eventsteps;

import java.util.function.Function;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.common.client.BoatClassImageResolver;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.communication.regatta.RegattaProgressSeriesDTO;
import com.sap.sailing.gwt.home.communication.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderDataIndicators;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;

public class EventSteps extends Composite implements RefreshableWidget<RegattaWithProgressDTO> {

    private static EventStepsUiBinder uiBinder = GWT.create(EventStepsUiBinder.class);

    interface EventStepsUiBinder extends UiBinder<MobileSection, EventSteps> {
    }
    
    @UiField EventStepsResources local_res;
    @UiField SectionHeaderContent sectionHeaderUi;
    private final MobileSection regattaProgessUi;
    private final Function<String, PlaceNavigation<?>> racesNavigationFactory;

    public EventSteps(RegattaMetadataDTO regatta, Function<String, PlaceNavigation<?>> racesNavigationFactory) {
        this.racesNavigationFactory = racesNavigationFactory;
        EventStepsResources.INSTANCE.css().ensureInjected();
        initWidget(regattaProgessUi = uiBinder.createAndBindUi(this));
        ImageResource boatClassIcon = BoatClassImageResolver.getBoatClassIconResource(regatta.getBoatClass());
        sectionHeaderUi.setImageUrl(boatClassIcon.getSafeUri().asString());
        sectionHeaderUi.setSectionTitle(regatta.getDisplayName());
        regattaProgessUi.setEdgeToEdgeContent(true);
        sectionHeaderUi.initBoatClassPopup(regatta.getBoatClass());
        sectionHeaderUi.initAdditionalWidget(new SectionHeaderDataIndicators(regatta.getRaceDataInfo()));
    }
    
    @Override
    public void setData(RegattaWithProgressDTO data) {
        regattaProgessUi.clearContent();
        final boolean showSeriesName = data.getProgress().getSeries().size() > 1;
        for (RegattaProgressSeriesDTO seriesProgress : data.getProgress().getSeries()) {
            final PlaceNavigation<?> navigation = racesNavigationFactory.apply(seriesProgress.getName());
            regattaProgessUi.addContent(new EventStepsPhase(seriesProgress, navigation, showSeriesName));
        }
    }
    
}
