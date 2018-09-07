package com.sap.sailing.gwt.home.mobile.partials.regattaStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.LiveRaceDTO;
import com.sap.sailing.gwt.home.communication.event.RegattasAndLiveRacesDTO;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.mobile.partials.toggleButton.ToggleButton;
import com.sap.sailing.gwt.home.mobile.partials.toggleButton.ToggleButton.ToggleButtonCommand;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase.Presenter;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractSelectionFilter;
import com.sap.sailing.gwt.home.shared.partials.regattalist.RegattaListView;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;

public class RegattaStatus extends Composite implements RefreshableWidget<RegattasAndLiveRacesDTO>, RegattaListView {

    private static final String TOGGLEHIDDEN_STYLE = RegattaStatusResources.INSTANCE.css().togglecontainerhidden();
    private static RegattaStatusUiBinder uiBinder = GWT.create(RegattaStatusUiBinder.class);

    interface RegattaStatusUiBinder extends UiBinder<Widget, RegattaStatus> {
    }

    @UiField
    RegattaStatusResources local_res;
    @UiField SectionHeaderContent sectionHeaderUi;
    @UiField MobileSection regattaContainerUi;
    @UiField MobileSection collapsableContainerUi;
    @UiField(provided = true) ToggleButton toggleButtonUi;
    private final Presenter presenter;
//    private CollapseAnimation animation;
    private final Map<RegattaListItem, RegattaMetadataDTO> stucture = new HashMap<>();
    
    public RegattaStatus(Presenter presenter) {
        this.presenter = presenter;
        RegattaStatusResources.INSTANCE.css().ensureInjected();
        toggleButtonUi = new ToggleButton(new ToggleButtonCommand() {
            @Override
            protected void execute(boolean expanded) {
                Widget.setVisible(collapsableContainerUi.getElement(), expanded);
//                if (animation != null) animation.animate(expanded);
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
        Widget.setVisible(collapsableContainerUi.getElement(), false);
//        this.animation = new CollapseAnimation(collapsableContainerUi.getElement().getFirstChildElement(), false);
    }
    
    public void setFilterSectionWidget(AbstractSelectionFilter<RegattaMetadataDTO, ?> filterWidget) {
        filterWidget.setStyleName(local_res.css().regattastatus_filter());
        sectionHeaderUi.initAdditionalWidget(filterWidget);
    }
    
    @Override
    public void setData(RegattasAndLiveRacesDTO data) {
        if (stucture.isEmpty()) {
            stucture.clear();
            regattaContainerUi.clearContent();
            collapsableContainerUi.clearContent();
            if (data.hasRegattasWithRaces()) {
                for (Entry<RegattaMetadataDTO, Set<LiveRaceDTO>> pair : data.getRegattasWithRaces().entrySet()) {
                    RegattaStatusRegatta regattaWidget = addRegatta(regattaContainerUi, pair.getKey());
                    regattaWidget.addRaces(pair.getValue());
                }
            }
            toggleButtonUi.setStyleName(TOGGLEHIDDEN_STYLE,
                    !data.hasRegattasWithRaces() || !data.hasRegattasWithoutRaces());
            for (RegattaMetadataDTO regatta : data.getRegattasWithoutRaces()) {
                addRegatta(data.hasRegattasWithRaces() ? collapsableContainerUi : regattaContainerUi, regatta);
            }
        }
    }
    
    private RegattaStatusRegatta addRegatta(MobileSection container, RegattaMetadataDTO regatta) {
        PlaceNavigation<?> placeNavigation = presenter.getRegattaOverviewNavigation(regatta.getId());
        RegattaStatusRegatta regattaWidget = new RegattaStatusRegatta(regatta, placeNavigation);
        container.addContent(regattaWidget);
        stucture.put(regattaWidget, regatta);
        return regattaWidget;
    }

    @Override
    public Map<RegattaListItem, RegattaMetadataDTO> getItemMap() {
        return stucture;
    }
    
}
